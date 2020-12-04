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
package esa.restlight.server.schedule;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.function.Consumer3;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.util.MediaType;
import esa.restlight.core.util.OrderedComparator;
import esa.restlight.server.bootstrap.DispatcherHandler;
import esa.restlight.server.config.ServerOptions;
import esa.restlight.server.handler.RestlightHandler;
import esa.restlight.server.route.Route;
import esa.restlight.server.route.predicate.RoutePredicate;
import esa.restlight.server.util.ErrorDetail;
import esa.restlight.server.util.LoggerUtils;
import esa.restlight.server.util.PromiseUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static esa.restlight.server.schedule.RequestTaskImpl.newRequestTask;

public class ScheduledRestlightHandler implements RestlightHandler {

    private final DispatcherHandler dispatcher;
    private final List<Scheduler> schedulers = new LinkedList<>();
    private final RequestTaskHook hook;
    private Consumer3<AsyncRequest, AsyncResponse, CompletableFuture<Void>> processor;
    private volatile long terminationTimeoutSeconds;

    public ScheduledRestlightHandler(ServerOptions options,
                                     DispatcherHandler dispatcher) {
        this(options, dispatcher, null);
    }

    public ScheduledRestlightHandler(ServerOptions options,
                                     DispatcherHandler dispatcher,
                                     List<RequestTaskHook> hooks) {
        Checks.checkNotNull(options, "RestlightOptions must not be null!");
        Checks.checkNotNull(dispatcher, "DispatcherHandler must not be null!");
        this.dispatcher = dispatcher;
        this.hook = hooks == null || hooks.isEmpty() ? t -> t : toRequestTaskHook(hooks);
        this.terminationTimeoutSeconds = options.getBizTerminationTimeoutSeconds();
    }

    @Override
    public synchronized void onStart() {
        final List<Route> routes = dispatcher.routes();
        final Set<Scheduler> tmp = new HashSet<>();
        for (Route route : routes) {
            Scheduler scheduler = route.scheduler();
            Checks.checkNotNull(scheduler);
            if (tmp.add(scheduler)) {
                this.schedulers.add(scheduler);
            }
        }

        if (this.schedulers.size() == 1) {
            this.processor = (req, res, promise) ->
                    processByFixedScheduler(req, res, promise, this.schedulers.get(0));
        } else {
            // some of the route should be execute on io scheduler
            // some of the route should be execute on route.scheduler()

            // request(io) -> find route(io) ---> run on route.scheduler()
            this.processor = this::processBySpecifiedScheduler;
        }
    }

    private void processByFixedScheduler(AsyncRequest req,
                                         AsyncResponse res,
                                         CompletableFuture<Void> promise,
                                         Scheduler scheduler) {
        final RequestTask task = hook.onRequest(newRequestTask(req,
                res,
                promise,
                () -> {
                    final Route route = routeOrNotFound(req, res, promise);
                    if (route != null) {
                        dispatcher.service(req, res, promise, route);
                    }
                }));
        if (task != null) {
            scheduler.schedule(task);
        }
    }

    private void processBySpecifiedScheduler(AsyncRequest req,
                                             AsyncResponse res,
                                             CompletableFuture<Void> promise) {
        final Route route = routeOrNotFound(req, res, promise);
        if (route != null) {
            final RequestTask task = hook.onRequest(newRequestTask(req,
                    res,
                    promise,
                    () -> dispatcher.service(req, res, promise, route)));

            if (task != null) {
                route.scheduler().schedule(task);
            }
        }
    }

    private Route routeOrNotFound(AsyncRequest req,
                                  AsyncResponse res,
                                  CompletableFuture<Void> promise) {
        final Route route = dispatcher.route(req, res);
        if (route == null) {
            notFound(req, res, promise);
            return null;
        }

        LoggerUtils.logger().debug("Mapping request(url={}, method={}) to {}",
                req.path(),
                req.method(),
                route);
        return route;
    }

    @Override
    public List<Scheduler> schedulers() {
        return this.schedulers;
    }

    @Override
    public CompletableFuture<Void> process(AsyncRequest request, AsyncResponse response) {
        if (LoggerUtils.logger().isDebugEnabled()) {
            LoggerUtils.logger().debug("Received request(url={}, method={})",
                    request.path(), request.method());
        }
        final CompletableFuture<Void> promise = new CompletableFuture<>();
        this.processor.accept(request, response, promise);
        return promise;
    }

    @Override
    public void onConnected(ChannelHandlerContext ctx) {
        // nothing to do
    }

    @Override
    public synchronized void shutdown() {
        try {
            dispatcher.shutdown();

            if (schedulers.isEmpty()) {
                return;
            }

            schedulers.forEach(e -> {
                if (e instanceof ExecutorScheduler) {
                    Executor executor = ((ExecutorScheduler) e).executor();
                    if (executor instanceof ThreadPoolExecutor) {
                        LoggerUtils.logger()
                                .info("Try to shutdown scheduler({}) with {} actively executing task(s)",
                                        e.name(), ((ThreadPoolExecutor) executor).getActiveCount());
                    }
                }
            });

        } catch (Exception e) {
            LoggerUtils.logger().error("Error while trying to shutdown Restlight server.", e);
        }

        if (schedulers.size() == 1) {
            doShutdown(schedulers.get(0));
        } else {
            final CountDownLatch latch = new CountDownLatch(schedulers.size());
            new Thread(() -> schedulers.forEach(scheduler -> {
                try {
                    doShutdown(scheduler);
                } finally {
                    latch.countDown();
                }
            }), "Scheduler-Shutdown").start();

            try {
                latch.await();
            } catch (InterruptedException e) {
                LoggerUtils.logger()
                        .error("Error occurred during waiting submitted biz task to finish.", e);
            }
        }
    }

    private void doShutdown(Scheduler scheduler) {
        try {
            if (scheduler instanceof ExecutorScheduler) {
                Executor executor = ((ExecutorScheduler) scheduler).executor();
                if (executor instanceof ExecutorService) {
                    ExecutorService es = (ExecutorService) executor;
                    if (es instanceof ThreadPoolExecutor) {
                        LoggerUtils.logger()
                                .info("Try to shutdown scheduler({}) with {} actively executing task(s)",
                                        scheduler.name(), ((ThreadPoolExecutor) es).getActiveCount());
                    }
                    es.shutdown();
                    try {
                        // Blocks until all tasks completes, or timeout occurs, or current thread is interrupted
                        es.awaitTermination(terminationTimeoutSeconds, TimeUnit.SECONDS);
                    } finally {
                        final List<RequestTask> tasks =
                                es.shutdownNow()
                                        .stream()
                                        .filter(task -> task instanceof RequestTask)
                                        .map(task -> (RequestTask) task)
                                        .collect(Collectors.toList());

                        if (tasks.isEmpty()) {
                            LoggerUtils.logger().info("Succeed to shutdown scheduler({})", scheduler.name());
                        } else {
                            dispatcher.handleUnfinishedWorks(tasks);
                            LoggerUtils.logger()
                                    .warn("Succeed to shutdown scheduler({}) with unfinished {} task(s)",
                                            scheduler.name(),
                                            tasks.size());
                        }
                    }
                } else {
                    scheduler.shutdown();
                }
            } else {
                scheduler.shutdown();
            }
        } catch (Throwable e) {
            LoggerUtils.logger()
                    .error("Failed to shutdown scheduler(" + scheduler.name() + ").", e);
        }
    }

    public void setTerminationTimeoutSeconds(long terminationTimeoutSeconds) {
        this.terminationTimeoutSeconds = terminationTimeoutSeconds;
    }

    /**
     * No route found -> set appropriate HTTP response status.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param promise  promise
     */
    static void notFound(AsyncRequest request,
                         AsyncResponse response,
                         CompletableFuture<Void> promise) {
        LoggerUtils.logger().warn("No mapping for request(url={}, method={})",
                request.path(), request.method());

        HttpResponseStatus status = request.removeUncheckedAttribute(RoutePredicate.MATCH_STATUS);
        if (status == null) {
            status = HttpResponseStatus.NOT_FOUND;
        }

        response.setHeader(HttpHeaderNames.CONTENT_TYPE, MediaType.TEXT_PLAIN.value());
        response.sendResult(status.code(),
                ErrorDetail.buildErrorMsg(request.path(), StringUtils.empty(),
                        status.reasonPhrase(), status.code()));

        PromiseUtils.setSuccess(promise);
    }

    private static RequestTaskHook toRequestTaskHook(List<RequestTaskHook> hooks) {
        final RequestTaskHook[] arr = hooks.toArray(new RequestTaskHook[0]);
        if (arr.length == 1) {
            // unwrap loop
            final RequestTaskHook rth = arr[0];
            return task -> {
                RequestTask maybeWrapped = rth.onRequest(task);
                if (maybeWrapped == null) {
                    handleUncommitted(task);
                    return null;
                } else {
                    return maybeWrapped;
                }
            };
        } else {
            OrderedComparator.sort(arr);
            return task -> {
                RequestTask maybeWrapped = task;
                for (RequestTaskHook hook : arr) {
                    maybeWrapped = hook.onRequest(maybeWrapped);
                    if (maybeWrapped == null) {
                        handleUncommitted(task);
                        return null;
                    }
                }
                return maybeWrapped;
            };
        }
    }

    private static void handleUncommitted(RequestTask task) {
        if (!task.response().isCommitted()) {
            if (LoggerUtils.logger().isDebugEnabled()) {
                LoggerUtils.logger()
                        .debug("{} rejected by RequestTaskHook, but response haven't been committed", task);
            }
            task.response().sendResult();
        }
        if (!task.promise().isDone()) {
            PromiseUtils.setSuccess(task.promise());
        }
    }
}
