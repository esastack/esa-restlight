/*
 * Copyright 2020 OPPO ESA Stack Project
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
package esa.restlight.server.bootstrap;

import esa.commons.Checks;
import esa.commons.annotation.Internal;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.server.route.CompletionHandler;
import esa.restlight.server.route.ExceptionHandler;
import esa.restlight.server.route.ReadOnlyRouteRegistry;
import esa.restlight.server.route.Route;
import esa.restlight.server.route.RouteExecution;
import esa.restlight.server.schedule.RequestTask;
import esa.restlight.server.util.Futures;
import esa.restlight.server.util.LoggerUtils;
import esa.restlight.server.util.PromiseUtils;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.LongAdder;

/**
 * Default implementation of {@link DispatcherHandler}.
 */
@Internal
public class DefaultDispatcherHandler implements DispatcherHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(DefaultDispatcherHandler.class);
    private final ReadOnlyRouteRegistry registry;
    private final DispatcherExceptionHandler exHandler;

    private final LongAdder rejectCount = new LongAdder();

    public DefaultDispatcherHandler(ReadOnlyRouteRegistry registry, DispatcherExceptionHandler exHandler) {
        Checks.checkNotNull(registry, "registry");
        Checks.checkNotNull(exHandler, "exceptionHandler");
        this.registry = registry;
        this.exHandler = exHandler;
    }


    @Override
    public List<Route> routes() {
        return registry.routes();
    }

    @Override
    public Route route(AsyncRequest request,
                       AsyncResponse response) {
        return registry.route(request);
    }

    @Override
    public void service(AsyncRequest request,
                        AsyncResponse response,
                        CompletableFuture<Void> promise,
                        Route route) {

        final RouteExecution execution;
        try {
            execution = route.toExecution(request);
        } catch (Throwable t) {
            cleanUp(request, response, promise, t, null);
            return;
        }

        try {
            execution.handle(request, response)
                    // wind up
                    .whenComplete((r, t) -> {
                        final Throwable ex = Futures.unwrapCompletionException(t);
                        final ExceptionHandler<Throwable> exHandler;
                        if (ex != null && (exHandler = execution.exceptionHandler()) != null) {
                            try {
                                exHandler.handleException(request, response, ex)
                                        .whenComplete((voidRet, err) ->
                                                cleanUp(request, response, promise,
                                                        Futures.unwrapCompletionException(err), execution));
                            } catch (Throwable e) {
                                cleanUp(request, response, promise, e, execution);
                            }
                        } else {
                            cleanUp(request, response, promise, ex, execution);
                        }
                    });

        } catch (Throwable throwable) {
            logger.error("Unexpected error occurred in asynchronous execution.", throwable);
            // error while invoking route or apply postHandle()
            cleanUp(request, response, promise, throwable, execution);
        }
    }

    private void cleanUp(AsyncRequest request,
                         AsyncResponse response,
                         CompletableFuture<Void> promise,
                         Throwable dispatchException,
                         RouteExecution execution) {
        final ExceptionHandleResult result = exHandler.handleException(request, response, dispatchException);
        final Throwable remained;
        if (result == null) {
            remained = dispatchException;
        } else if (result.handled) {
            remained = null;
        } else {
            remained = result.remained;
        }

        if (execution == null) {
            completeRequest(request, response, promise);
            return;
        }

        try {
            final CompletionHandler completionHandler
                    = execution.completionHandler();
            if (completionHandler == null) {
                completeRequest(request, response, promise);
                return;
            }

            completionHandler.onComplete(request, response, remained)
                    .whenComplete((r, t) -> {
                        if (t != null) {
                            logger.error("Error while triggering afterCompletion() for request(url={},method={})",
                                    request.path(), request.method(), t);
                        }
                        completeRequest(request, response, promise);
                    });

        } catch (Throwable throwable) {
            logger.error("Error while triggering afterCompletion() for request(url={},method={})",
                    request.path(), request.method(), throwable);
            completeRequest(request, response, promise);
        }
    }

    private void completeRequest(AsyncRequest request, AsyncResponse response, CompletableFuture<Void> promise) {
        if (logger.isDebugEnabled()) {
            logger.debug("Request(url={}, method={}) completed. {}",
                    request.path(), request.method(), response.status());
        }
        PromiseUtils.setSuccess(promise);
    }

    @Override
    public void handleRejectedWork(RequestTask task, String reason) {
        if (!task.response().isCommitted()) {
            DefaultDispatcherExceptionHandler.sendErrorResult(task.request(),
                    task.response(),
                    reason,
                    HttpResponseStatus.TOO_MANY_REQUESTS);
            LoggerUtils.logger().error("Task({}) rejected, {}", task, reason);
        }
        final CompletableFuture<Void> p = task.promise();
        if (!p.isDone()) {
            PromiseUtils.setSuccess(task.promise(), true);
        }
        rejectCount.increment();
    }

    @Override
    public void handleUnfinishedWorks(List<RequestTask> unfinishedWorkList) {
        AsyncResponse response;
        for (RequestTask task : unfinishedWorkList) {
            response = task.response();
            if (!response.isCommitted()) {
                DefaultDispatcherExceptionHandler.sendErrorResult(task.request(),
                        task.response(),
                        "The request was not processed correctly before the server was shutdown",
                        HttpResponseStatus.SERVICE_UNAVAILABLE);
            }
            final CompletableFuture<Void> p = task.promise();
            if (!p.isDone()) {
                PromiseUtils.setSuccess(task.promise(), true);
            }
        }
    }

    @Override
    public long rejectCount() {
        return this.rejectCount.sum();
    }
}
