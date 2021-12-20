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
package io.esastack.restlight.server.bootstrap;

import esa.commons.Checks;
import esa.commons.annotation.Internal;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.route.CompletionHandler;
import io.esastack.restlight.server.route.ExceptionHandler;
import io.esastack.restlight.server.route.ExecutionHandler;
import io.esastack.restlight.server.route.Route;
import io.esastack.restlight.server.route.RouteExecution;
import io.esastack.restlight.server.route.RouteFailureException;
import io.esastack.restlight.server.route.impl.AbstractRouteRegistry;
import io.esastack.restlight.server.route.predicate.RoutePredicate;
import io.esastack.restlight.server.schedule.RequestTask;
import io.esastack.restlight.server.util.ErrorDetail;
import io.esastack.restlight.server.util.Futures;
import io.esastack.restlight.server.util.LoggerUtils;
import io.esastack.restlight.server.util.PromiseUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.LongAdder;

/**
 * Default implementation of {@link DispatcherHandler}.
 */
@Internal
public class DispatcherHandlerImpl implements DispatcherHandler {

    private static final Logger logger = LoggerFactory.getLogger(DispatcherHandlerImpl.class);

    private final AbstractRouteRegistry registry;
    private final ExceptionHandlerChain exceptionHandler;

    private final LongAdder rejectCount = new LongAdder();

    public DispatcherHandlerImpl(AbstractRouteRegistry registry,
                                 ExceptionHandlerChain exceptionHandler) {
        Checks.checkNotNull(registry, "registry");
        Checks.checkNotNull(exceptionHandler, "exceptionHandler");
        this.registry = registry;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public List<Route> routes() {
        return Collections.unmodifiableList(registry.routes());
    }

    @Override
    public Route route(RequestContext context) {
        return registry.route(context);
    }

    @Override
    public void service(RequestContext context,
                        CompletableFuture<Void> promise,
                        Route route) {
        final RouteExecution execution = route.executionFactory().create(context);
        final ExecutionHandler executionHandler = execution.executionHandler();
        try {
            executionHandler.handle(context)
                    // wind up
                    .whenComplete((r, t) -> {
                        final Throwable ex = Futures.unwrapCompletionException(t);
                        final ExceptionHandler<Throwable> exHandler;
                        if (ex != null && (exHandler = execution.exceptionHandler()) != null) {
                            try {
                                exHandler.handleException(context, ex)
                                        .whenComplete((voidRet, err) ->
                                                cleanUp(context, execution.completionHandler(), promise,
                                                        Futures.unwrapCompletionException(err)));
                            } catch (Throwable e) {
                                cleanUp(context, execution.completionHandler(), promise, e);
                            }
                        } else {
                            cleanUp(context, execution.completionHandler(), promise, ex);
                        }
                    });
        } catch (Throwable throwable) {
            logger.error("Unexpected error occurred in asynchronous execution.", throwable);
            // error while invoking route or apply postHandle()
            cleanUp(context, execution.completionHandler(), promise, throwable);
        }
    }

    @Override
    public void handleRejectedWork(RequestTask task, String reason) {
        task.response().status(HttpStatus.TOO_MANY_REQUESTS.code());
        task.response().entity(new ErrorDetail<>(task.request().path(), reason));

        LoggerUtils.logger().error("RequestTask(url={}, method={}) rejected, {}", task.request().path(),
                task.request().method(), reason);
        final CompletableFuture<Void> p = task.promise();
        if (!p.isDone()) {
            PromiseUtils.setSuccess(task.promise(), true);
        }
        rejectCount.increment();
    }

    @Override
    public void handleUnfinishedWorks(List<RequestTask> unfinishedWorkList) {
        HttpResponse response;
        for (RequestTask task : unfinishedWorkList) {
            response = task.response();
            response.status(HttpStatus.SERVICE_UNAVAILABLE.code());
            response.entity(new ErrorDetail<>(task.request().path(),
                    "The request was not processed correctly before the server was shutdown"));
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

    public static RouteFailureException.RouteFailure notFound(RequestContext context) {
        HttpRequest request = context.request();
        LoggerUtils.logger().warn("No mapping for request(url={}, method={})",
                request.path(), request.method());
        RouteFailureException.RouteFailure cause = context.attr(RoutePredicate.MISMATCH_ERR).getAndRemove();
        if (cause == null) {
            cause = RouteFailureException.RouteFailure.PATTERN_MISMATCH;
        }
        return cause;
    }

    private void cleanUp(RequestContext context,
                         CompletionHandler completionHandler,
                         CompletableFuture<Void> promise,
                         Throwable dispatchException) {
        //clean up response.
        if (dispatchException != null) {
            exceptionHandler.handle(context, dispatchException)
                    .whenComplete((v, th) -> completeRequest(context, completionHandler, promise, th));
        } else {
            completeRequest(context, completionHandler, promise, null);
        }
    }

    private void completeRequest(RequestContext context,
                                 CompletionHandler completionHandler,
                                 CompletableFuture<Void> promise,
                                 Throwable dispatchException) {
        final HttpRequest request = context.request();
        final HttpResponse response = context.response();
        if (dispatchException != null) {
            logger.error("Error occurred when doing request(url={}, method={})",
                    request.path(), request.method(), dispatchException);
        }

        if (completionHandler == null) {
            completeRequest0(request, response, promise);
            return;
        }

        try {
            completionHandler.onComplete(context, dispatchException)
                    .whenComplete((r, t) -> {
                        if (t != null) {
                            logger.error("Error while triggering afterCompletion() for request(url={},method={})",
                                    request.path(), request.method(), t);
                        }
                        completeRequest0(request, response, promise);
                    });

        } catch (Throwable throwable) {
            logger.error("Error while triggering afterCompletion() for request(url={},method={})",
                    request.path(), request.method(), throwable);
            completeRequest0(request, response, promise);
        }
    }

    private void completeRequest0(HttpRequest request, HttpResponse response, CompletableFuture<Void> promise) {
        if (logger.isDebugEnabled()) {
            logger.debug("Request(url={}, method={}) completed. {}",
                    request.path(), request.method(), response.status());
        }
        PromiseUtils.setSuccess(promise);
    }
}
