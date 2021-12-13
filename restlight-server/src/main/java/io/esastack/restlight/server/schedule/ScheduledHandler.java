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

import io.esastack.commons.net.http.HttpStatus;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.httpserver.core.RequestContext;
import io.esastack.restlight.server.bootstrap.DispatcherHandler;
import io.esastack.restlight.server.bootstrap.DispatcherHandlerImpl;
import io.esastack.restlight.server.route.Route;
import io.esastack.restlight.server.route.RouteFailureException;
import io.esastack.restlight.server.util.Futures;
import io.esastack.restlight.server.util.LoggerUtils;
import io.esastack.restlight.server.util.PromiseUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static io.esastack.restlight.server.schedule.RequestTaskImpl.newRequestTask;

class ScheduledHandler<CTX extends RequestContext> {

    private final List<Scheduler> schedulers;
    private final RequestTaskHook hook;
    private final DispatcherHandler<CTX> dispatcher;
    private final BiConsumer<CTX, CompletableFuture<Void>> processor;

    ScheduledHandler(DispatcherHandler<CTX> dispatcher,
                     List<Scheduler> schedulers,
                     RequestTaskHook hook) {
        this.schedulers = schedulers;
        this.hook = hook;
        this.dispatcher = dispatcher;
        if (this.schedulers.size() == 1) {
            this.processor = (ctx, promise) ->
                    processByFixedScheduler(ctx, promise, this.schedulers.get(0));
        } else {
            // some of the route should be execute on io scheduler
            // some of the route should be execute on route.scheduler()

            // request(io) -> find route(io) ---> run on route.scheduler()
            this.processor = this::processBySpecifiedScheduler;
        }
    }

    void process(CTX context, CompletableFuture<Void> promise) {
        final HttpRequest request = context.request();
        if (LoggerUtils.logger().isDebugEnabled()) {
            LoggerUtils.logger().debug("Received request(url={}, method={})",
                    request.path(), request.method());
        }
        this.processor.accept(context, promise);
    }

    private void processByFixedScheduler(CTX ctx,
                                         CompletableFuture<Void> promise,
                                         Scheduler scheduler) {
        final RequestTask task = hook.onRequest(newRequestTask(ctx,
                promise,
                () -> {
                    final CompletableFuture<Route> route = route(ctx);
                    route.whenComplete((r, th) -> {
                        if (th != null) {
                            PromiseUtils.setFailure(promise, th);
                        } else {
                            if (r == null) {
                                if (!ctx.response().isCommitted()) {
                                    LoggerUtils.logger().warn("Request(url={}, method={}) hasn't been committed" +
                                            "after route failure handling");
                                    ctx.response().sendResult(HttpStatus.NOT_FOUND.code());
                                }
                                PromiseUtils.setSuccess(promise);
                            } else {
                                LoggerUtils.logger().debug("Mapping request(url={}, method={}) to {}",
                                        ctx.request().path(),
                                        ctx.request().method(), r);
                                dispatcher.service(ctx, promise, r);
                            }
                        }
                    });
                }));
        if (task != null) {
            scheduler.schedule(task);
        }
    }

    private void processBySpecifiedScheduler(CTX ctx,
                                             CompletableFuture<Void> promise) {
        final CompletableFuture<Route> route = route(ctx);
        route.whenComplete((r, th) -> {
            if (th != null) {
                PromiseUtils.setFailure(promise, th);
            } else {
                if (r == null) {
                    if (!ctx.response().isCommitted()) {
                        LoggerUtils.logger().warn("Request(url={}, method={}) hasn't been committed" +
                                "after route failure handling");
                        ctx.response().sendResult(HttpStatus.NOT_FOUND.code());
                    }
                    PromiseUtils.setSuccess(promise);
                } else {
                    LoggerUtils.logger().debug("Mapping request(url={}, method={}) to {}",
                            ctx.request().path(),
                            ctx.request().method(), r);

                    RequestTask task = hook.onRequest(newRequestTask(ctx,
                            promise,
                            () -> dispatcher.service(ctx, promise, r)));

                    if (task != null) {
                        r.scheduler().schedule(task);
                    }
                }
            }
        });
    }

    private CompletableFuture<Route> route(CTX context) {
        final Route route = dispatcher.route(context);
        // handle routed failure and return
        if (route == null) {
            return Futures.completedExceptionally(new RouteFailureException(DispatcherHandlerImpl.notFound(context)));
        }
        return Futures.completedFuture(route);
    }
}

