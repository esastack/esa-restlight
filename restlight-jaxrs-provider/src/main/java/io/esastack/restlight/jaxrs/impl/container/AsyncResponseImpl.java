/*
 * Copyright 2021 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.esastack.restlight.jaxrs.impl.container;

import esa.commons.Checks;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.TimeoutHandler;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class AsyncResponseImpl implements AsyncResponse {

    private static final UnsupportedOperationException UNSUPPORTED_REGISTRATION =
            new UnsupportedOperationException("registration operation is unsupported.");

    private static final Timer TIME_OUT_SCHEDULER = new HashedWheelTimer();
    private static final AtomicIntegerFieldUpdater<AsyncResponseImpl> STATUS_UPDATER = AtomicIntegerFieldUpdater
            .newUpdater(AsyncResponseImpl.class, "state");

    private final CompletableFuture<Object> asyncResponse;
    private final ReentrantLock handlerLock = new ReentrantLock();
    private final AtomicReference<TimeoutTask> timeoutTask = new AtomicReference<>();

    private volatile int state = AsyncState.SUSPENDED.code;

    public AsyncResponseImpl(CompletableFuture<Object> asyncResponse) {
        Checks.checkNotNull(asyncResponse, "asyncResponse");
        this.asyncResponse = asyncResponse;
    }

    @Override
    public boolean resume(Object response) {
        if (STATUS_UPDATER.compareAndSet(this, AsyncState.SUSPENDED.code, AsyncState.RESUMED.code)) {
            if (response instanceof Throwable) {
                asyncResponse.completeExceptionally((Throwable) response);
            } else {
                asyncResponse.complete(response);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean resume(Throwable response) {
        if (STATUS_UPDATER.compareAndSet(this, AsyncState.SUSPENDED.code, AsyncState.RESUMED.code)) {
            asyncResponse.completeExceptionally(response);
            return true;
        }
        return false;
    }

    @Override
    public boolean cancel() {
        if (AsyncState.CANCELLED.code == STATUS_UPDATER.get(this)) {
            return true;
        }
        if (STATUS_UPDATER.compareAndSet(this, AsyncState.SUSPENDED.code, AsyncState.CANCELLED.code)) {
            doCancel(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean cancel(int retryAfter) {
        if (AsyncState.CANCELLED.code == STATUS_UPDATER.get(this)) {
            return true;
        }
        if (STATUS_UPDATER.compareAndSet(this, AsyncState.SUSPENDED.code, AsyncState.CANCELLED.code)) {
            doCancel(retryAfter);
            return true;
        }
        return false;
    }

    @Override
    public boolean cancel(Date retryAfter) {
        if (AsyncState.CANCELLED.code == STATUS_UPDATER.get(this)) {
            return true;
        }
        if (STATUS_UPDATER.compareAndSet(this, AsyncState.SUSPENDED.code, AsyncState.CANCELLED.code)) {
            doCancel(retryAfter);
        }
        return false;
    }

    @Override
    public boolean isSuspended() {
        return AsyncState.SUSPENDED.code == STATUS_UPDATER.get(this);
    }

    @Override
    public boolean isCancelled() {
        return AsyncState.CANCELLED.code == STATUS_UPDATER.get(this);
    }

    @Override
    public boolean isDone() {
        return asyncResponse.isDone();
    }

    @Override
    public boolean setTimeout(long time, TimeUnit unit) {
        if (!isSuspended()) {
            return false;
        }
        handlerLock.lock();
        try {
            // cancel the previous task immediately.
            TimeoutTask task = timeoutTask.get();
            if (task != null && task.task != null) {
                task.task.cancel();
            }

            final Timeout newTimeoutTask;
            if (time <= 0L) {
                // we avoid adding the task which used to suspend indefinitely
                newTimeoutTask = null;
            } else {
                newTimeoutTask = TIME_OUT_SCHEDULER.newTimeout((timeout) -> {
                    if (!timeout.isCancelled()) {
                        TimeoutTask t = timeoutTask.get();
                        if (t != null && t.handler != null) {
                            t.handler.handleTimeout(this);
                            // clean the timeout task
                            timeoutTask.updateAndGet(pre -> new TimeoutTask(t.timeout, t.unit, t.handler, null));
                        }
                    }
                }, time, unit);
            }
            timeoutTask.updateAndGet(pre -> new TimeoutTask(time, unit, task != null ? task.handler : null,
                    newTimeoutTask));
        } finally {
            handlerLock.unlock();
        }
        return true;
    }

    @Override
    public void setTimeoutHandler(TimeoutHandler handler) {
        handlerLock.lock();
        try {
            final TimeoutTask newTask;
            TimeoutTask task = timeoutTask.get();
            if (task != null) {
                newTask = new TimeoutTask(task.timeout, task.unit, handler, task.task);
            } else {
                newTask = new TimeoutTask(0, TimeUnit.MICROSECONDS, handler, null);
            }
            timeoutTask.updateAndGet(pre -> newTask);
        } finally {
            handlerLock.unlock();
        }
    }

    @Override
    public Collection<Class<?>> register(Class<?> callback) {
        throw UNSUPPORTED_REGISTRATION;
    }

    @Override
    public Map<Class<?>, Collection<Class<?>>> register(Class<?> callback, Class<?>... callbacks) {
        throw UNSUPPORTED_REGISTRATION;
    }

    @Override
    public Collection<Class<?>> register(Object callback) {
        throw UNSUPPORTED_REGISTRATION;
    }

    @Override
    public Map<Class<?>, Collection<Class<?>>> register(Object callback, Object... callbacks) {
        throw UNSUPPORTED_REGISTRATION;
    }

    private void doCancel(Object retryAfter) {
        handlerLock.lock();
        try {
            Response.ResponseBuilder builder = Response.status(Response.Status.SERVICE_UNAVAILABLE);
            if (retryAfter != null) {
                builder.header(HttpHeaders.RETRY_AFTER, retryAfter);
            }
            this.completeResponse(builder.build());
            TimeoutTask task = timeoutTask.get();
            if (task != null && task.task != null) {
                task.task.cancel();
                timeoutTask.getAndSet(new TimeoutTask(task.timeout, task.unit, task.handler, null));
            }
        } finally {
            handlerLock.unlock();
        }
    }

    private void completeResponse(Object response) {
        if (asyncResponse.isDone()) {
            asyncResponse.complete(response);
        }
    }

    private enum AsyncState {

        SUSPENDED((byte) 0),

        RESUMED((byte) 1),

        CANCELLED((byte) 2);

        private final byte code;

        AsyncState(byte code) {
            this.code = code;
        }
    }

    private static class TimeoutTask {

        private final long timeout;
        private final TimeUnit unit;
        private final TimeoutHandler handler;
        private final Timeout task;

        private TimeoutTask(long timeout, TimeUnit unit, TimeoutHandler handler, Timeout task) {
            this.timeout = timeout;
            this.unit = unit;
            this.handler = handler;
            this.task = task;
        }
    }
}

