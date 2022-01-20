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
package io.esastack.restlight.core;

import esa.commons.Checks;
import esa.commons.ClassUtils;
import esa.commons.ObjectUtils;
import esa.commons.StringUtils;
import esa.commons.annotation.Beta;
import esa.commons.annotation.Internal;
import esa.commons.spi.SpiLoader;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.configure.ConfigurableDeployments;
import io.esastack.restlight.core.configure.DefaultHandlerRegistry;
import io.esastack.restlight.core.configure.DeploymentsConfigure;
import io.esastack.restlight.core.configure.ExtensionsHandler;
import io.esastack.restlight.core.configure.HandlerConfigure;
import io.esastack.restlight.core.configure.HandlerRegistry;
import io.esastack.restlight.core.configure.HandlerRegistryAware;
import io.esastack.restlight.core.configure.HandlersImpl;
import io.esastack.restlight.core.configure.MiniConfigurableDeployments;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.HandlerMappingProvider;
import io.esastack.restlight.core.handler.RouteFilterAdapter;
import io.esastack.restlight.core.handler.impl.HandlerAdvicesFactoryImpl;
import io.esastack.restlight.core.handler.impl.HandlerContexts;
import io.esastack.restlight.core.handler.locate.HandlerValueResolverLocator;
import io.esastack.restlight.core.handler.locate.MappingLocator;
import io.esastack.restlight.core.handler.locate.RouteMethodLocator;
import io.esastack.restlight.core.interceptor.HandlerInterceptor;
import io.esastack.restlight.core.interceptor.Interceptor;
import io.esastack.restlight.core.interceptor.InterceptorFactory;
import io.esastack.restlight.core.interceptor.MappingInterceptor;
import io.esastack.restlight.core.interceptor.RouteInterceptor;
import io.esastack.restlight.core.resolver.ContextResolverAdapter;
import io.esastack.restlight.core.resolver.ContextResolverFactory;
import io.esastack.restlight.core.resolver.ExceptionResolver;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.HandlerResolverFactoryImpl;
import io.esastack.restlight.core.resolver.ParamResolverAdapter;
import io.esastack.restlight.core.resolver.ParamResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.ParamResolverAdviceFactory;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdapter;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.RequestEntityResolverFactory;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdapter;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.ResponseEntityResolverFactory;
import io.esastack.restlight.core.resolver.StringConverterAdapter;
import io.esastack.restlight.core.resolver.StringConverterFactory;
import io.esastack.restlight.core.resolver.exception.DefaultExceptionMapper;
import io.esastack.restlight.core.resolver.exception.DefaultExceptionResolverFactory;
import io.esastack.restlight.core.resolver.exception.ExceptionResolverFactory;
import io.esastack.restlight.core.serialize.HttpBodySerializer;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.core.spi.ContextResolverProvider;
import io.esastack.restlight.core.spi.DefaultSerializerFactory;
import io.esastack.restlight.core.spi.ExceptionResolverFactoryProvider;
import io.esastack.restlight.core.spi.ExtensionsHandlerFactory;
import io.esastack.restlight.core.spi.FutureTransferFactory;
import io.esastack.restlight.core.spi.HandlerAdviceFactory;
import io.esastack.restlight.core.spi.HandlerFactoryProvider;
import io.esastack.restlight.core.spi.HandlerRegistryAwareFactory;
import io.esastack.restlight.core.spi.ParamResolverAdviceProvider;
import io.esastack.restlight.core.spi.ParamResolverProvider;
import io.esastack.restlight.core.spi.RequestEntityResolverAdviceProvider;
import io.esastack.restlight.core.spi.RequestEntityResolverProvider;
import io.esastack.restlight.core.spi.ResponseEntityResolverAdviceProvider;
import io.esastack.restlight.core.spi.ResponseEntityResolverProvider;
import io.esastack.restlight.core.spi.RouteFilterFactory;
import io.esastack.restlight.core.spi.StringConverterProvider;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.core.util.OrderedComparator;
import io.esastack.restlight.core.util.RouteUtils;
import io.esastack.restlight.server.bootstrap.DispatcherHandler;
import io.esastack.restlight.server.bootstrap.DispatcherHandlerImpl;
import io.esastack.restlight.server.bootstrap.IExceptionHandler;
import io.esastack.restlight.server.bootstrap.RestlightThreadFactory;
import io.esastack.restlight.server.config.BizThreadsOptions;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementation for a Restlight server bootstrap and returns a {@link RestlightHandler} for
 * {@link AbstractRestlight}.
 */
public abstract class Deployments<R extends AbstractRestlight<R, D>, D extends
        Deployments<R, D>> {

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
    private final DeployContext deployContext;

    private final List<HandlerMappingProvider> mappingProviders = new LinkedList<>();
    private final List<Object> singletonControllers = new LinkedList<>();
    private final List<Class<?>> prototypeControllers = new LinkedList<>();
    private final List<Object> extensions = new LinkedList<>();
    private final List<Object> advices = new LinkedList<>();
    private final List<HandlerConfigure> handlerConfigures = new LinkedList<>();

    private final List<RouteFilterFactory> routeFilters = new LinkedList<>();
    private final List<StringConverterFactory> stringConverters = new LinkedList<>();
    private final List<ParamResolverFactory> paramResolvers = new LinkedList<>();
    private final List<ParamResolverAdviceFactory> paramResolverAdvices = new LinkedList<>();
    private final List<ContextResolverFactory> contextResolvers = new LinkedList<>();
    private final List<RequestEntityResolverFactory> requestEntityResolvers = new LinkedList<>();
    private final List<RequestEntityResolverAdviceFactory> requestEntityResolverAdvices = new LinkedList<>();
    private final List<ResponseEntityResolverFactory> responseEntityResolvers = new LinkedList<>();
    private final List<ResponseEntityResolverAdviceFactory> responseEntityResolverAdvices = new LinkedList<>();
    private final List<HttpRequestSerializer> rxSerializers = new LinkedList<>();
    private final List<HttpResponseSerializer> txSerializers = new LinkedList<>();
    private final List<InterceptorFactory> interceptors = new LinkedList<>();
    private final Map<Class<? extends Throwable>, ExceptionResolver<Throwable>> exceptionResolvers
            = new LinkedHashMap<>();
    private final List<HandlerRegistryAwareFactory> handlerAwareness = new LinkedList<>();

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

    protected Deployments(R restlight, RestlightOptions options) {
        this.restlight = restlight;
        this.deployContext = newDeployContext(options);
        configEmbeddedSchedulers(options);
    }

    private void configEmbeddedSchedulers(RestlightOptions options) {
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

    protected DeployContext newDeployContext(RestlightOptions options) {
        return new DeployContextImpl(restlight.name(), options);
    }

    public DeployContext deployContext() {
        return ctx();
    }

    public D addRouteFilter(RouteFilterAdapter filter) {
        checkImmutable();
        Checks.checkNotNull(filter, "filter");
        return addRouteFilter(RouteFilterFactory.singleton(filter));
    }

    public D addRouteFilter(RouteFilterFactory filter) {
        checkImmutable();
        Checks.checkNotNull(filter, "filter");
        this.routeFilters.add(filter);
        return self();
    }

    public D addRouteFilters(Collection<? extends RouteFilterFactory> filters) {
        checkImmutable();
        if (filters != null && !filters.isEmpty()) {
            filters.forEach(this::addRouteFilter);
        }
        return self();
    }

    public D addExtension(Object extension) {
        checkImmutable();
        Checks.checkNotNull(extension, "extension");
        return this.addExtensions(Collections.singleton(extension));
    }

    public D addExtensions(Collection<Object> extensions) {
        checkImmutable();
        if (extensions != null && !extensions.isEmpty()) {
            this.extensions.addAll(extensions);
        }
        return self();
    }

    public D addHandlerConfigure(HandlerConfigure handlerConfigure) {
        checkImmutable();
        Checks.checkNotNull(handlerConfigure, "handlerConfigure");
        this.handlerConfigures.add(handlerConfigure);
        return self();
    }

    public D addHandlerConfigures(Collection<? extends HandlerConfigure> handlerConfigures) {
        checkImmutable();
        if (handlerConfigures != null && !handlerConfigures.isEmpty()) {
            this.handlerConfigures.addAll(handlerConfigures);
        }
        return self();
    }

    /**
     * Adds {@link HandlerMapping}
     */
    public D addHandlerMapping(HandlerMapping mapping) {
        checkImmutable();
        Checks.checkNotNull(mapping, "mapping");
        return this.addHandlerMappingProvider(ctx -> Collections.singletonList(mapping));
    }

    /**
     * Adds {@link HandlerMapping}
     */
    public D addHandlerMappings(Collection<? extends HandlerMapping> mappings) {
        checkImmutable();
        if (mappings != null && !mappings.isEmpty()) {
            return this.addHandlerMappingProvider(ctx -> new HashSet<>(mappings));
        }
        return self();
    }

    /**
     * Adds {@link HandlerMappingProvider} and {@link HandlerMapping}s returned by this provider will be registered in
     * the {@link RouteRegistry}
     */
    public D addHandlerMappingProvider(HandlerMappingProvider provider) {
        checkImmutable();
        Checks.checkNotNull(provider, "provider");
        this.mappingProviders.add(provider);
        return self();
    }

    /**
     * Adds {@link HandlerMappingProvider}s and {@link HandlerMapping}s returned by these providers will be registered
     * in the {@link RouteRegistry}
     *
     * @param providers providers
     * @return this deployments
     */
    public D addHandlerMappingProviders(Collection<? extends HandlerMappingProvider> providers) {
        checkImmutable();
        if (providers != null && !providers.isEmpty()) {
            this.mappingProviders.addAll(providers);
        }
        return self();
    }

    /**
     * Adds a controller bean which will be registered in the {@link RouteRegistry}.
     *
     * @param bean an {@link Object} instances.
     * @return this deployments
     */
    public D addController(Object bean) {
        checkImmutable();
        Checks.checkNotNull(bean, "beans");
        checkDuplicateController(bean);
        this.singletonControllers.add(ObjectUtils.instantiateBeanIfNecessary(bean));
        return self();
    }

    /**
     * Adds controller beans which will be registered in the {@link RouteRegistry}
     *
     * @param beans {@link Object} instances.
     * @return this deployments
     */
    public D addControllers(Collection<?> beans) {
        checkImmutable();
        if (beans != null && !beans.isEmpty()) {
            beans.forEach(this::addController);
        }
        return self();
    }

    public D addController(Class<?> clazz, boolean singleton) {
        checkImmutable();
        Checks.checkNotNull(clazz, "clazz");
        checkDuplicateController(clazz);
        if (singleton) {
            return addController(ObjectUtils.instantiateBeanIfNecessary(clazz));
        } else {
            this.prototypeControllers.add(clazz);
            return self();
        }
    }

    public D addControllers(Collection<Class<?>> classes, boolean singleton) {
        checkImmutable();
        if (classes != null && !classes.isEmpty()) {
            classes.forEach(clazz -> this.addController(clazz, singleton));
        }
        return self();
    }

    /**
     * Adds controller advice bean or class.
     *
     * @param beanOrClass an {@link Object} instance or {@link Class}.
     * @return this deployments
     */
    public D addControllerAdvice(Object beanOrClass) {
        checkImmutable();
        Checks.checkNotNull(beanOrClass, "beanOrClass");
        Checks.checkArg(this.advices.stream()
                        .noneMatch(adc -> ClassUtils.getUserType(adc) == ClassUtils.getUserType(beanOrClass)),
                "Duplicated advice bean or class '" + beanOrClass + "'");
        this.advices.add(ObjectUtils.instantiateBeanIfNecessary(beanOrClass));
        return self();
    }

    /**
     * Adds controller advice beans.
     *
     * @param beanOrClasses {@link Object} instances or {@link Class}s.
     * @return this deployments
     */
    public D addControllerAdvices(Collection<?> beanOrClasses) {
        checkImmutable();
        if (beanOrClasses != null && !beanOrClasses.isEmpty()) {
            beanOrClasses.forEach(this::addControllerAdvice);
        }
        return self();
    }

    /**
     * Adds a {@link RouteInterceptor}.
     *
     * @param interceptor interceptor
     * @return this deployments
     */
    public D addRouteInterceptor(RouteInterceptor interceptor) {
        return addInterceptorFactory(InterceptorFactory.of(interceptor));
    }

    /**
     * Adds {@link RouteInterceptor}s.
     *
     * @param interceptors interceptors
     * @return this deployments
     */
    public D addRouteInterceptors(Collection<? extends RouteInterceptor> interceptors) {
        if (interceptors != null && !interceptors.isEmpty()) {
            interceptors.forEach(this::addRouteInterceptor);
        }
        return self();
    }

    /**
     * Adds a {@link HandlerInterceptor}.
     *
     * @param interceptor interceptor
     * @return this deployments
     */
    public D addHandlerInterceptor(HandlerInterceptor interceptor) {
        return addInterceptorFactory(InterceptorFactory.of(interceptor));
    }

    /**
     * Adds {@link HandlerInterceptor}s.
     *
     * @param interceptors interceptors
     * @return this deployments
     */
    public D addHandlerInterceptors(Collection<? extends HandlerInterceptor> interceptors) {
        checkImmutable();
        if (interceptors != null && !interceptors.isEmpty()) {
            interceptors.forEach(this::addHandlerInterceptor);
        }
        return self();
    }

    /**
     * Adds a {@link MappingInterceptor}.
     *
     * @param interceptor interceptors
     * @return this deployments
     */
    public D addMappingInterceptor(MappingInterceptor interceptor) {
        return addInterceptorFactory(InterceptorFactory.of(interceptor));
    }

    /**
     * Adds {@link MappingInterceptor}s.
     *
     * @param interceptors interceptors
     * @return this deployments
     */
    public D addMappingInterceptors(Collection<? extends MappingInterceptor> interceptors) {
        checkImmutable();
        if (interceptors != null && !interceptors.isEmpty()) {
            interceptors.forEach(this::addMappingInterceptor);
        }
        return self();
    }

    /**
     * Adds {@link InterceptorFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param interceptor interceptor
     * @return this deployments
     */
    public D addInterceptor(Interceptor interceptor) {
        return addInterceptorFactory(InterceptorFactory.of(interceptor));
    }

    /**
     * Adds {@link InterceptorFactory}s.
     *
     * @param interceptors interceptors
     * @return this deployments
     */
    public D addInterceptors(Collection<? extends Interceptor> interceptors) {
        checkImmutable();
        if (interceptors != null && !interceptors.isEmpty()) {
            interceptors.forEach(this::addInterceptor);
        }
        return self();
    }

    /**
     * Adds {@link InterceptorFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param factory factory
     * @return this deployments
     */
    public D addInterceptorFactory(InterceptorFactory factory) {
        checkImmutable();
        Checks.checkNotNull(factory, "factory");
        this.interceptors.add(factory);
        return self();
    }

    /**
     * Adds {@link InterceptorFactory}s.
     *
     * @param interceptors interceptors
     * @return this deployments
     */
    public D addInterceptorFactories(Collection<? extends InterceptorFactory> interceptors) {
        checkImmutable();
        if (interceptors != null && !interceptors.isEmpty()) {
            this.interceptors.addAll(interceptors);
        }
        return self();
    }

    /**
     * Adds {@link StringConverterAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param converter resolver
     * @return this deployments
     */
    public D addStringConverter(StringConverterAdapter converter) {
        checkImmutable();
        Checks.checkNotNull(converter, "converter");
        return addStringConverter(StringConverterFactory.singleton(converter));
    }

    /**
     * Adds {@link StringConverterFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param converter resolver
     * @return this deployments
     */
    public D addStringConverter(StringConverterFactory converter) {
        checkImmutable();
        Checks.checkNotNull(converter, "converter");
        this.stringConverters.add(converter);
        return self();
    }

    /**
     * Adds {@link StringConverterFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param converts converters
     * @return this deployments
     */
    public D addStringConverters(Collection<? extends StringConverterFactory> converts) {
        checkImmutable();
        if (converts != null && !converts.isEmpty()) {
            this.stringConverters.addAll(converts);
        }
        return self();
    }

    /**
     * Adds {@link ParamResolverAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     * @return this deployments
     */
    public D addParamResolver(ParamResolverAdapter resolver) {
        checkImmutable();
        Checks.checkNotNull(resolver, "resolver");
        return addParamResolver(ParamResolverFactory.singleton(resolver));
    }

    /**
     * Adds {@link ParamResolverFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     * @return this deployments
     */
    public D addParamResolver(ParamResolverFactory resolver) {
        checkImmutable();
        Checks.checkNotNull(resolver, "resolver");
        this.paramResolvers.add(resolver);
        return self();
    }

    /**
     * Adds {@link ParamResolverFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolvers resolvers
     * @return this deployments
     */
    public D addParamResolvers(Collection<? extends ParamResolverFactory> resolvers) {
        checkImmutable();
        if (resolvers != null && !resolvers.isEmpty()) {
            this.paramResolvers.addAll(resolvers);
        }
        return self();
    }

    /**
     * Adds {@link ParamResolverFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     * @return this deployments
     */
    public D addParamResolverAdvice(ParamResolverAdviceAdapter advice) {
        checkImmutable();
        Checks.checkNotNull(advice, "advice");
        return addParamResolverAdvice(ParamResolverAdviceFactory.singleton(advice));
    }

    /**
     * Adds {@link ParamResolverAdviceFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     * @return this deployments
     */
    public D addParamResolverAdvice(ParamResolverAdviceFactory advice) {
        checkImmutable();
        Checks.checkNotNull(advice, "advice");
        this.paramResolverAdvices.add(advice);
        return self();
    }

    /**
     * Adds {@link ParamResolverAdviceFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advices advices
     * @return this deployments
     */
    public D addParamResolverAdvices(Collection<? extends ParamResolverAdviceFactory> advices) {
        checkImmutable();
        if (advices != null && !advices.isEmpty()) {
            this.paramResolverAdvices.addAll(advices);
        }
        return self();
    }

    /**
     * Adds {@link ContextResolverAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     * @return this deployments
     */
    public D addContextResolver(ContextResolverAdapter resolver) {
        checkImmutable();
        Checks.checkNotNull(resolver, "resolver");
        return addContextResolver(ContextResolverFactory.singleton(resolver));
    }

    /**
     * Adds {@link ParamResolverFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     * @return this deployments
     */
    public D addContextResolver(ContextResolverFactory resolver) {
        checkImmutable();
        Checks.checkNotNull(resolver, "resolver");
        this.contextResolvers.add(resolver);
        return self();
    }

    /**
     * Adds {@link ParamResolverFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolvers resolvers
     * @return this deployments
     */
    public D addContextResolvers(Collection<? extends ContextResolverFactory> resolvers) {
        checkImmutable();
        if (resolvers != null && !resolvers.isEmpty()) {
            this.contextResolvers.addAll(resolvers);
        }
        return self();
    }

    /**
     * Adds {@link RequestEntityResolverAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     * @return this deployments
     */
    public D addRequestEntityResolver(RequestEntityResolverAdapter resolver) {
        checkImmutable();
        Checks.checkNotNull(resolver, "resolver");
        return addRequestEntityResolver(RequestEntityResolverFactory.singleton(resolver));
    }

    /**
     * Adds {@link RequestEntityResolverFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     * @return this deployments
     */
    public D addRequestEntityResolver(RequestEntityResolverFactory resolver) {
        checkImmutable();
        Checks.checkNotNull(resolver, "resolver");
        this.requestEntityResolvers.add(resolver);
        return self();
    }

    /**
     * Adds {@link RequestEntityResolverFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolvers resolvers
     * @return this deployments
     */
    public D addRequestEntityResolvers(Collection<? extends RequestEntityResolverFactory> resolvers) {
        checkImmutable();
        if (resolvers != null && !resolvers.isEmpty()) {
            this.requestEntityResolvers.addAll(resolvers);
        }
        return self();
    }

    /**
     * Adds {@link RequestEntityResolverAdviceAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     * @return this deployments
     */
    public D addRequestEntityResolverAdvice(RequestEntityResolverAdviceAdapter advice) {
        checkImmutable();
        Checks.checkNotNull(advice, "advice");
        return addRequestEntityResolverAdvice(RequestEntityResolverAdviceFactory.singleton(advice));
    }

    /**
     * Adds {@link RequestEntityResolverAdviceFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     * @return this deployments
     */
    public D addRequestEntityResolverAdvice(RequestEntityResolverAdviceFactory advice) {
        checkImmutable();
        Checks.checkNotNull(advice, "advice");
        this.requestEntityResolverAdvices.add(advice);
        return self();
    }

    /**
     * Adds {@link RequestEntityResolverAdviceFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advices resolvers
     * @return this deployments
     */
    public D addRequestEntityResolverAdvices(Collection<? extends RequestEntityResolverAdviceFactory> advices) {
        checkImmutable();
        if (advices != null && !advices.isEmpty()) {
            this.requestEntityResolverAdvices.addAll(advices);
        }
        return self();
    }

    public D addResponseEntityResolver(ResponseEntityResolverAdapter resolver) {
        checkImmutable();
        Checks.checkNotNull(resolver, "resolver");
        return addResponseEntityResolver(ResponseEntityResolverFactory.singleton(resolver));
    }

    /**
     * Adds {@link ResponseEntityResolverFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     * @return this deployments
     */
    public D addResponseEntityResolver(ResponseEntityResolverFactory resolver) {
        checkImmutable();
        Checks.checkNotNull(resolver, "resolver");
        this.responseEntityResolvers.add(resolver);
        return self();
    }

    /**
     * Adds {@link ResponseEntityResolverFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolvers resolvers
     * @return this deployments
     */
    public D addResponseEntityResolvers(Collection<? extends ResponseEntityResolverFactory> resolvers) {
        checkImmutable();
        if (resolvers != null && !resolvers.isEmpty()) {
            this.responseEntityResolvers.addAll(resolvers);
        }
        return self();
    }

    /**
     * Adds {@link ResponseEntityResolverAdviceAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     * @return this deployments
     */
    public D addResponseEntityResolverAdvice(ResponseEntityResolverAdviceAdapter advice) {
        checkImmutable();
        Checks.checkNotNull(advice, "advice");
        return addResponseEntityResolverAdvice(ResponseEntityResolverAdviceFactory.singleton(advice));
    }

    /**
     * Adds {@link ResponseEntityResolverAdviceAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     * @return this deployments
     */
    public D addResponseEntityResolverAdvice(ResponseEntityResolverAdviceFactory advice) {
        checkImmutable();
        Checks.checkNotNull(advice, "advice");
        this.responseEntityResolverAdvices.add(advice);
        return self();
    }

    /**
     * Adds {@link ResponseEntityResolverAdviceFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advices resolvers
     * @return this deployments
     */
    public D addResponseEntityResolverAdvices(Collection<? extends ResponseEntityResolverAdviceFactory> advices) {
        checkImmutable();
        if (advices != null && !advices.isEmpty()) {
            this.responseEntityResolverAdvices.addAll(advices);
        }
        return self();
    }

    /**
     * Adds {@link HttpRequestSerializer} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param requestSerializer requestSerializer
     * @return this deployments
     */
    public D addRequestSerializer(HttpRequestSerializer requestSerializer) {
        checkImmutable();
        Checks.checkNotNull(requestSerializer, "requestSerializer");
        this.rxSerializers.add(requestSerializer);
        return self();
    }

    /**
     * Adds {@link HttpRequestSerializer}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param requestSerializers requestSerializers
     * @return this deployments
     */
    public D addRequestSerializers(Collection<? extends HttpRequestSerializer> requestSerializers) {
        checkImmutable();
        if (requestSerializers != null && !requestSerializers.isEmpty()) {
            this.rxSerializers.addAll(requestSerializers);
        }
        return self();
    }

    /**
     * Adds {@link HttpResponseSerializer} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param responseSerializer responseSerializer
     * @return this deployments
     */
    public D addResponseSerializer(HttpResponseSerializer responseSerializer) {
        checkImmutable();
        Checks.checkNotNull(responseSerializer, "responseSerializer");
        this.txSerializers.add(responseSerializer);
        return self();
    }

    /**
     * Adds {@link HttpResponseSerializer}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param responseSerializers responseSerializers
     * @return this deployments
     */
    public D addResponseSerializers(Collection<? extends HttpResponseSerializer> responseSerializers) {
        checkImmutable();
        if (responseSerializers != null && !responseSerializers.isEmpty()) {
            this.txSerializers.addAll(responseSerializers);
        }
        return self();
    }

    /**
     * Adds {@link HttpBodySerializer} which will be registered in the {@link HandlerResolverFactory} as {@link
     * HttpRequestSerializer} and {@link HttpResponseSerializer}
     *
     * @param serializer serializer
     * @return this deployments
     */
    public D addSerializer(HttpBodySerializer serializer) {
        checkImmutable();
        Checks.checkNotNull(serializer, "serializer");
        this.addRequestSerializer(serializer);
        this.addResponseSerializer(serializer);
        return self();
    }

    /**
     * Adds {@link HttpBodySerializer}s which will be registered in the {@link HandlerResolverFactory} as {@link
     * HttpRequestSerializer} and {@link HttpResponseSerializer}
     *
     * @param serializers serializers
     * @return this deployments
     */
    public D addSerializers(Collection<? extends HttpBodySerializer> serializers) {
        checkImmutable();
        if (serializers != null && !serializers.isEmpty()) {
            this.addRequestSerializers(serializers);
            this.addResponseSerializers(serializers);
        }
        return self();
    }

    @SuppressWarnings("unchecked")
    public <T extends Throwable> D addExceptionResolver(Class<T> type, ExceptionResolver<T> resolver) {
        checkImmutable();
        Checks.checkNotNull(type, "type");
        Checks.checkNotNull(resolver, "resolver");
        ExceptionResolver<Throwable> prev
                = this.exceptionResolvers.put(type, (ExceptionResolver<Throwable>) resolver);
        if (prev != null) {
            throw new IllegalStateException("Found duplicated ExceptionResolver of '"
                    + type.getName() +
                    "': '" + prev.getClass().getName() + ", '" + resolver.getClass().getName() + "'");
        }
        return self();
    }

    public D addHandlerRegistryAware(HandlerRegistryAware aware) {
        checkImmutable();
        Checks.checkNotNull(aware, "aware");
        return addHandlerRegistryAware((HandlerRegistryAwareFactory) deployContext -> Optional.of(aware));
    }

    public D addHandlerRegistryAware(HandlerRegistryAwareFactory aware) {
        checkImmutable();
        Checks.checkNotNull(aware, "aware");
        return addHandlerRegistryAwareness(Collections.singletonList(aware));
    }

    public D addHandlerRegistryAwareness(Collection<? extends HandlerRegistryAwareFactory> awareness) {
        checkImmutable();
        if (awareness != null && !awareness.isEmpty()) {
            this.handlerAwareness.addAll(awareness);
        }
        return self();
    }

    protected void beforeApplyDeployments() {
        List<DeploymentsConfigure> configures = SpiLoader.cached(DeploymentsConfigure.class)
                .getByFeature(restlight.name(),
                        true,
                        Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                        false);
        if (!configures.isEmpty()) {
            ConfigurableDeployments configurable = new ConfigurableDeployments(this);
            for (DeploymentsConfigure configure : configures) {
                try {
                    configure.accept(configurable);
                } catch (Throwable ex) {
                    LoggerUtils.logger().error("Error occurred while configuring deployments", ex);
                }
            }
        }
    }

    protected RestlightHandler doGetRestlightHandler() {
        // get or create a route registry.
        getOrCreateRegistry();

        ctx().setHandlerContextProvider(new HandlerContexts());
        // set the ResolvableParamPredicate immediately due to it may be used when resolving extensions.
        ctx().setParamPredicate(RouteUtils.loadResolvableParamPredicate(ctx()));

        ctx().setHandlers(new HandlersImpl());
        List<HandlerFactoryProvider> handlerFactories = SpiLoader.cached(HandlerFactoryProvider.class)
                .getByFeature(restlight.name(),
                        true,
                        Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                        false);
        OrderedComparator.sort(handlerFactories);
        handlerFactories.stream().map(provider -> provider.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .findFirst()
                .ifPresent(f -> ctx().setHandlerFactory(f.orElse(null)));

        ctx().setExtensions(handleThenGetExtensions());

        // set context components into context before initializing.
        OrderedComparator.sort(singletonControllers);
        ctx().setSingletonControllers(Collections.unmodifiableList(singletonControllers));
        ctx().setPrototypeControllers(Collections.unmodifiableList(prototypeControllers));

        OrderedComparator.sort(advices);
        ctx().setAdvices(Collections.unmodifiableList(advices));
        ctx().setExceptionMappers(exceptionResolvers.isEmpty()
                ? Collections.emptyList()
                : Collections.singletonList(new DefaultExceptionMapper(exceptionResolvers)));

        // load handler interceptors by spi
        addInterceptorFactories(SpiLoader.cached(InterceptorFactory.class)
                .getByGroup(restlight.name(), true));
        addRouteInterceptors(SpiLoader.cached(RouteInterceptor.class)
                .getByGroup(restlight.name(), true));
        addHandlerInterceptors(SpiLoader.cached(HandlerInterceptor.class)
                .getByGroup(restlight.name(), true));
        addMappingInterceptors(SpiLoader.cached(MappingInterceptor.class)
                .getByGroup(restlight.name(), true));
        addInterceptors(SpiLoader.cached(Interceptor.class)
                .getByGroup(restlight.name(), true));
        OrderedComparator.sort(interceptors);
        ctx().setInterceptors(Collections.unmodifiableList(interceptors));

        // load then add RouteRegistryAware by spi
        SpiLoader.cached(RouteRegistryAwareFactory.class).getByGroup(restlight.name(), true)
                .stream()
                .map(factory -> factory.createAware(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::addRouteRegistryAware);

        return createRestlightHandler();
    }

    private List<Object> handleThenGetExtensions() {
        List<Object> extensions = Collections.unmodifiableList(this.extensions);
        // handles extensions by custom ExtensionsHandler
        final MiniConfigurableDeployments miniDeployments = new MiniConfigurableDeployments(this);
        List<ExtensionsHandler> handlers = SpiLoader.cached(ExtensionsHandlerFactory.class)
                .getByFeature(restlight.name(),
                        true,
                        Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                        false)
                .stream().map((factory) -> factory.handler(miniDeployments))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        if (!handlers.isEmpty()) {
            for (ExtensionsHandler handler : handlers) {
                try {
                    handler.handle(extensions);
                } catch (Throwable th) {
                    LoggerUtils.logger().error("Error occurred while configuring routes", th);
                }
            }
        }
        return extensions;
    }

    /**
     * Registers routes into the given {@link RouteRegistry}.
     *
     * @param registry registry
     */
    protected void registerRoutes(RouteRegistry registry) {
        ctx().setResolverFactory(getHandlerResolverFactory());

        // set HandlerAdviceFactory
        Collection<HandlerAdviceFactory> handlerAdviceFactories = SpiLoader.cached(HandlerAdviceFactory.class)
                .getByFeature(restlight.name(),
                        true,
                        Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                        false);
        // set into context
        ctx().setHandlerAdvicesFactory(new HandlerAdvicesFactoryImpl(ctx(), handlerAdviceFactories));

        // RouteMethodLocator
        RouteMethodLocator methodLocator = RouteUtils.loadRouteMethodLocator(ctx());
        if (methodLocator == null) {
            LoggerUtils.logger().warn("Could not find any extension of " + RouteMethodLocator.class.getName());
        }
        ctx().setRouteMethodLocator(methodLocator);

        // MappingLocator
        MappingLocator mappingLocator = RouteUtils.loadMappingLocator(ctx());
        if (mappingLocator == null) {
            LoggerUtils.logger().warn("Could not find any extension of " +
                    MappingLocator.class.getName());
        }
        ctx().setMappingLocator(mappingLocator);

        // HandlerValueResolverLocator
        HandlerValueResolverLocator handlerResolverLocator = RouteUtils.loadHandlerValueResolverLocator(ctx());
        if (handlerResolverLocator == null) {
            LoggerUtils.logger().warn("Could not find any extension of " +
                    HandlerValueResolverLocator.class.getName());
        }
        ctx().setHandlerResolverLocator(handlerResolverLocator);

        // ExceptionResolverFactory
        ExceptionResolverFactory exceptionResolverFactory;
        List<ExceptionResolverFactoryProvider> providers = SpiLoader.cached(ExceptionResolverFactoryProvider.class)
                .getByFeature(restlight.name(),
                        true,
                        Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                        false);
        if (providers.isEmpty()) {
            exceptionResolverFactory =
                    new DefaultExceptionResolverFactory(ctx().exceptionMappers().orElse(null));
        } else {
            exceptionResolverFactory = providers.iterator().next().factory(ctx());
        }
        ctx().setExceptionResolverFactory(exceptionResolverFactory);

        // load and set HandlerConfigures
        handlerConfigures.addAll(SpiLoader.cached(HandlerConfigure.class)
                .getByFeature(restlight.name(),
                        true,
                        Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                        false));
        OrderedComparator.sort(handlerConfigures);
        ctx().setHandlerConfigure(Collections.unmodifiableList(handlerConfigures));

        HandlerRegistry handlerRegistry = new DefaultHandlerRegistry(ctx(),
                (HandlersImpl) ctx().handlers().orElse(null));
        // register handlers by handlerRegistry.
        this.registerHandlers(handlerRegistry);

        loadHandlerRegistriesFromSpi()
                .stream()
                .map(factory -> factory.createAware(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(aware -> aware.setRegistry(handlerRegistry));
        // register routes added by user.
        routes.forEach(registry::register);
    }

    private void registerHandlers(HandlerRegistry registry) {
        // register from singleton beans
        if (ctx().singletonControllers().isPresent()) {
            registry.addHandlers(ctx().singletonControllers().get());
        }

        // register from prototype beans
        if (ctx().prototypeControllers().isPresent()) {
            registry.addHandlers(ctx().prototypeControllers().get(), false);
        }

        // register from HandlerMappingProvider
        if (!mappingProviders.isEmpty()) {
            mappingProviders.forEach(provider -> {
                Collection<HandlerMapping> mappings = provider.mappings(ctx());
                if (mappings != null && !mappings.isEmpty()) {
                    registry.addHandlerMappings(mappings);
                }
            });
        }
    }

    private HandlerResolverFactory getHandlerResolverFactory() {
        loadDefaultSerializersIfNecessary();
        loadResolversFromSpi();

        this.routeFilters.addAll(SpiLoader.cached(RouteFilterFactory.class)
                .getByGroup(restlight.name(), true));
        // keep in order.
        OrderedComparator.sort(rxSerializers);
        OrderedComparator.sort(txSerializers);
        OrderedComparator.sort(routeFilters);
        OrderedComparator.sort(stringConverters);
        OrderedComparator.sort(paramResolvers);
        OrderedComparator.sort(paramResolverAdvices);
        OrderedComparator.sort(contextResolvers);
        OrderedComparator.sort(requestEntityResolvers);
        OrderedComparator.sort(requestEntityResolverAdvices);
        OrderedComparator.sort(responseEntityResolvers);
        OrderedComparator.sort(responseEntityResolverAdvices);

        List<FutureTransferFactory> futureTransfers = SpiLoader.cached(FutureTransferFactory.class)
                .getByFeature(restlight.name(),
                        true,
                        Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                        false);
        OrderedComparator.sort(futureTransfers);

        return new HandlerResolverFactoryImpl(
                rxSerializers,
                txSerializers,
                futureTransfers,
                routeFilters,
                null,
                stringConverters,
                null,
                paramResolvers,
                null,
                paramResolverAdvices,
                null,
                contextResolvers,
                null,
                requestEntityResolvers,
                null,
                requestEntityResolverAdvices,
                null,
                responseEntityResolvers,
                null,
                responseEntityResolverAdvices);
    }

    private void loadDefaultSerializersIfNecessary() {
        if (rxSerializers.isEmpty() || txSerializers.isEmpty()) {
            List<DefaultSerializerFactory> factories = SpiLoader.cached(DefaultSerializerFactory.class)
                    .getByFeature(restlight.name(),
                            true,
                            Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                            false);
            if (!factories.isEmpty()) {
                if (rxSerializers.isEmpty()) {
                    addRequestSerializer(factories.get(0).defaultRequestSerializer(ctx()));
                }
                if (txSerializers.isEmpty()) {
                    addResponseSerializer(factories.get(0).defaultResponseSerializer(ctx()));
                }
            }
        }
    }

    private List<HandlerRegistryAwareFactory> loadHandlerRegistriesFromSpi() {
        // load HandlerRegistryAware by spi
        this.handlerAwareness.addAll(SpiLoader.cached(HandlerRegistryAware.class).getAll()
                .stream().map(aware -> (HandlerRegistryAwareFactory) deployContext -> Optional.of(aware))
                .collect(Collectors.toList()));
        this.handlerAwareness.addAll(SpiLoader.cached(HandlerRegistryAwareFactory.class).getAll());
        return handlerAwareness;
    }

    private void loadResolversFromSpi() {
        // load StringConverter from spi
        SpiLoader.cached(StringConverterAdapter.class)
                .getByGroup(restlight.name(), true)
                .forEach(this::addStringConverter);
        addStringConverters(SpiLoader.cached(StringConverterFactory.class)
                .getByGroup(restlight.name(), true));
        addStringConverters(SpiLoader.cached(StringConverterProvider.class)
                .getByGroup(restlight.name(), true)
                .stream()
                .map(provider -> provider.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));

        // load ParamResolver from spi
        SpiLoader.cached(ParamResolverAdapter.class)
                .getByGroup(restlight.name(), true)
                .forEach(this::addParamResolver);
        addParamResolvers(SpiLoader.cached(ParamResolverFactory.class)
                .getByGroup(restlight.name(), true));
        addParamResolvers(SpiLoader.cached(ParamResolverProvider.class)
                .getByGroup(restlight.name(), true)
                .stream()
                .map(provider -> provider.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));

        // load ParamResolverAdvice from spi
        SpiLoader.cached(ParamResolverAdviceAdapter.class)
                .getByGroup(restlight.name(), true)
                .forEach(this::addParamResolverAdvice);
        addParamResolverAdvices(SpiLoader.cached(ParamResolverAdviceFactory.class)
                .getByGroup(restlight.name(), true));
        addParamResolverAdvices(SpiLoader.cached(ParamResolverAdviceProvider.class)
                .getByGroup(restlight.name(), true)
                .stream()
                .map(provider -> provider.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));

        // load ContextResolver from spi
        SpiLoader.cached(ContextResolverAdapter.class)
                .getByGroup(restlight.name(), true)
                .forEach(this::addContextResolver);
        addContextResolvers(SpiLoader.cached(ContextResolverFactory.class)
                .getByGroup(restlight.name(), true));
        addContextResolvers(SpiLoader.cached(ContextResolverProvider.class)
                .getByGroup(restlight.name(), true)
                .stream()
                .map(provider -> provider.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));

        // load RequestEntityResolver from spi
        SpiLoader.cached(RequestEntityResolverAdapter.class)
                .getByGroup(restlight.name(), true)
                .forEach(this::addRequestEntityResolver);
        addRequestEntityResolvers(SpiLoader.cached(RequestEntityResolverFactory.class)
                .getByGroup(restlight.name(), true));
        addRequestEntityResolvers(SpiLoader.cached(RequestEntityResolverProvider.class)
                .getByGroup(restlight.name(), true)
                .stream()
                .map(provider -> provider.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));

        // load RequestEntityResolverAdvice from spi
        SpiLoader.cached(RequestEntityResolverAdviceAdapter.class)
                .getByGroup(restlight.name(), true)
                .forEach(this::addRequestEntityResolverAdvice);
        addRequestEntityResolverAdvices(SpiLoader.cached(RequestEntityResolverAdviceFactory.class)
                .getByGroup(restlight.name(), true));
        addRequestEntityResolverAdvices(SpiLoader.cached(RequestEntityResolverAdviceProvider.class)
                .getByGroup(restlight.name(), true)
                .stream()
                .map(provider -> provider.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));

        // load ResponseEntityResolver from spi
        SpiLoader.cached(ResponseEntityResolverAdapter.class)
                .getByGroup(restlight.name(), true)
                .forEach(this::addResponseEntityResolver);
        addResponseEntityResolvers(SpiLoader.cached(ResponseEntityResolverFactory.class)
                .getByGroup(restlight.name(), true));
        addResponseEntityResolvers(SpiLoader.cached(ResponseEntityResolverProvider.class)
                .getByGroup(restlight.name(), true)
                .stream()
                .map(provider -> provider.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));

        // load ResponseEntityResolverAdvice from spi
        SpiLoader.cached(ResponseEntityResolverAdviceAdapter.class)
                .getByGroup(restlight.name(), true)
                .forEach(this::addResponseEntityResolverAdvice);
        addResponseEntityResolverAdvices(SpiLoader.cached(ResponseEntityResolverAdviceFactory.class)
                .getByGroup(restlight.name(), true));
        addResponseEntityResolverAdvices(SpiLoader.cached(ResponseEntityResolverAdviceProvider.class)
                .getByGroup(restlight.name(), true)
                .stream()
                .map(provider -> provider.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));
    }

    private void checkDuplicateController(Object beanOrClass) {
        Checks.checkArg(this.singletonControllers.stream()
                        .noneMatch(c -> ClassUtils.getUserType(c) == ClassUtils.getUserType(beanOrClass)),
                "Duplicated controller bean or class '"
                        + beanOrClass + "' of class '" + beanOrClass.getClass() + "'");
        Checks.checkArg(this.prototypeControllers.stream()
                        .noneMatch(c -> ClassUtils.getUserType(c) == ClassUtils.getUserType(beanOrClass)),
                "Duplicated controller bean or class '"
                        + beanOrClass + "' of class '" + beanOrClass.getClass() + "'");
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

    /**
     * @return current Restlight
     */
    public R server() {
        return restlight;
    }

    protected DeployContextImpl ctx() {
        return (DeployContextImpl) deployContext;
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

    protected RestlightHandler getRestlightHandler() {
        if (handler == null) {
            handler = doGetRestlightHandler();
        }
        return handler;
    }

    protected RestlightHandler createRestlightHandler() {
        RoutableRegistry routeRegistry = getOrCreateRegistry();
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

    protected final RoutableRegistry getOrCreateRegistry() {
        if (ctx().routeRegistry().isPresent()) {
            return (RoutableRegistry) ctx().routeRegistry().get();
        } else {
            AbstractRouteRegistry registry;
            if (deployContext.options().getRoute().isUseCachedRouting() && routes.size() >= 10) {
                registry = new CachedRouteRegistry(deployContext.options().getRoute().getComputeRate());
            } else {
                registry = new SimpleRouteRegistry();
            }
            RoutableRegistry registry0 = new RoutableRegistry(ctx(), registry);
            ctx().setRegistry(registry0);
            return registry0;
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

    public static class Impl extends Deployments<Restlight, Impl> {

        Impl(Restlight restlight, RestlightOptions options) {
            super(restlight, options);
        }
    }

}
