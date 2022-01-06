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
package io.esastack.restlight.server;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.annotation.Beta;
import esa.commons.annotation.Internal;
import esa.commons.spi.SpiLoader;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.core.util.OrderedComparator;
import io.esastack.restlight.server.bootstrap.DispatcherHandler;
import io.esastack.restlight.server.bootstrap.DispatcherHandlerImpl;
import io.esastack.restlight.server.bootstrap.IExceptionHandler;
import io.esastack.restlight.server.bootstrap.RestlightThreadFactory;
import io.esastack.restlight.server.config.BizThreadsOptions;
import io.esastack.restlight.server.config.ServerOptions;
import io.esastack.restlight.server.config.TimeoutOptions;
import io.esastack.restlight.server.handler.ConnectionHandler;
import io.esastack.restlight.server.handler.DisConnectionHandler;
import io.esastack.restlight.server.handler.Filter;
import io.esastack.restlight.server.handler.RestlightHandler;
import io.esastack.restlight.server.route.Route;
import io.esastack.restlight.server.route.RouteRegistry;
import io.esastack.restlight.server.route.impl.AbstractRouteRegistry;
import io.esastack.restlight.server.route.impl.CachedRouteRegistry;
import io.esastack.restlight.server.route.impl.RoutableRegistry;
import io.esastack.restlight.server.route.impl.SimpleRouteRegistry;
import io.esastack.restlight.server.schedule.ExecutorScheduler;
import io.esastack.restlight.server.schedule.RequestTask;
import io.esastack.restlight.server.schedule.RequestTaskHook;
import io.esastack.restlight.server.schedule.ScheduledRestlightHandler;
import io.esastack.restlight.server.schedule.Scheduler;
import io.esastack.restlight.server.schedule.Schedulers;
import io.esastack.restlight.server.spi.ConnectionHandlerFactory;
import io.esastack.restlight.server.spi.DisConnectionHandlerFactory;
import io.esastack.restlight.server.spi.ExceptionHandlerFactory;
import io.esastack.restlight.server.spi.FilterFactory;
import io.esastack.restlight.server.spi.RequestTaskHookFactory;
import io.esastack.restlight.server.spi.RouteRegistryAware;
import io.esastack.restlight.server.spi.RouteRegistryAwareFactory;
import io.esastack.restlight.server.util.LoggerUtils;

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

public abstract class BaseDeployments<R extends BaseRestlightServer<R, D, O>, D extends
        BaseDeployments<R, D, O>, O extends ServerOptions> {

    private static final Class<? extends RejectedExecutionHandler> JDK_DEFAULT_REJECT_HANDLER;

    /**
     * Hold the reference of {@link R}
     */
    protected final R restlight;
    private final List<Route> routes = new LinkedList<>();
    private final List<FilterFactory> filters = new LinkedList<>();
    private final List<ExceptionHandlerFactory> exceptionHandlerFactories = new LinkedList<>();
    private final List<ConnectionHandlerFactory> connectionHandlers = new LinkedList<>();
    private final List<DisConnectionHandlerFactory> disConnectionHandlers = new LinkedList<>();
    private final List<RequestTaskHookFactory> requestTaskHooks = new LinkedList<>();
    private final List<RouteRegistryAwareFactory> registryAwareness = new LinkedList<>();
    private final ServerDeployContext<O> deployContext;

    IExceptionHandler[] exceptionHandlers;
    private DispatcherHandler dispatcher;
    private RestlightHandler handler;

    static {
        Class<? extends RejectedExecutionHandler> defaultHandlerClass;
        try {
            defaultHandlerClass = (Class<? extends RejectedExecutionHandler>)
                    ThreadPoolExecutor.class.getDeclaredField("defaultHandler").getType();
        } catch (NoSuchFieldException e) {
            LoggerUtils.logger().debug("Could not find the field named 'defaultHandler'" +
                    " in java.util.concurrent.ThreadPoolExecutor", e);
            final ThreadPoolExecutor executor = new ThreadPoolExecutor(0,
                    1,
                    0L,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(),
                    new RestlightThreadFactory("useless"));
            defaultHandlerClass = executor.getRejectedExecutionHandler().getClass();
            executor.shutdownNow();
        }
        JDK_DEFAULT_REJECT_HANDLER = defaultHandlerClass;
    }

    protected BaseDeployments(R restlight, O options) {
        this.restlight = restlight;
        this.deployContext = newDeployContext(options);
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

    protected ServerDeployContext<O> newDeployContext(O options) {
        return new ServerDeployContextImpl<>(restlight.name(), options);
    }

    /**
     * Adds a {@link Route} to handle request of Restlight.
     *
     * @param route route
     * @return this
     */
    public D addRoute(Route route) {
        checkImmutable();
        Checks.checkNotNull(route, "route");
        this.routes.add(route);
        return self();
    }

    /**
     * Add {@link Route}s to handle request of Restlight.
     *
     * @param routes routes
     * @return this
     */
    public D addRoutes(Collection<? extends Route> routes) {
        checkImmutable();
        if (routes != null) {
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

    public D addConnectionHandler(ConnectionHandler handler) {
        checkImmutable();
        Checks.checkNotNull(handler, "handler");
        return addConnectionHandler(ctx -> {
            return Optional.of(handler);
        });
    }

    public D addConnectionHandler(ConnectionHandlerFactory handler) {
        checkImmutable();
        Checks.checkNotNull(handler, "handler");
        return addConnectionHandlers(Collections.singletonList(handler));
    }

    public D addConnectionHandlers(Collection<? extends ConnectionHandlerFactory> handlers) {
        checkImmutable();
        if (handlers != null && !handlers.isEmpty()) {
            this.connectionHandlers.addAll(handlers);
        }
        return self();
    }

    public D addDisConnectionHandler(DisConnectionHandler handler) {
        checkImmutable();
        Checks.checkNotNull(handler, "handler");
        return addDisConnectionHandler(ctx -> {
            return Optional.of(handler);
        });
    }

    public D addDisConnectionHandler(DisConnectionHandlerFactory handler) {
        checkImmutable();
        Checks.checkNotNull(handler, "handler");
        return addDisConnectionHandlers(Collections.singletonList(handler));
    }

    public D addDisConnectionHandlers(Collection<? extends DisConnectionHandlerFactory> handlers) {
        checkImmutable();
        if (handlers != null && !handlers.isEmpty()) {
            this.disConnectionHandlers.addAll(handlers);
        }
        return self();
    }

    public D addFilter(Filter filter) {
        checkImmutable();
        Checks.checkNotNull(filter, "filter");
        addFilter(ctx -> Optional.of(filter));
        return self();
    }

    public D addFilter(FilterFactory filter) {
        checkImmutable();
        Checks.checkNotNull(filter, "filter");
        addFilters(Collections.singletonList(filter));
        return self();
    }

    public D addFilters(Collection<? extends FilterFactory> filters) {
        checkImmutable();
        if (filters != null && !filters.isEmpty()) {
            this.filters.addAll(filters);
        }
        return self();
    }

    @Internal
    public D addExceptionHandler(IExceptionHandler handler) {
        checkImmutable();
        Checks.checkNotNull(handler, "handler");
        this.exceptionHandlerFactories.add(ctx -> Optional.of(handler));
        return self();
    }

    @Internal
    public D addExceptionHandlers(Collection<? extends IExceptionHandler> handlers) {
        checkImmutable();
        if (handlers != null && !handlers.isEmpty()) {
            handlers.forEach(this::addExceptionHandler);
        }
        return self();
    }

    @Internal
    public D addExceptionHandler(ExceptionHandlerFactory handler) {
        checkImmutable();
        Checks.checkNotNull(handler, "handler");
        this.exceptionHandlerFactories.add(handler);
        return self();
    }

    public D addRouteRegistryAware(RouteRegistryAware aware) {
        checkImmutable();
        Checks.checkNotNull(aware, "aware");
        return addRouteRegistryAware((RouteRegistryAwareFactory) deployContext -> Optional.of(aware));
    }

    public D addRouteRegistryAware(RouteRegistryAwareFactory aware) {
        checkImmutable();
        Checks.checkNotNull(aware, "aware");
        return addRouteRegistryAwareness(Collections.singletonList(aware));
    }

    public D addRouteRegistryAwareness(Collection<? extends RouteRegistryAwareFactory> awareness) {
        checkImmutable();
        if (awareness != null && !awareness.isEmpty()) {
            this.registryAwareness.addAll(awareness);
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

        // config by timeout options
        TimeoutOptions timeoutOptions = ctx().options().getScheduling().getTimeout().get(scheduler.name());
        return Schedulers.wrapped(scheduler, timeoutOptions);
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
        return (ServerDeployContextImpl<O>) deployContext;
    }

    /**
     * Obtains all {@link Filter}s.
     *
     * @return filters
     */
    List<Filter> filters() {
        return filters.stream()
                .map(factory -> factory.filter(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
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
        final RoutableRegistry routeRegistry = new RoutableRegistry(ctx(), getRouteRegistry());
        ctx().setRegistry(routeRegistry);

        // register routes
        registerRoutes(routeRegistry);
        ctx().setDispatcherHandler(dispatcher);

        // load RouteRegistryAware by spi
        this.registryAwareness.addAll(SpiLoader.cached(RouteRegistryAware.class).getAll()
                .stream().map(aware -> (RouteRegistryAwareFactory) deployContext -> Optional.of(aware))
                .collect(Collectors.toList()));
        this.registryAwareness.addAll(SpiLoader.cached(RouteRegistryAwareFactory.class).getAll());
        this.registryAwareness.stream().map(factory -> factory.createAware(deployContext))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(aware -> aware.setRegistry(routeRegistry));

        // init ExceptionHandlerChain
        final IExceptionHandler[] iExceptionHandlers = getExceptionHandlers();
        this.exceptionHandlers = iExceptionHandlers;

        // init DispatcherHandler
        this.dispatcher = new DispatcherHandlerImpl(routeRegistry, iExceptionHandlers);

        // load RequestTaskHookFactory by spi
        SpiLoader.cached(RequestTaskHookFactory.class)
                .getByGroup(restlight.name(), true)
                .forEach(this::addRequestTaskHook);
        // load RequestTaskHook by spi
        SpiLoader.cached(RequestTaskHook.class)
                .getByGroup(restlight.name(), true)
                .forEach(this::addRequestTaskHook);

        // load and add ConnectionHandlerFactory by spi
        SpiLoader.cached(ConnectionHandlerFactory.class)
                .getByGroup(restlight.name(), true)
                .forEach(factory -> factory.handler(ctx()).ifPresent(this::addConnectionHandler));

        // load and add DisConnectionHandlerFactory by spi
        SpiLoader.cached(DisConnectionHandlerFactory.class)
                .getByGroup(restlight.name(), true)
                .forEach(factory -> factory.handler(ctx()).ifPresent(this::addDisConnectionHandler));

        return new ScheduledRestlightHandler(deployContext.options(),
                dispatcher,
                requestTaskHooks.stream()
                        .map(f -> f.hook(ctx()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()),
                connectionHandlers.stream()
                        .map(factory -> factory.handler(deployContext))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()),
                disConnectionHandlers.stream()
                        .map(factory -> factory.handler(deployContext))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList())
        );
    }

    private AbstractRouteRegistry getRouteRegistry() {
        if (deployContext.options().getRoute().isUseCachedRouting() && routes.size() >= 10) {
            return new CachedRouteRegistry(deployContext.options().getRoute().getComputeRate());
        } else {
            return new SimpleRouteRegistry();
        }
    }

    private IExceptionHandler[] getExceptionHandlers() {
        this.exceptionHandlerFactories.addAll(SpiLoader.cached(ExceptionHandlerFactory.class)
                .getByFeature(restlight.name(),
                        true,
                        Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                        false));
        IExceptionHandler[] exImpls = this.exceptionHandlerFactories
                .stream().map(factory -> factory.handler(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toArray(IExceptionHandler[]::new);
        OrderedComparator.sort(exImpls);
        return exImpls;
    }

    /**
     * Registers routes into the given {@link RouteRegistry}.
     *
     * @param registry registry
     */
    protected void registerRoutes(RouteRegistry registry) {
        routes.forEach(registry::register);
    }

    protected void checkImmutable() {
        restlight.checkImmutable();
    }

    @SuppressWarnings("unchecked")
    protected D self() {
        return (D) this;
    }

    /**
     * Custom task rejected route: write 429 to response
     */
    class BizRejectedHandler implements RejectedExecutionHandler {

        private final String name;

        private BizRejectedHandler(String name) {
            Checks.checkNotEmptyArg(name, "name");
            this.name = name;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            final DispatcherHandler h;
            if (r instanceof RequestTask && ((h = dispatcher) != null)) {
                String reason;
                if (executor.isShutdown()) {
                    reason = "Scheduler(" + name + ") has been shutdown";
                } else {
                    try {
                        reason = "Rejected by scheduler(" + name + "), size of queue: " + executor.getQueue().size();
                    } catch (Throwable ignored) {
                        reason = "Rejected by scheduler(" + name + ")";
                    }
                }
                h.handleRejectedWork((RequestTask) r, reason);
            }
        }
    }
}
