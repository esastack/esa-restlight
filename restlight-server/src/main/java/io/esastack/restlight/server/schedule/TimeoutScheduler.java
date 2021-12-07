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
package io.esastack.restlight.server.schedule;

import esa.commons.Checks;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.httpserver.core.HttpResponse;
import io.esastack.httpserver.core.RequestContext;
import io.esastack.restlight.server.config.TimeoutOptions;
import io.esastack.restlight.server.util.ErrorDetail;
import io.esastack.restlight.server.util.LoggerUtils;
import io.esastack.restlight.server.util.PromiseUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

class TimeoutScheduler implements Scheduler {

    private final Scheduler scheduler;
    private final long timeoutMillis;

    TimeoutScheduler(Scheduler scheduler, TimeoutOptions timeoutOptions) {
        Checks.checkNotNull(scheduler, "scheduler");
        Checks.checkNotNull(timeoutOptions, "timeoutOptions");
        this.scheduler = scheduler;
        this.timeoutMillis = timeoutOptions.getTimeMillis();
    }

    @Override
    public void schedule(Runnable command) {
        if (command instanceof RequestTask) {
            schedule0(new TimeoutRequestTask(((RequestTask) command), name(),
                    getStartTime((RequestTask) command), timeoutMillis));
        } else {
            scheduler.schedule(command);
        }
    }

    @Override
    public String name() {
        return scheduler.name();
    }

    @Override
    public void shutdown() {
        scheduler.shutdown();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TimeoutScheduler that = (TimeoutScheduler) o;
        return Objects.equals(name(), that.name());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name());
    }

    @Override
    public String toString() {
        return "TimeoutScheduler{name='" + name() + "'}";
    }

    long getStartTime(RequestTask task) {
        return System.currentTimeMillis();
    }

    void schedule0(TimeoutRequestTask task) {
        scheduler.schedule(task);
    }

    static final class TimeoutRequestTask implements RequestTask {

        final long startTime;
        final String schedulerName;
        final long timeout;
        private final RequestTask delegate;

        private TimeoutRequestTask(RequestTask delegate, String schedulerName, long startTime, long timeout) {
            this.delegate = delegate;
            this.schedulerName = schedulerName;
            this.startTime = startTime;
            this.timeout = timeout;
        }

        @Override
        public RequestContext context() {
            return delegate.context();
        }

        @Override
        public HttpRequest request() {
            return delegate.request();
        }

        @Override
        public HttpResponse response() {
            return delegate.response();
        }

        @Override
        public CompletableFuture<Void> promise() {
            return delegate.promise();
        }

        @Override
        public void run() {
            long actualCost;
            if ((actualCost = System.currentTimeMillis() - startTime) < timeout) {
                delegate.run();
            } else {
                failFast();
                LoggerUtils.logger().warn("Request(url = {}, method={}) has been rejected before execution: " +
                                "Out of scheduler({}) timeout ({}ms), actual costs: {}ms",
                        delegate.request().path(),
                        delegate.request().rawMethod(),
                        schedulerName,
                        timeout,
                        actualCost);
            }
        }

        void failFast() {
            byte[] errorInfo = ErrorDetail.buildErrorMsg(delegate.request().path(),
                    "Out of scheduler(" + schedulerName + ") timeout(" + timeout + ")ms",
                    HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase(),
                    HttpResponseStatus.INTERNAL_SERVER_ERROR.code());

            delegate.response().headers().set(HttpHeaderNames.CONTENT_TYPE, MediaType.TEXT_PLAIN.value());
            delegate.response().sendResult(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), errorInfo);
            PromiseUtils.setSuccess(delegate.promise());
        }

    }
}

