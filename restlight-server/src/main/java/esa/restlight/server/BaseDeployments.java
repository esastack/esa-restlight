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
package esa.restlight.server;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.annotation.Beta;
import esa.commons.spi.SpiLoader;
import esa.restlight.core.util.Constants;
import esa.restlight.core.util.OrderedComparator;
import esa.restlight.server.bootstrap.DispatcherExceptionHandler;
import esa.restlight.server.bootstrap.DispatcherHandler;
import esa.restlight.server.bootstrap.RestlightThreadFactory;
import esa.restlight.server.config.BizThreadsOptions;
import esa.restlight.server.config.FailFastOptions;
import esa.restlight.server.config.ServerOptions;
import esa.restlight.server.handler.RestlightHandler;
import esa.restlight.server.route.Route;
import esa.restlight.server.route.RouteRegistry;
import esa.restlight.server.route.impl.CachedRouteRegistry;
import esa.restlight.server.route.impl.SimpleRouteRegistry;
import esa.restlight.server.schedule.ExecutorScheduler;
import esa.restlight.server.schedule.FailFastExecutorScheduler;
import esa.restlight.server.schedule.FailFastScheduler;
import esa.restlight.server.schedule.RequestTask;
import esa.restlight.server.schedule.RequestTaskHook;
import esa.restlight.server.schedule.ScheduledRestlightHandler;
import esa.restlight.server.schedule.Scheduler;
import esa.restlight.server.schedule.Schedulers;
import esa.restlight.server.spi.DispatcherExceptionHandlerFactory;
import esa.restlight.server.spi.DispatcherHandlerFactory;
import esa.restlight.server.spi.RequestTaskHookFactory;
import esa.restlight.server.util.LoggerUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BaseDeployments<R extends BaseRestlightServer<R, D, O>, D extends BaseDeployments<R, D, O>,
        O extends ServerOptions> {

    private static final Class<? extends RejectedExecutionHandler> JDK_DEFAULT_REJECT_HANDLER;

    /**
     * Hold the reference of {@link R}
     */
    protected final R restlight;
    private final List<Route> routes = new LinkedList<>();
    private final List<RequestTaskHookFactory> requestTaskHooks = new LinkedList<>();
    private final ServerDeployContext<O> ctx;
    private RestlightHandler handler;

    static {
        final ThreadPoolExecutor e = new ThreadPoolExecutor(0,
                1,
                0L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new RestlightThreadFactory("useless"));
        JDK_DEFAULT_REJECT_HANDLER = e.getRejectedExecutionHandler().getClass();
        e.shutdownNow();
    }

    protected BaseDeployments(R restlight, O options) {
        this.restlight = restlight;
        this.ctx = newContext(options);
        configEmbeddedSchedulers(options);
    }

    private void configEmbeddedSchedulers(O options) {
        this.addScheduler(Schedulers.io());
        BizThreadsOptions bizOptions = options.getBizThreads();
        final BlockingQueue<Runnable> workQueue = bizOptions.getBlockingQueueLength() > 0
                ? new LinkedBlockingQueue<>(bizOptions.getBlockingQueueLength())
                : new SynchronousQueue<>();
        final ThreadPoolExecutor biz = new ThreadPoolExecutor(bizOptions.getCore(),
                bizOptions.getMax(),
                bizOptions.getKeepAliveTimeSeconds(),
                TimeUnit.SECONDS,
                workQueue,
                new RestlightThreadFactory("Restlight-Biz"));
        this.addScheduler(Schedulers.fromExecutor(Schedulers.BIZ, biz));
    }

    protected ServerDeployContext<O> newContext(O options) {
        return new ServerDeployContextImpl<>(restlight.name(), options);
    }

    /**
     * Adds a {@link Route} to shouldHandle request of Restlight.
     *
     * @param route route
     *
     * @return this
     */
    public D addRoute(Route route) {
        checkImmutable();
        Checks.checkNotNull(route, "route");
        if (route.scheduler() == null) {
            String defaultScheduler = ctx.options().getScheduling().getDefaultScheduler();
            if (StringUtils.isNotEmpty(defaultScheduler)) {
                Scheduler scheduler = ctx().schedulers().get(defaultScheduler);
                if (scheduler == null) {
                    throw new IllegalStateException("Could not find any scheduler named '"
                            + defaultScheduler + "'");
                }
                route = Route.route(route)
                        .schedule(scheduler);
            } else {
                route = Route.route(route)
                        .schedule(ctx().schedulers().get(Schedulers.BIZ));
            }
        } else if (Schedulers.isBiz(route.scheduler())) {
            route = Route.route(route)
                    .schedule(ctx().schedulers().get(Schedulers.BIZ));
        }
        this.routes.add(route);
        return self();
    }

    /**
     * Adds {@link Route}s to shouldHandle request of Restlight.
     *
     * @param routes routes
     *
     * @return this
     */
    public D addRoutes(Collection<? extends Route> routes) {
        checkImmutable();
        if (routes != null && !routes.isEmpty()) {
            routes.forEach(this::addRoute);
        }
        return self();
    }

    public D addScheduler(Scheduler scheduler) {
        checkImmutable();
        Checks.checkNotNull(scheduler, "scheduler");
        Scheduler configured = configExecutor(scheduler);
        ctx().mutableSchedulers().putIfAbsent(configured.name(), configured);
        return self();
    }

    public D addSchedulers(Collection<? extends Scheduler> schedulers) {
        checkImmutable();
        if (schedulers != null && !schedulers.isEmpty()) {
            schedulers.forEach(this::addScheduler);
        }
        return self();
    }

    @Beta
    public D addRequestTaskHook(RequestTaskHook hook) {
        return addRequestTaskHook((RequestTaskHookFactory) ctx -> Optional.of(hook));
    }

    @Beta
    public D addRequestTaskHook(RequestTaskHookFactory hook) {
        checkImmutable();
        Checks.checkNotNull(hook, "hook");
        this.requestTaskHooks.add(hook);
        return self();
    }

    @Beta
    public D addRequestTaskHooks(Collection<? extends RequestTaskHookFactory> hooks) {
        checkImmutable();
        if (hooks != null && !hooks.isEmpty()) {
            hooks.forEach(this::addRequestTaskHook);
        }
        return self();
    }

    private Scheduler configExecutor(Scheduler scheduler) {
        if (scheduler instanceof ExecutorScheduler) {
            Executor e = ((ExecutorScheduler) scheduler).executor();
            if (e instanceof ThreadPoolExecutor) {
                final ThreadPoolExecutor pool = (ThreadPoolExecutor) e;
                final RejectedExecutionHandler rejectHandler = pool.getRejectedExecutionHandler();

                if (!rejectHandler.getClass().equals(JDK_DEFAULT_REJECT_HANDLER)) {
                    LoggerUtils.logger()
                            .warn("Custom RejectedExecutionHandler is not allowed in scheduler({}): '{}'",
                                    scheduler.name(),
                                    rejectHandler.getClass().getName());
                }
                // replace reject handler to restlight embedded BizRejectedHandler whatever what reject handler it is.
                pool.setRejectedExecutionHandler(new BizRejectedHandler(scheduler.name()));
            }
        }

        // config by failFast options
        FailFastOptions failFastOption;
        if (scheduler instanceof FailFastScheduler
                || (failFastOption = ctx().options().getScheduling().getFailFastOptions()
                .get(scheduler.name())) == null) {
            return scheduler;
        } else {
            return scheduler instanceof ExecutorScheduler
                    ? new FailFastExecutorScheduler((ExecutorScheduler) scheduler, failFastOption)
                    : new FailFastScheduler(scheduler, failFastOption);
        }
    }

    public ServerDeployContext<O> deployContext() {
        return ctx();
    }

    /**
     * @return current Restlight
     */
    public R server() {
        return restlight;
    }

    protected ServerDeployContextImpl<O> ctx() {
        return (ServerDeployContextImpl<O>) ctx;
    }

    RestlightHandler applyDeployments() {
        this.beforeApplyDeployments();
        return getRestlightHandler();
    }

    protected void beforeApplyDeployments() {
    }

    protected RestlightHandler getRestlightHandler() {
        if (handler == null) {
            handler = doGetRestlightHandler();
        }
        return handler;
    }

    protected RestlightHandler doGetRestlightHandler() {

        final RouteRegistry routeRegistry = getRouteRegistry();
        // register routes
        registerRoutes(routeRegistry);
        ctx().setRegistry(routeRegistry.toReadOnly());

        // load DispatcherExceptionHandler by spi
        List<DispatcherExceptionHandlerFactory> exHandlerFactories =
                SpiLoader.cached(DispatcherExceptionHandlerFactory.class)
                .getByFeature(restlight.name(),
                        true,
                        Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                        false);
        Checks.checkNotEmptyState(exHandlerFactories, "exHandlerFactories");
        final List<DispatcherExceptionHandler> exceptionHandlers = new ArrayList<>(3);
        exHandlerFactories.forEach(factory -> factory.exceptionHandler(ctx).ifPresent(exceptionHandlers::add));
        OrderedComparator.sort(exceptionHandlers);
        ctx().setDispatcherExceptionHandlers(exceptionHandlers);

        // load DispatcherHandler by spi
        List<DispatcherHandlerFactory> handlerFactories = SpiLoader.cached(DispatcherHandlerFactory.class)
                .getByFeature(restlight.name(),
                        true,
                        Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                        false);
        Checks.checkNotEmptyState(handlerFactories, "handlerFactories");
        final DispatcherHandler dispatcherHandler =
                handlerFactories.iterator().next().dispatcherHandler(ctx());
        ctx().setDispatcherHandler(dispatcherHandler);

        // load RequestTaskHookFactory by spi
        SpiLoader.cached(RequestTaskHookFactory.class)
                .getByGroup(restlight.name(), true)
                .forEach(this::addRequestTaskHook);
        // load RequestTaskHook by spi
        SpiLoader.cached(RequestTaskHook.class)
                .getByGroup(restlight.name(), true)
                .forEach(this::addRequestTaskHook);

        return new ScheduledRestlightHandler(ctx.options(),
                dispatcherHandler,
                requestTaskHooks.stream()
                        .map(f -> f.hook(ctx()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()));
    }

    private RouteRegistry getRouteRegistry() {
        if (ctx.options().getRoute().isUseCachedRouting() && routes.size() >= 10) {
            return new CachedRouteRegistry(
                    ctx.options().getRoute().getComputeRate());
        } else {
            return new SimpleRouteRegistry();
        }
    }

    /**
     * Registers routes into the given {@link RouteRegistry}.
     *
     * @param registry registry
     */
    protected void registerRoutes(RouteRegistry registry) {
        routes.forEach(registry::registerRoute);
    }

    protected void checkImmutable() {
        restlight.checkImmutable();
    }

    @SuppressWarnings("unchecked")
    protected D self() {
        return (D) this;
    }

    public static class Impl extends BaseDeployments<Restlite, Impl, ServerOptions> {
        Impl(Restlite restlight, ServerOptions options) {
            super(restlight, options);
        }
    }

    /**
     * Custom task rejected route: write 503 to response
     */
    class BizRejectedHandler implements RejectedExecutionHandler {

        private final String name;

        private BizRejectedHandler(String name) {
            Checks.checkNotEmptyArg(name, "name");
            this.name = name;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            final Optional<DispatcherHandler> h;
            if (r instanceof RequestTask && (h = ctx().dispatcherHandler()).isPresent()) {
                String reason;
                if (executor.isShutdown()) {
                    reason =
                            "Scheduler(" + name + ") has been shutdown";
                } else {
                    try {
                        reason = "Rejected by scheduler(" + name + "), size of queue: " +
                                executor.getQueue().size();
                    } catch (Throwable ignored) {
                        reason = "Rejected by scheduler(" + name + ")";
                    }
                }
                h.get().handleRejectedWork((RequestTask) r, reason);
            }
        }
    }
}
