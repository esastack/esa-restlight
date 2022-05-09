/*
 * Copyright 2022 OPPO ESA Stack Project
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
import io.esastack.restlight.core.deploy.ConfigurableDeployments;
import io.esastack.restlight.core.handler.DefaultHandlerRegistry;
import io.esastack.restlight.core.deploy.DeploymentsConfigure;
import io.esastack.restlight.core.deploy.ExtensionsHandler;
import io.esastack.restlight.core.deploy.HandlerConfigure;
import io.esastack.restlight.core.handler.HandlerRegistry;
import io.esastack.restlight.core.handler.HandlerRegistryAware;
import io.esastack.restlight.core.handler.HandlersImpl;
import io.esastack.restlight.core.deploy.MiniConfigurableDeployments;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.HandlerMappingProvider;
import io.esastack.restlight.core.filter.RouteFilterAdapter;
import io.esastack.restlight.core.handler.impl.HandlerAdvicesFactoryImpl;
import io.esastack.restlight.core.handler.impl.HandlerContexts;
import io.esastack.restlight.core.locator.HandlerValueResolverLocator;
import io.esastack.restlight.core.locator.MappingLocator;
import io.esastack.restlight.core.locator.RouteMethodLocator;
import io.esastack.restlight.core.interceptor.HandlerInterceptor;
import io.esastack.restlight.core.interceptor.Interceptor;
import io.esastack.restlight.core.interceptor.InterceptorFactory;
import io.esastack.restlight.core.interceptor.MappingInterceptor;
import io.esastack.restlight.core.interceptor.RouteInterceptor;
import io.esastack.restlight.core.resolver.context.ContextResolverAdapter;
import io.esastack.restlight.core.resolver.context.ContextResolverFactory;
import io.esastack.restlight.core.resolver.exception.ExceptionResolver;
import io.esastack.restlight.core.resolver.factory.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.factory.HandlerResolverFactoryImpl;
import io.esastack.restlight.core.resolver.param.ParamResolverAdapter;
import io.esastack.restlight.core.resolver.param.ParamResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.param.ParamResolverAdviceFactory;
import io.esastack.restlight.core.resolver.param.ParamResolverFactory;
import io.esastack.restlight.core.resolver.reqentity.RequestEntityResolverAdapter;
import io.esastack.restlight.core.resolver.reqentity.RequestEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.reqentity.RequestEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.reqentity.RequestEntityResolverFactory;
import io.esastack.restlight.core.resolver.rspentity.ResponseEntityResolverAdapter;
import io.esastack.restlight.core.resolver.rspentity.ResponseEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.rspentity.ResponseEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.rspentity.ResponseEntityResolverFactory;
import io.esastack.restlight.core.resolver.converter.StringConverterAdapter;
import io.esastack.restlight.core.resolver.converter.StringConverterFactory;
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
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.core.util.OrderedComparator;
import io.esastack.restlight.core.util.RouteUtils;
import io.esastack.restlight.core.dispatcher.DispatcherHandler;
import io.esastack.restlight.core.dispatcher.DispatcherHandlerImpl;
import io.esastack.restlight.core.dispatcher.IExceptionHandler;
import io.esastack.restlight.core.server.processor.schedule.RestlightThreadFactory;
import io.esastack.restlight.core.config.BizThreadsOptions;
import io.esastack.restlight.core.config.TimeoutOptions;
import io.esastack.restlight.core.server.handler.ConnectionHandler;
import io.esastack.restlight.core.server.handler.ConnectionInitHandler;
import io.esastack.restlight.core.server.handler.DisConnectionHandler;
import io.esastack.restlight.core.filter.Filter;
import io.esastack.restlight.core.server.processor.RestlightHandler;
import io.esastack.restlight.core.route.Route;
import io.esastack.restlight.core.route.RouteRegistry;
import io.esastack.restlight.core.route.impl.AbstractRouteRegistry;
import io.esastack.restlight.core.route.impl.CachedRouteRegistry;
import io.esastack.restlight.core.route.impl.RoutableRegistry;
import io.esastack.restlight.core.route.impl.SimpleRouteRegistry;
import io.esastack.restlight.core.server.processor.schedule.ExecutorScheduler;
import io.esastack.restlight.core.server.processor.schedule.RequestTask;
import io.esastack.restlight.core.server.processor.schedule.RequestTaskHook;
import io.esastack.restlight.core.server.processor.schedule.ScheduledRestlightHandler;
import io.esastack.restlight.core.server.processor.schedule.Scheduler;
import io.esastack.restlight.core.server.processor.schedule.Schedulers;
import io.esastack.restlight.core.spi.ConnectionHandlerFactory;
import io.esastack.restlight.core.spi.ConnectionInitHandlerFactory;
import io.esastack.restlight.core.spi.DisConnectionHandlerFactory;
import io.esastack.restlight.core.spi.ExceptionHandlerFactory;
import io.esastack.restlight.core.spi.FilterFactory;
import io.esastack.restlight.core.spi.RequestTaskHookFactory;
import io.esastack.restlight.core.spi.RouteRegistryAware;
import io.esastack.restlight.core.spi.RouteRegistryAwareFactory;
import io.esastack.restlight.core.util.LoggerUtils;

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
public abstract class Deployments {

    private static final Class<? extends RejectedExecutionHandler> JDK_DEFAULT_REJECT_HANDLER;

    /**
     * Hold the reference of {@link AbstractRestlight}
     */
    protected final AbstractRestlight restlight;
    private final List<Route> routes = new LinkedList<>();
    private final List<FilterFactory> filters = new LinkedList<>();
    private final List<ExceptionHandlerFactory> exceptionHandlerFactories = new LinkedList<>();
    private final List<ConnectionInitHandlerFactory> connectionInitHandlers = new LinkedList<>();
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

    protected Deployments(AbstractRestlight restlight, RestlightOptions options) {
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

    public Deployments addRouteFilter(RouteFilterAdapter filter) {
        checkImmutable();
        Checks.checkNotNull(filter, "filter");
        return addRouteFilter(RouteFilterFactory.singleton(filter));
    }

    public Deployments addRouteFilter(RouteFilterFactory filter) {
        checkImmutable();
        Checks.checkNotNull(filter, "filter");
        this.routeFilters.add(filter);
        return self();
    }

    public Deployments addRouteFilters(Collection<? extends RouteFilterFactory> filters) {
        checkImmutable();
        if (filters != null && !filters.isEmpty()) {
            filters.forEach(this::addRouteFilter);
        }
        return self();
    }

    public Deployments addExtension(Object extension) {
        checkImmutable();
        Checks.checkNotNull(extension, "extension");
        return this.addExtensions(Collections.singleton(extension));
    }

    public Deployments addExtensions(Collection<Object> extensions) {
        checkImmutable();
        if (extensions != null && !extensions.isEmpty()) {
            this.extensions.addAll(extensions);
        }
        return self();
    }

    public Deployments addHandlerConfigure(HandlerConfigure handlerConfigure) {
        checkImmutable();
        Checks.checkNotNull(handlerConfigure, "handlerConfigure");
        this.handlerConfigures.add(handlerConfigure);
        return self();
    }

    public Deployments addHandlerConfigures(Collection<? extends HandlerConfigure> handlerConfigures) {
        checkImmutable();
        if (handlerConfigures != null && !handlerConfigures.isEmpty()) {
            this.handlerConfigures.addAll(handlerConfigures);
        }
        return self();
    }

    /**
     * Adds {@link HandlerMapping}
     */
    public Deployments addHandlerMapping(HandlerMapping mapping) {
        checkImmutable();
        Checks.checkNotNull(mapping, "mapping");
        return this.addHandlerMappingProvider(ctx -> Collections.singletonList(mapping));
    }

    /**
     * Adds {@link HandlerMapping}
     */
    public Deployments addHandlerMappings(Collection<? extends HandlerMapping> mappings) {
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
    public Deployments addHandlerMappingProvider(HandlerMappingProvider provider) {
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
    public Deployments addHandlerMappingProviders(Collection<? extends HandlerMappingProvider> providers) {
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
    public Deployments addController(Object bean) {
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
    public Deployments addControllers(Collection<?> beans) {
        checkImmutable();
        if (beans != null && !beans.isEmpty()) {
            beans.forEach(this::addController);
        }
        return self();
    }

    public Deployments addController(Class<?> clazz, boolean singleton) {
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

    public Deployments addControllers(Collection<Class<?>> classes, boolean singleton) {
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
    public Deployments addControllerAdvice(Object beanOrClass) {
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
    public Deployments addControllerAdvices(Collection<?> beanOrClasses) {
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
    public Deployments addRouteInterceptor(RouteInterceptor interceptor) {
        return addInterceptorFactory(InterceptorFactory.of(interceptor));
    }

    /**
     * Adds {@link RouteInterceptor}s.
     *
     * @param interceptors interceptors
     * @return this deployments
     */
    public Deployments addRouteInterceptors(Collection<? extends RouteInterceptor> interceptors) {
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
    public Deployments addHandlerInterceptor(HandlerInterceptor interceptor) {
        return addInterceptorFactory(InterceptorFactory.of(interceptor));
    }

    /**
     * Adds {@link HandlerInterceptor}s.
     *
     * @param interceptors interceptors
     * @return this deployments
     */
    public Deployments addHandlerInterceptors(Collection<? extends HandlerInterceptor> interceptors) {
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
    public Deployments addMappingInterceptor(MappingInterceptor interceptor) {
        return addInterceptorFactory(InterceptorFactory.of(interceptor));
    }

    /**
     * Adds {@link MappingInterceptor}s.
     *
     * @param interceptors interceptors
     * @return this deployments
     */
    public Deployments addMappingInterceptors(Collection<? extends MappingInterceptor> interceptors) {
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
    public Deployments addInterceptor(Interceptor interceptor) {
        return addInterceptorFactory(InterceptorFactory.of(interceptor));
    }

    /**
     * Adds {@link InterceptorFactory}s.
     *
     * @param interceptors interceptors
     * @return this deployments
     */
    public Deployments addInterceptors(Collection<? extends Interceptor> interceptors) {
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
    public Deployments addInterceptorFactory(InterceptorFactory factory) {
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
    public Deployments addInterceptorFactories(Collection<? extends InterceptorFactory> interceptors) {
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
    public Deployments addStringConverter(StringConverterAdapter converter) {
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
    public Deployments addStringConverter(StringConverterFactory converter) {
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
    public Deployments addStringConverters(Collection<? extends StringConverterFactory> converts) {
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
    public Deployments addParamResolver(ParamResolverAdapter resolver) {
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
    public Deployments addParamResolver(ParamResolverFactory resolver) {
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
    public Deployments addParamResolvers(Collection<? extends ParamResolverFactory> resolvers) {
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
    public Deployments addParamResolverAdvice(ParamResolverAdviceAdapter advice) {
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
    public Deployments addParamResolverAdvice(ParamResolverAdviceFactory advice) {
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
    public Deployments addParamResolverAdvices(Collection<? extends ParamResolverAdviceFactory> advices) {
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
    public Deployments addContextResolver(ContextResolverAdapter resolver) {
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
    public Deployments addContextResolver(ContextResolverFactory resolver) {
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
    public Deployments addContextResolvers(Collection<? extends ContextResolverFactory> resolvers) {
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
    public Deployments addRequestEntityResolver(RequestEntityResolverAdapter resolver) {
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
    public Deployments addRequestEntityResolver(RequestEntityResolverFactory resolver) {
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
    public Deployments addRequestEntityResolvers(Collection<? extends RequestEntityResolverFactory> resolvers) {
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
    public Deployments addRequestEntityResolverAdvice(RequestEntityResolverAdviceAdapter advice) {
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
    public Deployments addRequestEntityResolverAdvice(RequestEntityResolverAdviceFactory advice) {
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
    public Deployments addRequestEntityResolverAdvices(
            Collection<? extends RequestEntityResolverAdviceFactory> advices) {
        checkImmutable();
        if (advices != null && !advices.isEmpty()) {
            this.requestEntityResolverAdvices.addAll(advices);
        }
        return self();
    }

    public Deployments addResponseEntityResolver(ResponseEntityResolverAdapter resolver) {
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
    public Deployments addResponseEntityResolver(ResponseEntityResolverFactory resolver) {
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
    public Deployments addResponseEntityResolvers(Collection<? extends ResponseEntityResolverFactory> resolvers) {
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
    public Deployments addResponseEntityResolverAdvice(ResponseEntityResolverAdviceAdapter advice) {
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
    public Deployments addResponseEntityResolverAdvice(ResponseEntityResolverAdviceFactory advice) {
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
    public Deployments addResponseEntityResolverAdvices(
            Collection<? extends ResponseEntityResolverAdviceFactory> advices) {
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
    public Deployments addRequestSerializer(HttpRequestSerializer requestSerializer) {
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
    public Deployments addRequestSerializers(Collection<? extends HttpRequestSerializer> requestSerializers) {
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
    public Deployments addResponseSerializer(HttpResponseSerializer responseSerializer) {
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
    public Deployments addResponseSerializers(Collection<? extends HttpResponseSerializer> responseSerializers) {
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
    public Deployments addSerializer(HttpBodySerializer serializer) {
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
    public Deployments addSerializers(Collection<? extends HttpBodySerializer> serializers) {
        checkImmutable();
        if (serializers != null && !serializers.isEmpty()) {
            this.addRequestSerializers(serializers);
            this.addResponseSerializers(serializers);
        }
        return self();
    }

    @SuppressWarnings("unchecked")
    public <T extends Throwable> Deployments addExceptionResolver(Class<T> type, ExceptionResolver<T> resolver) {
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

    public Deployments addHandlerRegistryAware(HandlerRegistryAware aware) {
        checkImmutable();
        Checks.checkNotNull(aware, "aware");
        return addHandlerRegistryAware((HandlerRegistryAwareFactory) deployContext -> Optional.of(aware));
    }

    public Deployments addHandlerRegistryAware(HandlerRegistryAwareFactory aware) {
        checkImmutable();
        Checks.checkNotNull(aware, "aware");
        return addHandlerRegistryAwareness(Collections.singletonList(aware));
    }

    public Deployments addHandlerRegistryAwareness(Collection<? extends HandlerRegistryAwareFactory> awareness) {
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
    public Deployments addRoute(Route route) {
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
    public Deployments addRoutes(Collection<? extends Route> routes) {
        checkImmutable();
        if (routes != null) {
            routes.forEach(this::addRoute);
        }
        return self();
    }

    public Deployments addScheduler(Scheduler scheduler) {
        checkImmutable();
        Checks.checkNotNull(scheduler, "scheduler");
        Scheduler configured = configExecutor(scheduler);
        ctx().mutableSchedulers().putIfAbsent(configured.name(), configured);
        return self();
    }

    public Deployments addSchedulers(Collection<? extends Scheduler> schedulers) {
        checkImmutable();
        if (schedulers != null && !schedulers.isEmpty()) {
            schedulers.forEach(this::addScheduler);
        }
        return self();
    }

    public Deployments addConnectionInitHandler(ConnectionInitHandler handler) {
        checkImmutable();
        Checks.checkNotNull(handler, "handler");
        return addConnectionInitHandler(ctx -> {
            return Optional.of(handler);
        });
    }

    public Deployments addConnectionInitHandler(ConnectionInitHandlerFactory handler) {
        checkImmutable();
        Checks.checkNotNull(handler, "handler");
        return addConnectionInitHandlers(Collections.singletonList(handler));
    }

    public Deployments addConnectionInitHandlers(Collection<? extends ConnectionInitHandlerFactory> handlers) {
        checkImmutable();
        if (handlers != null && !handlers.isEmpty()) {
            this.connectionInitHandlers.addAll(handlers);
        }
        return self();
    }

    public Deployments addConnectionHandler(ConnectionHandler handler) {
        checkImmutable();
        Checks.checkNotNull(handler, "handler");
        return addConnectionHandler(ctx -> {
            return Optional.of(handler);
        });
    }

    public Deployments addConnectionHandler(ConnectionHandlerFactory handler) {
        checkImmutable();
        Checks.checkNotNull(handler, "handler");
        return addConnectionHandlers(Collections.singletonList(handler));
    }

    public Deployments addConnectionHandlers(Collection<? extends ConnectionHandlerFactory> handlers) {
        checkImmutable();
        if (handlers != null && !handlers.isEmpty()) {
            this.connectionHandlers.addAll(handlers);
        }
        return self();
    }

    public Deployments addDisConnectionHandler(DisConnectionHandler handler) {
        checkImmutable();
        Checks.checkNotNull(handler, "handler");
        return addDisConnectionHandler(ctx -> {
            return Optional.of(handler);
        });
    }

    public Deployments addDisConnectionHandler(DisConnectionHandlerFactory handler) {
        checkImmutable();
        Checks.checkNotNull(handler, "handler");
        return addDisConnectionHandlers(Collections.singletonList(handler));
    }

    public Deployments addDisConnectionHandlers(Collection<? extends DisConnectionHandlerFactory> handlers) {
        checkImmutable();
        if (handlers != null && !handlers.isEmpty()) {
            this.disConnectionHandlers.addAll(handlers);
        }
        return self();
    }

    public Deployments addFilter(Filter filter) {
        checkImmutable();
        Checks.checkNotNull(filter, "filter");
        addFilter(ctx -> Optional.of(filter));
        return self();
    }

    public Deployments addFilter(FilterFactory filter) {
        checkImmutable();
        Checks.checkNotNull(filter, "filter");
        addFilters(Collections.singletonList(filter));
        return self();
    }

    public Deployments addFilters(Collection<? extends FilterFactory> filters) {
        checkImmutable();
        if (filters != null && !filters.isEmpty()) {
            this.filters.addAll(filters);
        }
        return self();
    }

    @Internal
    public Deployments addExceptionHandler(IExceptionHandler handler) {
        checkImmutable();
        Checks.checkNotNull(handler, "handler");
        this.exceptionHandlerFactories.add(ctx -> Optional.of(handler));
        return self();
    }

    @Internal
    public Deployments addExceptionHandlers(Collection<? extends IExceptionHandler> handlers) {
        checkImmutable();
        if (handlers != null && !handlers.isEmpty()) {
            handlers.forEach(this::addExceptionHandler);
        }
        return self();
    }

    @Internal
    public Deployments addExceptionHandler(ExceptionHandlerFactory handler) {
        checkImmutable();
        Checks.checkNotNull(handler, "handler");
        this.exceptionHandlerFactories.add(handler);
        return self();
    }

    public Deployments addRouteRegistryAware(RouteRegistryAware aware) {
        checkImmutable();
        Checks.checkNotNull(aware, "aware");
        return addRouteRegistryAware((RouteRegistryAwareFactory) deployContext -> Optional.of(aware));
    }

    public Deployments addRouteRegistryAware(RouteRegistryAwareFactory aware) {
        checkImmutable();
        Checks.checkNotNull(aware, "aware");
        return addRouteRegistryAwareness(Collections.singletonList(aware));
    }

    public Deployments addRouteRegistryAwareness(Collection<? extends RouteRegistryAwareFactory> awareness) {
        checkImmutable();
        if (awareness != null && !awareness.isEmpty()) {
            this.registryAwareness.addAll(awareness);
        }
        return self();
    }

    @Beta
    public Deployments addRequestTaskHook(RequestTaskHook hook) {
        return addRequestTaskHook((RequestTaskHookFactory) ctx -> Optional.of(hook));
    }

    @Beta
    public Deployments addRequestTaskHook(RequestTaskHookFactory hook) {
        checkImmutable();
        Checks.checkNotNull(hook, "hook");
        this.requestTaskHooks.add(hook);
        return self();
    }

    @Beta
    public Deployments addRequestTaskHooks(Collection<? extends RequestTaskHookFactory> hooks) {
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
    public AbstractRestlight server() {
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
        // init ExceptionHandlerChain
        final IExceptionHandler[] iExceptionHandlers = getExceptionHandlers();
        this.exceptionHandlers = iExceptionHandlers;

        // init DispatcherHandler
        this.dispatcher = new DispatcherHandlerImpl(routeRegistry, iExceptionHandlers);
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

        // load RequestTaskHookFactory by spi
        SpiLoader.cached(RequestTaskHookFactory.class)
                .getByGroup(restlight.name(), true)
                .forEach(this::addRequestTaskHook);
        // load RequestTaskHook by spi
        SpiLoader.cached(RequestTaskHook.class)
                .getByGroup(restlight.name(), true)
                .forEach(this::addRequestTaskHook);

        // load and add ConnectionInitHandlerFactory by spi
        SpiLoader.cached(ConnectionInitHandlerFactory.class)
                .getByGroup(restlight.name(), true)
                .forEach(factory -> factory.handler(ctx()).ifPresent(this::addConnectionInitHandler));

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
                connectionInitHandlers.stream()
                        .map(factory -> factory.handler(deployContext))
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

    protected Deployments self() {
        return this;
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

    public static class Impl extends Deployments {
        Impl(Restlight restlight, RestlightOptions options) {
            super(restlight, options);
        }
    }

}
