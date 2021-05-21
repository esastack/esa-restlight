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
package esa.restlight.core;

import esa.commons.Checks;
import esa.commons.ClassUtils;
import esa.commons.ObjectUtils;
import esa.commons.StringUtils;
import esa.commons.spi.SpiLoader;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.handler.HandlerMapping;
import esa.restlight.core.handler.HandlerMappingProvider;
import esa.restlight.core.handler.impl.HandlerAdvicesFactoryImpl;
import esa.restlight.core.handler.locate.MappingLocator;
import esa.restlight.core.handler.locate.RouteHandlerLocator;
import esa.restlight.core.interceptor.HandlerInterceptor;
import esa.restlight.core.interceptor.Interceptor;
import esa.restlight.core.interceptor.InterceptorFactory;
import esa.restlight.core.interceptor.MappingInterceptor;
import esa.restlight.core.interceptor.RouteInterceptor;
import esa.restlight.core.resolver.ArgumentResolverAdapter;
import esa.restlight.core.resolver.ArgumentResolverAdviceAdapter;
import esa.restlight.core.resolver.ArgumentResolverAdviceFactory;
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.resolver.ExceptionResolver;
import esa.restlight.core.resolver.HandlerResolverFactory;
import esa.restlight.core.resolver.HandlerResolverFactoryImpl;
import esa.restlight.core.resolver.ReturnValueResolverAdapter;
import esa.restlight.core.resolver.ReturnValueResolverAdviceAdapter;
import esa.restlight.core.resolver.ReturnValueResolverAdviceFactory;
import esa.restlight.core.resolver.ReturnValueResolverFactory;
import esa.restlight.core.resolver.exception.DefaultExceptionMapper;
import esa.restlight.core.resolver.exception.DefaultExceptionResolverFactory;
import esa.restlight.core.resolver.exception.ExceptionResolverFactory;
import esa.restlight.core.serialize.HttpBodySerializer;
import esa.restlight.core.serialize.HttpRequestSerializer;
import esa.restlight.core.serialize.HttpResponseSerializer;
import esa.restlight.core.spi.ArgumentResolverAdviceProvider;
import esa.restlight.core.spi.ArgumentResolverProvider;
import esa.restlight.core.spi.DefaultSerializerFactory;
import esa.restlight.core.spi.ExceptionResolverFactoryProvider;
import esa.restlight.core.spi.HandlerAdviceFactory;
import esa.restlight.core.spi.MethodAdviceFactory;
import esa.restlight.core.spi.ReturnValueResolverAdviceProvider;
import esa.restlight.core.spi.ReturnValueResolverProvider;
import esa.restlight.core.util.Constants;
import esa.restlight.core.util.OrderedComparator;
import esa.restlight.core.util.RouteUtils;
import esa.restlight.server.BaseDeployments;
import esa.restlight.server.ServerDeployContext;
import esa.restlight.server.bootstrap.WebServerException;
import esa.restlight.server.handler.RestlightHandler;
import esa.restlight.server.route.RouteRegistry;
import esa.restlight.server.util.LoggerUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation for a Restlight server bootstrap and returns a {@link RestlightHandler} by {@link BaseDeployments} for
 * {@link AbstractRestlight}.
 */
public class Deployments<R extends AbstractRestlight<R, D, O>, D extends Deployments<R, D, O>,
        O extends RestlightOptions>
        extends BaseDeployments<R, D, O> {

    private final List<HandlerMappingProvider> mappingProviders = new LinkedList<>();
    private final List<Object> controllers = new LinkedList<>();
    private final List<Object> advices = new LinkedList<>();
    private final List<ArgumentResolverFactory> argumentResolvers = new LinkedList<>();
    private final List<ArgumentResolverAdviceFactory> argumentResolverAdvices = new LinkedList<>();
    private final List<ReturnValueResolverFactory> returnValueResolvers = new LinkedList<>();
    private final List<ReturnValueResolverAdviceFactory> returnValueResolverAdvices = new LinkedList<>();
    private final List<HttpRequestSerializer> rxSerializers = new LinkedList<>();
    private final List<HttpResponseSerializer> txSerializers = new LinkedList<>();
    private final List<InterceptorFactory> interceptors = new LinkedList<>();
    private final Map<Class<? extends Throwable>, ExceptionResolver<Throwable>> exceptionResolvers
            = new LinkedHashMap<>();

    protected Deployments(R restlight, O options) {
        super(restlight, options);
    }

    @Override
    protected ServerDeployContext<O> newContext(O options) {
        return new DeployContextImpl<>(restlight.name(), options);
    }

    @Override
    protected DeployContextImpl<O> ctx() {
        return (DeployContextImpl<O>) super.ctx();
    }

    @Override
    public DeployContext<O> deployContext() {
        return ctx();
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
     * @deprecated use {@link #addHandlerMappings(Collection)}
     */
    @Deprecated
    public D addHandlerMapping(Collection<? extends HandlerMapping> mappings) {
        return addHandlerMappings(mappings);
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
     *
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
     * Adds a controller bean or class which will be registered in the {@link RouteRegistry}.
     *
     * @param beanOrClass an {@link Object} instance or {@link Class}.
     *
     * @return this deployments
     */
    public D addController(Object beanOrClass) {
        checkImmutable();
        Checks.checkNotNull(beanOrClass, "beanOrClass");
        Checks.checkArg(this.controllers.stream()
                        .noneMatch(c -> ClassUtils.getUserType(c) == ClassUtils.getUserType(beanOrClass)),
                "Duplicated controller bean or class '"
                        + beanOrClass + "' of class '" + beanOrClass.getClass() + "'");
        this.controllers.add(ObjectUtils.instantiateBeanIfNecessary(beanOrClass));
        return self();
    }

    /**
     * Adds controller beans or classes which will be registered in the {@link RouteRegistry}
     *
     * @param beanOrClasses {@link Object} instances or {@link Class}s.
     *
     * @return this deployments
     */
    public D addControllers(Collection<?> beanOrClasses) {
        checkImmutable();
        if (beanOrClasses != null && !beanOrClasses.isEmpty()) {
            beanOrClasses.forEach(this::addController);
        }
        return self();
    }

    /**
     * Adds controller advice bean or class.
     *
     * @param beanOrClass an {@link Object} instance or {@link Class}.
     *
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
     *
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
     *
     * @return this deployments
     */
    public D addRouteInterceptor(RouteInterceptor interceptor) {
        return addInterceptorFactory(InterceptorFactory.of(interceptor));
    }

    /**
     * Adds {@link RouteInterceptor}s.
     *
     * @param interceptors interceptors
     *
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
     *
     * @return this deployments
     */
    public D addHandlerInterceptor(HandlerInterceptor interceptor) {
        return addInterceptorFactory(InterceptorFactory.of(interceptor));
    }

    /**
     * Adds {@link HandlerInterceptor}s.
     *
     * @param interceptors interceptors
     *
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
     *
     * @return this deployments
     */
    public D addMappingInterceptor(MappingInterceptor interceptor) {
        return addInterceptorFactory(InterceptorFactory.of(interceptor));
    }

    /**
     * Adds {@link MappingInterceptor}s.
     *
     * @param interceptors interceptors
     *
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
     *
     * @return this deployments
     */
    public D addInterceptor(Interceptor interceptor) {
        return addInterceptorFactory(InterceptorFactory.of(interceptor));
    }

    /**
     * Adds {@link InterceptorFactory}s.
     *
     * @param interceptors interceptors
     *
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
     *
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
     *
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
     * Adds {@link ArgumentResolverAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     *
     * @return this deployments
     */
    public D addArgumentResolver(ArgumentResolverAdapter resolver) {
        checkImmutable();
        Checks.checkNotNull(resolver, "resolver");
        return addArgumentResolver(ArgumentResolverFactory.singleton(resolver));
    }

    /**
     * Adds {@link ArgumentResolverFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     *
     * @return this deployments
     */
    public D addArgumentResolver(ArgumentResolverFactory resolver) {
        checkImmutable();
        Checks.checkNotNull(resolver, "resolver");
        this.argumentResolvers.add(resolver);
        return self();
    }

    /**
     * Adds {@link ArgumentResolverFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolvers resolvers
     *
     * @return this deployments
     */
    public D addArgumentResolvers(Collection<? extends ArgumentResolverFactory> resolvers) {
        checkImmutable();
        if (resolvers != null && !resolvers.isEmpty()) {
            this.argumentResolvers.addAll(resolvers);
        }
        return self();
    }

    /**
     * Adds {@link ArgumentResolverFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     *
     * @return this deployments
     */
    public D addArgumentResolverAdvice(ArgumentResolverAdviceAdapter advice) {
        checkImmutable();
        Checks.checkNotNull(advice, "advice");
        return addArgumentResolverAdvice(ArgumentResolverAdviceFactory.singleton(advice));
    }

    /**
     * Adds {@link ArgumentResolverAdviceFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     *
     * @return this deployments
     */
    public D addArgumentResolverAdvice(ArgumentResolverAdviceFactory advice) {
        checkImmutable();
        Checks.checkNotNull(advice, "advice");
        this.argumentResolverAdvices.add(advice);
        return self();
    }

    /**
     * Adds {@link ArgumentResolverAdviceFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advices advices
     *
     * @return this deployments
     */
    public D addArgumentResolverAdvices(Collection<? extends ArgumentResolverAdviceFactory> advices) {
        checkImmutable();
        if (advices != null && !advices.isEmpty()) {
            this.argumentResolverAdvices.addAll(advices);
        }
        return self();
    }

    /**
     * Adds {@link ReturnValueResolverAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     *
     * @return this deployments
     */
    public D addReturnValueResolver(ReturnValueResolverAdapter resolver) {
        checkImmutable();
        Checks.checkNotNull(resolver, "resolver");
        return addReturnValueResolver(ReturnValueResolverFactory.singleton(resolver));
    }

    /**
     * Adds {@link ReturnValueResolverFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     *
     * @return this deployments
     */
    public D addReturnValueResolver(ReturnValueResolverFactory resolver) {
        checkImmutable();
        Checks.checkNotNull(resolver, "resolver");
        this.returnValueResolvers.add(resolver);
        return self();
    }

    /**
     * Adds {@link ReturnValueResolverFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolvers resolvers
     *
     * @return this deployments
     */
    public D addReturnValueResolvers(Collection<? extends ReturnValueResolverFactory> resolvers) {
        checkImmutable();
        if (resolvers != null && !resolvers.isEmpty()) {
            this.returnValueResolvers.addAll(resolvers);
        }
        return self();
    }

    /**
     * Adds {@link ReturnValueResolverAdviceAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     *
     * @return this deployments
     */
    public D addReturnValueResolverAdvice(ReturnValueResolverAdviceAdapter advice) {
        checkImmutable();
        Checks.checkNotNull(advice, "advice");
        return addReturnValueResolverAdvice(ReturnValueResolverAdviceFactory.singleton(advice));
    }

    /**
     * Adds {@link ReturnValueResolverAdviceAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     *
     * @return this deployments
     */
    public D addReturnValueResolverAdvice(ReturnValueResolverAdviceFactory advice) {
        checkImmutable();
        Checks.checkNotNull(advice, "advice");
        this.returnValueResolverAdvices.add(advice);
        return self();
    }

    /**
     * Adds {@link ReturnValueResolverAdviceFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advices resolvers
     *
     * @return this deployments
     */
    public D addReturnValueResolverAdvices(Collection<? extends ReturnValueResolverAdviceFactory> advices) {
        checkImmutable();
        if (advices != null && !advices.isEmpty()) {
            this.returnValueResolverAdvices.addAll(advices);
        }
        return self();
    }

    /**
     * Adds {@link HttpRequestSerializer} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param requestSerializer requestSerializer
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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

    @Override
    protected RestlightHandler doGetRestlightHandler() {
        // set context components into context before initializing.
        OrderedComparator.sort(controllers);
        ctx().setControllers(Collections.unmodifiableList(controllers));
        OrderedComparator.sort(advices);
        ctx().setAdvices(Collections.unmodifiableList(advices));
        ctx().setExceptionMappers(exceptionResolvers.isEmpty()
                ? Collections.emptyList()
                : Collections.singletonList(new DefaultExceptionMapper(exceptionResolvers)));

        // load spi handler interceptors
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
        return super.doGetRestlightHandler();
    }

    @Override
    protected void registerRoutes(RouteRegistry registry) {
        // register routes added by user.
        super.registerRoutes(registry);
        // set context components
        ctx().setResolverFactory(getHandlerResolverFactory());
        //set HandlerAdviceFactory
        Collection<MethodAdviceFactory> methodAdviceFactories = SpiLoader.cached(MethodAdviceFactory.class)
                .getByFeature(restlight.name(),
                        true,
                        Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                        false);
        Collection<HandlerAdviceFactory> handlerAdviceFactories = SpiLoader.cached(HandlerAdviceFactory.class)
                .getByFeature(restlight.name(),
                        true,
                        Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                        false);
        // convert MethodAdviceFactory to HandlerAdviceFactory
        handlerAdviceFactories.addAll(methodAdviceFactories.stream()
                .map(Deployments::convert2HandlerAdviceFactory)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        // set into context
        ctx().setHandlerAdvicesFactory(new HandlerAdvicesFactoryImpl(ctx(), handlerAdviceFactories));

        // route handler locator
        RouteHandlerLocator handlerLocator = RouteUtils.loadRouteHandlerLocator(ctx());
        if (handlerLocator == null) {
            LoggerUtils.logger().warn("Could not find any extension of " + RouteHandlerLocator.class.getName());
        }
        ctx().setRouteHandlerLocator(handlerLocator);

        // ExceptionResolverFactory
        final ExceptionResolverFactory exceptionResolverFactory;
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

        // MappingLocator
        MappingLocator mappingLocator = RouteUtils.loadRouteMappingLocator(ctx());
        if (mappingLocator == null) {
            LoggerUtils.logger().warn("Could not find any extension of " +
                    MappingLocator.class.getName());
        }
        ctx().setMappingLocator(mappingLocator);

        // registerRoutes handler
        this.registerHandlerMethod(registry);
    }

    private static HandlerAdviceFactory convert2HandlerAdviceFactory(MethodAdviceFactory methodAdviceFactory) {
        if (methodAdviceFactory == null) {
            return null;
        }
        return (ctx, handler) -> {
            final Object object = handler.handler().object();
            final Method method = handler.handler().method();
            return methodAdviceFactory.methodAdvice(ctx, object, method)
                    .map(mAdvice -> (request, response, args, invoker) -> {
                        if (!mAdvice.preInvoke(request, response, args)) {
                            throw new WebServerException("Failed to invoke method: [" + method.getName() + "], "
                                    + "due to that preInvoke() returned false!");
                        }
                        Object result = invoker.invoke(request, response, args);
                        //actual invoke
                        result = mAdvice.postInvoke(request, response, result);
                        return result;
                    });
        };
    }

    private HandlerResolverFactory getHandlerResolverFactory() {
        loadDefaultSerializersIfNecessary();
        loadResolversFromSpi();
        // keep in order.
        OrderedComparator.sort(rxSerializers);
        OrderedComparator.sort(txSerializers);
        OrderedComparator.sort(argumentResolvers);
        OrderedComparator.sort(argumentResolverAdvices);
        OrderedComparator.sort(returnValueResolvers);
        OrderedComparator.sort(returnValueResolverAdvices);

        return new HandlerResolverFactoryImpl(rxSerializers,
                txSerializers,
                null,
                argumentResolvers,
                null,
                argumentResolverAdvices,
                null,
                returnValueResolvers,
                null,
                returnValueResolverAdvices);

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

    private void loadResolversFromSpi() {
        // load ArgumentResolver from spi
        SpiLoader.cached(ArgumentResolverAdapter.class)
                .getByGroup(restlight.name(), true)
                .forEach(this::addArgumentResolver);
        addArgumentResolvers(SpiLoader.cached(ArgumentResolverFactory.class)
                .getByGroup(restlight.name(), true));
        addArgumentResolvers(SpiLoader.cached(ArgumentResolverProvider.class)
                .getByGroup(restlight.name(), true)
                .stream()
                .map(provider -> provider.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));

        // load ArgumentResolverAdvice from spi
        SpiLoader.cached(ArgumentResolverAdviceAdapter.class)
                .getByGroup(restlight.name(), true)
                .forEach(this::addArgumentResolverAdvice);
        addArgumentResolverAdvices(SpiLoader.cached(ArgumentResolverAdviceFactory.class)
                .getByGroup(restlight.name(), true));
        addArgumentResolverAdvices(SpiLoader.cached(ArgumentResolverAdviceProvider.class)
                .getByGroup(restlight.name(), true)
                .stream()
                .map(provider -> provider.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));

        // load ReturnValueResolver from spi
        SpiLoader.cached(ReturnValueResolverAdapter.class)
                .getByGroup(restlight.name(), true)
                .forEach(this::addReturnValueResolver);
        addReturnValueResolvers(SpiLoader.cached(ReturnValueResolverFactory.class)
                .getByGroup(restlight.name(), true));
        addReturnValueResolvers(SpiLoader.cached(ReturnValueResolverProvider.class)
                .getByGroup(restlight.name(), true)
                .stream()
                .map(provider -> provider.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));

        // load ReturnValueResolverAdvice from spi
        SpiLoader.cached(ReturnValueResolverAdviceAdapter.class)
                .getByGroup(restlight.name(), true)
                .forEach(this::addReturnValueResolverAdvice);
        addReturnValueResolverAdvices(SpiLoader.cached(ReturnValueResolverAdviceFactory.class)
                .getByGroup(restlight.name(), true));
        addReturnValueResolverAdvices(SpiLoader.cached(ReturnValueResolverAdviceProvider.class)
                .getByGroup(restlight.name(), true)
                .stream()
                .map(provider -> provider.factoryBean(ctx()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));
    }

    private void registerHandlerMethod(RouteRegistry registry) {
        // register from controller
        ctx().controllers().ifPresent(cs -> cs.forEach(bean -> {
            final Class<?> userType = ClassUtils.getUserType(bean);
            ClassUtils.doWithUserDeclaredMethodsMethods(userType,
                    method ->
                            RouteUtils.extractRoute(
                                    ctx(),
                                    userType,
                                    method,
                                    bean)
                                    .ifPresent(registry::registerRoute), method -> !method.isBridge());
        }));
        // register from HandlerMappingProvider
        if (!mappingProviders.isEmpty()) {
            mappingProviders.forEach(provider -> {
                Collection<HandlerMapping> handlerMappings = provider.mappings(ctx());
                if (handlerMappings != null && !handlerMappings.isEmpty()) {

                    handlerMappings.forEach(handlerMapping -> RouteUtils.extractRoute(
                            ctx(),
                            handlerMapping.mapping(),
                            handlerMapping.handler())
                            .ifPresent(registry::registerRoute));
                }
            });
        }
    }

    public static class Impl extends Deployments<Restlight, Impl, RestlightOptions> {
        Impl(Restlight restlight, RestlightOptions options) {
            super(restlight, options);
        }
    }

}
