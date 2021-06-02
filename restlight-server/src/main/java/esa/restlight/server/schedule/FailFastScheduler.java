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
package esa.restlight.server.schedule;

import esa.commons.Checks;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.httpserver.utils.Constants;
import esa.restlight.core.util.MediaType;
import esa.restlight.server.config.FailFastOptions;
import esa.restlight.server.util.ErrorDetail;
import esa.restlight.server.util.LoggerUtils;
import esa.restlight.server.util.PromiseUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.concurrent.CompletableFuture;

public class FailFastScheduler implements Scheduler {

    private final Scheduler scheduler;
    private final long timeoutMillis;
    private final boolean ttfb;
    private final boolean allowed;

    public FailFastScheduler(Scheduler scheduler, FailFastOptions failFastOptions) {
        Checks.checkNotNull(scheduler, "scheduler");
        Checks.checkNotNull(failFastOptions, "failFastOptions");
        this.scheduler = scheduler;
        this.timeoutMillis = failFastOptions.getTimeoutMillis();
        this.ttfb = FailFastOptions.TimeoutType.TTFB == failFastOptions.getTimeoutType();
        this.allowed = timeoutMillis > 0L;
    }

    @Override
    public void schedule(Runnable command) {
        if (command instanceof RequestTask && allowed) {
            long startTime;
            if (ttfb) {
                startTime = ((RequestTask) command).request().getUncheckedAttribute(Constants.TTFB.toString());
            } else {
                startTime = System.currentTimeMillis();
            }

            // make sure startTime is an positive number.
            if (startTime <= 0L) {
                startTime = System.currentTimeMillis();
            }
            scheduler.schedule(new FailFastRequestTask(((RequestTask) command), startTime, timeoutMillis));
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

    private static final class FailFastRequestTask implements RequestTask {

        private final RequestTask delegate;
        private final long startTime;
        private final long timeout;

        private FailFastRequestTask(RequestTask delegate, long startTime, long timeout) {
            Checks.checkNotNull(delegate, "delegate");
            this.delegate = delegate;
            this.startTime = startTime;
            this.timeout = timeout;
        }

        @Override
        public AsyncRequest request() {
            return delegate.request();
        }

        @Override
        public AsyncResponse response() {
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
                failFast(delegate, timeout);
                LoggerUtils.logger().warn("Request(url = {}, method={}) has been rejected before submitting" +
                                " request task: Out of failFast({}ms), actual costs: {}ms",
                        delegate.request().path(),
                        delegate.request().rawMethod(),
                        timeout,
                        actualCost);
            }
        }

        private static void failFast(RequestTask requestTask, long timeout) {
            final byte[] errorInfo = ErrorDetail.buildErrorMsg(requestTask.request().path(),
                    "Out of Request failFast(" + timeout + ")ms",
                    HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase(),
                    HttpResponseStatus.INTERNAL_SERVER_ERROR.code());

            requestTask.response().setHeader(HttpHeaderNames.CONTENT_TYPE, MediaType.TEXT_PLAIN.value());
            requestTask.response().sendResult(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), errorInfo);
            PromiseUtils.setSuccess(requestTask.promise());
        }

    }
}

