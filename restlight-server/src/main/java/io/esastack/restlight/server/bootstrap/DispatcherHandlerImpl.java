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

import static io.esastack.restlight.server.util.ErrorDetail.getMessage;

/**
 * Default implementation of {@link DispatcherHandler}.
 */
@Internal
public class DispatcherHandlerImpl implements DispatcherHandler {

    private static final Logger logger = LoggerFactory.getLogger(DispatcherHandlerImpl.class);

    private final AbstractRouteRegistry registry;
    private final IExceptionHandler[] exceptionHandlers;

    private final LongAdder rejectCount = new LongAdder();

    public DispatcherHandlerImpl(AbstractRouteRegistry registry, IExceptionHandler[] exceptionHandlers) {
        Checks.checkNotNull(registry, "registry");
        Checks.checkNotNull(exceptionHandlers, "exceptionHandlers");
        this.registry = registry;
        this.exceptionHandlers = exceptionHandlers;
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
                        if (ex != null) {
                            try {
                                final ExceptionHandler<Throwable> exHandler = execution.exceptionHandler();
                                LinkedExceptionHandlerChain.immutable(exceptionHandlers, (ctx, th) -> {
                                    if (th == null) {
                                        return Futures.completedFuture();
                                    } else {
                                        if (exHandler != null) {
                                            return exHandler.handleException(ctx, th);
                                        } else {
                                            return Futures.completedExceptionally(th);
                                        }
                                    }
                                }).handle(context, ex)
                                        .whenComplete((voidRet, err) -> completeRequest(context,
                                                execution.completionHandler(), promise,
                                                Futures.unwrapCompletionException(err)));
                            } catch (Throwable e) {
                                completeRequest(context, execution.completionHandler(), promise, e);
                            }
                        } else {
                            completeRequest(context, execution.completionHandler(), promise, ex);
                        }
                    });
        } catch (Throwable throwable) {
            logger.error("Unexpected error occurred in asynchronous execution.", throwable);
            // error while invoking route or apply postHandle()
            completeRequest(context, execution.completionHandler(), promise, throwable);
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
        RouteFailureException.RouteFailure cause = context.attrs().attr(RoutePredicate.MISMATCH_ERR).getAndRemove();
        if (cause == null) {
            cause = RouteFailureException.RouteFailure.PATTERN_MISMATCH;
        }
        return cause;
    }

    private void completeRequest(RequestContext context,
                                 CompletionHandler completionHandler,
                                 CompletableFuture<Void> promise,
                                 Throwable dispatchException) {
        final HttpRequest request = context.request();
        if (dispatchException != null) {
            logger.error("Error occurred when doing request(url={}, method={})",
                    request.path(), request.method(), dispatchException);
        }

        if (completionHandler == null) {
            completeRequest0(context, promise, dispatchException);
            return;
        }

        try {
            completionHandler.onComplete(context, dispatchException)
                    .whenComplete((r, t) -> {
                        if (t != null) {
                            logger.error("Error while triggering afterCompletion() for request(url={},method={})",
                                    request.path(), request.method(), t);
                        }
                        completeRequest0(context, promise, dispatchException);
                    });
        } catch (Throwable throwable) {
            logger.error("Error while triggering afterCompletion() for request(url={},method={})",
                    request.path(), request.method(), throwable);
            completeRequest0(context, promise, throwable);
        }
    }

    private void completeRequest0(RequestContext context, CompletableFuture<Void> promise, Throwable th) {
        if (th != null) {
            handleException(context, th);
        }
        PromiseUtils.setSuccess(promise);
    }

    public static void handleException(RequestContext context, Throwable th) {
        final HttpStatus status;
        if (th instanceof WebServerException) {
            status = ((WebServerException) th).status();
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        context.response().status(status.code());
        context.response().entity(new ErrorDetail<>(context.request().path(), getMessage(status, th)));
    }
}
