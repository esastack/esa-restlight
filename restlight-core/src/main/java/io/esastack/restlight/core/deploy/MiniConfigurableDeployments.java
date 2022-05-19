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
package io.esastack.restlight.core.deploy;

import esa.commons.annotation.Internal;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.Deployments;
import io.esastack.restlight.core.Restlight;
import io.esastack.restlight.core.dispatcher.IExceptionHandler;
import io.esastack.restlight.core.filter.Filter;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.HandlerMappingProvider;
import io.esastack.restlight.core.interceptor.HandlerInterceptor;
import io.esastack.restlight.core.interceptor.Interceptor;
import io.esastack.restlight.core.interceptor.InterceptorFactory;
import io.esastack.restlight.core.interceptor.MappingInterceptor;
import io.esastack.restlight.core.interceptor.RouteInterceptor;
import io.esastack.restlight.core.resolver.context.ContextResolverAdapter;
import io.esastack.restlight.core.resolver.context.ContextResolverFactory;
import io.esastack.restlight.core.resolver.converter.StringConverterFactory;
import io.esastack.restlight.core.resolver.exception.ExceptionResolver;
import io.esastack.restlight.core.resolver.factory.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.param.ParamResolverAdapter;
import io.esastack.restlight.core.resolver.param.ParamResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.param.ParamResolverAdviceFactory;
import io.esastack.restlight.core.resolver.param.ParamResolverFactory;
import io.esastack.restlight.core.resolver.param.entity.RequestEntityResolverAdapter;
import io.esastack.restlight.core.resolver.param.entity.RequestEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.param.entity.RequestEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.param.entity.RequestEntityResolverFactory;
import io.esastack.restlight.core.resolver.ret.entity.ResponseEntityResolverAdapter;
import io.esastack.restlight.core.resolver.ret.entity.ResponseEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.ret.entity.ResponseEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.ret.entity.ResponseEntityResolverFactory;
import io.esastack.restlight.core.route.RouteRegistry;
import io.esastack.restlight.core.serialize.HttpBodySerializer;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.core.spi.FilterFactory;
import io.esastack.restlight.core.spi.RouteFilterFactory;

import java.util.Collection;

/**
 * You can regard this {@link MiniConfigurableDeployments} is a proxy of {@link Deployments}. This class is used to
 * help user add some components to the corresponding {@link Restlight}.
 */
public class MiniConfigurableDeployments {

    final Deployments deployments;

    public DeployContext deployContext() {
        return deployments.deployContext();
    }

    public MiniConfigurableDeployments(Deployments deployments) {
        this.deployments = deployments;
    }

    public MiniConfigurableDeployments addHandlerConfigure(HandlerConfigure handlerConfigure) {
        deployments.addHandlerConfigure(handlerConfigure);
        return self();
    }

    public MiniConfigurableDeployments addHandlerConfigures(Collection<? extends HandlerConfigure> handlerConfigures) {
        deployments.addHandlerConfigures(handlerConfigures);
        return self();
    }

    /**
     * Adds {@link Filter}.
     *
     * @param filter filter
     * @return deployments
     */
    public MiniConfigurableDeployments addFilter(Filter filter) {
        deployments.addFilter(filter);
        return self();
    }

    /**
     * Adds {@link Filter}.
     *
     * @param filter filter
     * @return deployments
     */
    public MiniConfigurableDeployments addFilter(FilterFactory filter) {
        deployments.addFilter(filter);
        return self();
    }

    /**
     * Adds {@link Filter}s.
     *
     * @param filters filters
     * @return deployments
     */
    public MiniConfigurableDeployments addFilters(Collection<? extends FilterFactory> filters) {
        deployments.addFilters(filters);
        return self();
    }

    /**
     * Adds {@link RouteFilterFactory}.
     *
     * @param filter route filter factory
     * @return this deployments
     */
    public MiniConfigurableDeployments addRouteFilter(RouteFilterFactory filter) {
        deployments.addRouteFilter(filter);
        return self();
    }

    /**
     * Adds {@link RouteFilterFactory}s.
     *
     * @param filters route filter factories
     * @return this deployments
     */
    public MiniConfigurableDeployments addRouteFilters(Collection<? extends RouteFilterFactory> filters) {
        deployments.addRouteFilters(filters);
        return self();
    }

    /**
     * Adds {@link IExceptionHandler}.
     *
     * @param handler exception handler
     * @return this deployments
     */
    @Internal
    public MiniConfigurableDeployments addExceptionHandler(IExceptionHandler handler) {
        deployments.addExceptionHandler(handler);
        return self();
    }

    /**
     * Adds {@link IExceptionHandler}s.
     *
     * @param handlers exception handlers
     * @return this deployments
     */
    @Internal
    public MiniConfigurableDeployments addExceptionHandlers(Collection<? extends IExceptionHandler> handlers) {
        deployments.addExceptionHandlers(handlers);
        return self();
    }

    /**
     * Adds {@link HandlerMapping}
     */
    public MiniConfigurableDeployments addHandlerMapping(HandlerMapping mapping) {
        deployments.addHandlerMapping(mapping);
        deployments.server();
        return self();
    }

    /**
     * Adds {@link HandlerMapping}
     */
    public MiniConfigurableDeployments addHandlerMappings(Collection<? extends HandlerMapping> mappings) {
        deployments.addHandlerMappings(mappings);
        return self();
    }

    /**
     * Adds {@link HandlerMappingProvider} and {@link HandlerMapping}s returned by this provider will be registered in
     * the {@link RouteRegistry}
     */
    public MiniConfigurableDeployments addHandlerMappingProvider(HandlerMappingProvider provider) {
        deployments.addHandlerMappingProvider(provider);
        return self();
    }

    /**
     * Adds {@link HandlerMappingProvider}s and {@link HandlerMapping}s returned by these providers will be registered
     * in the {@link RouteRegistry}
     *
     * @param providers providers
     * @return this deployments
     */
    public MiniConfigurableDeployments addHandlerMappingProviders(Collection<? extends HandlerMappingProvider>
                                                                          providers) {
        deployments.addHandlerMappingProviders(providers);
        return self();
    }

    /**
     * Adds a controller bean which will be registered in the {@link RouteRegistry}.
     *
     * @param bean an {@link Object} instances.
     * @return this deployments
     */
    public MiniConfigurableDeployments addController(Object bean) {
        deployments.addController(bean);
        return self();
    }

    /**
     * Adds controller beans which will be registered in the {@link RouteRegistry}
     *
     * @param beans {@link Object} instances.
     * @return this deployments
     */
    public MiniConfigurableDeployments addControllers(Collection<?> beans) {
        deployments.addController(beans);
        return self();
    }

    public MiniConfigurableDeployments addController(Class<?> clazz, boolean singleton) {
        deployments.addController(clazz, singleton);
        return self();
    }

    public MiniConfigurableDeployments addControllers(Collection<Class<?>> classes, boolean singleton) {
        deployments.addControllers(classes, singleton);
        return self();
    }

    /**
     * Adds controller advice bean or class.
     *
     * @param beanOrClass an {@link Object} instance or {@link Class}.
     * @return this deployments
     */
    public MiniConfigurableDeployments addControllerAdvice(Object beanOrClass) {
        deployments.addControllerAdvice(beanOrClass);
        return self();
    }

    /**
     * Adds controller advice beans.
     *
     * @param beanOrClasses {@link Object} instances or {@link Class}s.
     * @return this deployments
     */
    public MiniConfigurableDeployments addControllerAdvices(Collection<?> beanOrClasses) {
        deployments.addControllerAdvices(beanOrClasses);
        return self();
    }

    /**
     * Adds a {@link RouteInterceptor}.
     *
     * @param interceptor interceptor
     * @return this deployments
     */
    public MiniConfigurableDeployments addRouteInterceptor(RouteInterceptor interceptor) {
        deployments.addRouteInterceptor(interceptor);
        return self();
    }

    /**
     * Adds {@link RouteInterceptor}s.
     *
     * @param interceptors interceptors
     * @return this deployments
     */
    public MiniConfigurableDeployments addRouteInterceptors(Collection<? extends RouteInterceptor> interceptors) {
        deployments.addRouteInterceptors(interceptors);
        return self();
    }

    /**
     * Adds a {@link HandlerInterceptor}.
     *
     * @param interceptor interceptor
     * @return this deployments
     */
    public MiniConfigurableDeployments addHandlerInterceptor(HandlerInterceptor interceptor) {
        deployments.addHandlerInterceptor(interceptor);
        return self();
    }

    /**
     * Adds {@link HandlerInterceptor}s.
     *
     * @param interceptors interceptors
     * @return this deployments
     */
    public MiniConfigurableDeployments addHandlerInterceptors(Collection<? extends HandlerInterceptor> interceptors) {
        deployments.addHandlerInterceptors(interceptors);
        return self();
    }

    /**
     * Adds a {@link MappingInterceptor}.
     *
     * @param interceptor interceptors
     * @return this deployments
     */
    public MiniConfigurableDeployments addMappingInterceptor(MappingInterceptor interceptor) {
        deployments.addMappingInterceptor(interceptor);
        return self();
    }

    /**
     * Adds {@link MappingInterceptor}s.
     *
     * @param interceptors interceptors
     * @return this deployments
     */
    public MiniConfigurableDeployments addMappingInterceptors(Collection<? extends MappingInterceptor> interceptors) {
        deployments.addMappingInterceptors(interceptors);
        return self();
    }

    /**
     * Adds {@link InterceptorFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param interceptor interceptor
     * @return this deployments
     */
    public MiniConfigurableDeployments addInterceptor(Interceptor interceptor) {
        deployments.addInterceptor(interceptor);
        return self();
    }

    /**
     * Adds {@link InterceptorFactory}s.
     *
     * @param interceptors interceptors
     * @return this deployments
     */
    public MiniConfigurableDeployments addInterceptors(Collection<? extends Interceptor> interceptors) {
        deployments.addInterceptors(interceptors);
        return self();
    }

    /**
     * Adds {@link InterceptorFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param factory factory
     * @return this deployments
     */
    public MiniConfigurableDeployments addInterceptorFactory(InterceptorFactory factory) {
        deployments.addInterceptorFactory(factory);
        return self();
    }

    /**
     * Adds {@link InterceptorFactory}s.
     *
     * @param interceptors interceptors
     * @return this deployments
     */
    public MiniConfigurableDeployments addInterceptorFactories(Collection<? extends InterceptorFactory> interceptors) {
        deployments.addInterceptorFactories(interceptors);
        return self();
    }

    /**
     * Adds {@link StringConverterFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param converter resolver
     * @return this deployments
     */
    public MiniConfigurableDeployments addStringConverter(StringConverterFactory converter) {
        deployments.addStringConverter(converter);
        return self();
    }

    /**
     * Adds {@link StringConverterFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param converters converters
     * @return this deployments
     */
    public MiniConfigurableDeployments addStringConverters(Collection<? extends StringConverterFactory> converters) {
        deployments.addStringConverters(converters);
        return self();
    }

    /**
     * Adds {@link ParamResolverAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     * @return this deployments
     */
    public MiniConfigurableDeployments addParamResolver(ParamResolverAdapter resolver) {
        deployments.addParamResolver(resolver);
        return self();
    }

    /**
     * Adds {@link ParamResolverFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     * @return this deployments
     */
    public MiniConfigurableDeployments addParamResolver(ParamResolverFactory resolver) {
        deployments.addParamResolver(resolver);
        return self();
    }

    /**
     * Adds {@link ParamResolverFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolvers resolvers
     * @return this deployments
     */
    public MiniConfigurableDeployments addParamResolvers(Collection<? extends ParamResolverFactory> resolvers) {
        deployments.addParamResolvers(resolvers);
        return self();
    }

    /**
     * Adds {@link ParamResolverFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     * @return this deployments
     */
    public MiniConfigurableDeployments addParamResolverAdvice(ParamResolverAdviceAdapter advice) {
        deployments.addParamResolverAdvice(advice);
        return self();
    }

    /**
     * Adds {@link ParamResolverAdviceFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     * @return this deployments
     */
    public MiniConfigurableDeployments addParamResolverAdvice(ParamResolverAdviceFactory advice) {
        deployments.addParamResolverAdvice(advice);
        return self();
    }

    /**
     * Adds {@link ParamResolverAdviceFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advices advices
     * @return this deployments
     */
    public MiniConfigurableDeployments addParamResolverAdvices(Collection<? extends ParamResolverAdviceFactory>
                                                                       advices) {
        deployments.addParamResolverAdvices(advices);
        return self();
    }

    /**
     * Adds {@link ContextResolverAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     * @return this deployments
     */
    public MiniConfigurableDeployments addContextResolver(ContextResolverAdapter resolver) {
        deployments.addContextResolver(resolver);
        return self();
    }

    /**
     * Adds {@link ParamResolverFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     * @return this deployments
     */
    public MiniConfigurableDeployments addContextResolver(ContextResolverFactory resolver) {
        deployments.addContextResolver(resolver);
        return self();
    }

    /**
     * Adds {@link ParamResolverFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolvers resolvers
     * @return this deployments
     */
    public MiniConfigurableDeployments addContextResolvers(Collection<? extends ContextResolverFactory> resolvers) {
        deployments.addContextResolvers(resolvers);
        return self();
    }

    /**
     * Adds {@link RequestEntityResolverAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     * @return this deployments
     */
    public MiniConfigurableDeployments addRequestEntityResolver(RequestEntityResolverAdapter resolver) {
        deployments.addRequestEntityResolver(resolver);
        return self();
    }

    /**
     * Adds {@link RequestEntityResolverFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     * @return this deployments
     */
    public MiniConfigurableDeployments addRequestEntityResolver(RequestEntityResolverFactory resolver) {
        deployments.addRequestEntityResolver(resolver);
        return self();
    }

    /**
     * Adds {@link RequestEntityResolverFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolvers resolvers
     * @return this deployments
     */
    public MiniConfigurableDeployments addRequestEntityResolvers(Collection<? extends RequestEntityResolverFactory>
                                                                         resolvers) {
        deployments.addRequestEntityResolvers(resolvers);
        return self();
    }

    /**
     * Adds {@link RequestEntityResolverAdviceAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     * @return this deployments
     */
    public MiniConfigurableDeployments addRequestEntityResolverAdvice(RequestEntityResolverAdviceAdapter advice) {
        deployments.addRequestEntityResolverAdvice(advice);
        return self();
    }

    /**
     * Adds {@link RequestEntityResolverAdviceFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     * @return this deployments
     */
    public MiniConfigurableDeployments addRequestEntityResolverAdvice(RequestEntityResolverAdviceFactory advice) {
        deployments.addRequestEntityResolverAdvice(advice);
        return self();
    }

    /**
     * Adds {@link RequestEntityResolverAdviceFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advices resolvers
     * @return this deployments
     */
    public MiniConfigurableDeployments addRequestEntityResolverAdvices(
            Collection<? extends RequestEntityResolverAdviceFactory> advices) {
        deployments.addRequestEntityResolverAdvices(advices);
        return self();
    }

    public MiniConfigurableDeployments addResponseEntityResolver(ResponseEntityResolverAdapter resolver) {
        deployments.addResponseEntityResolver(resolver);
        return self();
    }

    /**
     * Adds {@link ResponseEntityResolverFactory} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolver resolver
     * @return this deployments
     */
    public MiniConfigurableDeployments addResponseEntityResolver(ResponseEntityResolverFactory resolver) {
        deployments.addResponseEntityResolver(resolver);
        return self();
    }

    /**
     * Adds {@link ResponseEntityResolverFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param resolvers resolvers
     * @return this deployments
     */
    public MiniConfigurableDeployments addResponseEntityResolvers(Collection<? extends ResponseEntityResolverFactory>
                                                                          resolvers) {
        deployments.addResponseEntityResolvers(resolvers);
        return self();
    }

    /**
     * Adds {@link ResponseEntityResolverAdviceAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     * @return this deployments
     */
    public MiniConfigurableDeployments addResponseEntityResolverAdvice(ResponseEntityResolverAdviceAdapter advice) {
        deployments.addResponseEntityResolverAdvice(advice);
        return self();
    }

    /**
     * Adds {@link ResponseEntityResolverAdviceAdapter} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advice advice
     * @return this deployments
     */
    public MiniConfigurableDeployments addResponseEntityResolverAdvice(ResponseEntityResolverAdviceFactory advice) {
        deployments.addResponseEntityResolverAdvice(advice);
        return self();
    }

    /**
     * Adds {@link ResponseEntityResolverAdviceFactory}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param advices resolvers
     * @return this deployments
     */
    public MiniConfigurableDeployments addResponseEntityResolverAdvices(
            Collection<? extends ResponseEntityResolverAdviceFactory> advices) {
        deployments.addResponseEntityResolverAdvices(advices);
        return self();
    }

    /**
     * Adds {@link HttpRequestSerializer} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param requestSerializer requestSerializer
     * @return this deployments
     */
    public MiniConfigurableDeployments addRequestSerializer(HttpRequestSerializer requestSerializer) {
        deployments.addRequestSerializer(requestSerializer);
        return self();
    }

    /**
     * Adds {@link HttpRequestSerializer}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param requestSerializers requestSerializers
     * @return this deployments
     */
    public MiniConfigurableDeployments addRequestSerializers(Collection<? extends HttpRequestSerializer>
                                                                     requestSerializers) {
        deployments.addRequestSerializers(requestSerializers);
        return self();
    }

    /**
     * Adds {@link HttpResponseSerializer} which will be registered in the {@link HandlerResolverFactory}
     *
     * @param responseSerializer responseSerializer
     * @return this deployments
     */
    public MiniConfigurableDeployments addResponseSerializer(HttpResponseSerializer responseSerializer) {
        deployments.addResponseSerializer(responseSerializer);
        return self();
    }

    /**
     * Adds {@link HttpResponseSerializer}s which will be registered in the {@link HandlerResolverFactory}
     *
     * @param responseSerializers responseSerializers
     * @return this deployments
     */
    public MiniConfigurableDeployments addResponseSerializers(Collection<? extends HttpResponseSerializer>
                                                                      responseSerializers) {
        deployments.addResponseSerializers(responseSerializers);
        return self();
    }

    /**
     * Adds {@link HttpBodySerializer} which will be registered in the {@link HandlerResolverFactory} as {@link
     * HttpRequestSerializer} and {@link HttpResponseSerializer}
     *
     * @param serializer serializer
     * @return this deployments
     */
    public MiniConfigurableDeployments addSerializer(HttpBodySerializer serializer) {
        deployments.addSerializer(serializer);
        return self();
    }

    /**
     * Adds {@link HttpBodySerializer}s which will be registered in the {@link HandlerResolverFactory} as {@link
     * HttpRequestSerializer} and {@link HttpResponseSerializer}
     *
     * @param serializers serializers
     * @return this deployments
     */
    public MiniConfigurableDeployments addSerializers(Collection<? extends HttpBodySerializer> serializers) {
        deployments.addSerializers(serializers);
        return self();
    }

    public <T extends Throwable> MiniConfigurableDeployments addExceptionResolver(Class<T> type,
                                                                                  ExceptionResolver<T> resolver) {
        deployments.addExceptionResolver(type, resolver);
        return self();
    }

    protected MiniConfigurableDeployments self() {
        return this;
    }

}

