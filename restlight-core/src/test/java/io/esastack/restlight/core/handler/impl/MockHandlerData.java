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
package io.esastack.restlight.core.handler.impl;

import esa.commons.collection.LinkedMultiValueMap;
import esa.commons.collection.MultiValueMap;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.annotation.Intercepted;
import io.esastack.restlight.core.filter.RouteFilter;
import io.esastack.restlight.core.handler.HandlerAdvicesFactory;
import io.esastack.restlight.core.handler.HandlerFactory;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.handler.Handlers;
import io.esastack.restlight.core.handler.method.DefaultResolvableParamPredicate;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.HandlerMethodAdapter;
import io.esastack.restlight.core.handler.method.RouteHandlerMethod;
import io.esastack.restlight.core.handler.method.RouteHandlerMethodImpl;
import io.esastack.restlight.core.handler.method.RouteMethodInfo;
import io.esastack.restlight.core.interceptor.Interceptor;
import io.esastack.restlight.core.interceptor.InterceptorPredicate;
import io.esastack.restlight.core.resolver.context.ContextResolver;
import io.esastack.restlight.core.resolver.exception.ExceptionResolver;
import io.esastack.restlight.core.resolver.factory.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.param.ParamResolver;
import io.esastack.restlight.core.resolver.param.ParamResolverContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockHandlerData {
    private HandlerMapping mapping;
    private RouteMethodInfo methodInfo;
    private DeployContext deployContext;
    private final Method method;
    private final RouteHandlerMethod handlerMethod;
    private HandlerContext context;
    private HandlerResolverFactory resolverFactory;
    private ExceptionResolver<Throwable> exceptionResolver;
    private MultiValueMap<InterceptorPredicate, Interceptor> interceptors;
    private HandlerValueResolver handlerValueResolver;
    private HandlerMethodAdapter handlerMethodAdapter;

    public MockHandlerData() throws Exception {
        method = Subject.class
                .getDeclaredMethod("params", String.class, int.class, List.class);
        handlerMethod = RouteHandlerMethodImpl.of(Subject.class,
                method,
                true,
                null);
        init();
    }

    public MockHandlerData(Class<?> clazz, Method method0) throws Exception {
        method = method0;
        handlerMethod = RouteHandlerMethodImpl.of(clazz,
                method,
                true,
                null);
        init();
    }

    private void init() throws Exception {
        mapping = mock(HandlerMapping.class);
        methodInfo = mock(RouteMethodInfo.class);
        when(mapping.methodInfo()).thenReturn(methodInfo);
        when(methodInfo.handlerMethod()).thenReturn(handlerMethod);
        context = mock(HandlerContext.class);
        resolverFactory = mockResolverFactory(handlerMethod);
        when(context.resolverFactory()).thenReturn(Optional.of(resolverFactory));
        when(context.paramPredicate()).thenReturn(Optional.of(new DefaultResolvableParamPredicate()));
        when(context.handlerAdvicesFactory()).thenReturn(Optional.of(mock(HandlerAdvicesFactory.class)));
        deployContext = mock(DeployContext.class);
        when(deployContext.handlerContexts()).thenReturn(Optional.of(method -> context));
        HandlerFactory handlerFactory = new HandlerFactoryImpl(deployContext,
                mock(Handlers.class));
        when(context.handlerFactory()).thenReturn(Optional.of(handlerFactory));
        List<RouteFilter> filters = new ArrayList<>();
        when(resolverFactory.getRouteFilters(handlerMethod)).thenReturn(filters);
        handlerValueResolver = mock(HandlerValueResolver.class);
        interceptors = new LinkedMultiValueMap<>();
        exceptionResolver = mock(ExceptionResolver.class);
        handlerMethodAdapter = new HandlerMethodAdapter<>(context, handlerMethod);
    }

    public HandlerMapping mapping() {
        return mapping;
    }

    public RouteMethodInfo methodInfo() {
        return methodInfo;
    }

    public Method method() {
        return method;
    }

    public RouteHandlerMethod handlerMethod() {
        return handlerMethod;
    }

    public HandlerContext context() {
        return context;
    }

    public HandlerResolverFactory resolverFactory() {
        return resolverFactory;
    }

    public ExceptionResolver<Throwable> exceptionResolver() {
        return exceptionResolver;
    }

    public MultiValueMap<InterceptorPredicate, Interceptor> interceptors() {
        return interceptors;
    }

    public HandlerValueResolver handlerValueResolver() {
        return handlerValueResolver;
    }

    public HandlerMethodAdapter handlerMethodAdapter() {
        return handlerMethodAdapter;
    }

    private HandlerResolverFactory mockResolverFactory(HandlerMethod handlerMethod) throws Exception {
        final HandlerResolverFactory resolverFactory = mock(HandlerResolverFactory.class);
        ContextResolver p1Resolver = mock(ContextResolver.class);

        if (handlerMethod.beanType() == Subject.class) {
            when(resolverFactory.getContextResolver(handlerMethod.parameters()[0]))
                    .thenReturn(p1Resolver);
            when(p1Resolver.resolve(any())).thenReturn(null);

            ParamResolver p2Resolver = mock(ParamResolver.class);
            when(resolverFactory.getNoEntityParamResolver(handlerMethod.parameters()[1]))
                    .thenReturn(p2Resolver);
            when(p2Resolver.resolve(any())).thenReturn(null);

            List<ParamResolver> p3Resolver = mock(List.class);
            when(resolverFactory.getEntityParamResolvers(handlerMethod.parameters()[2]))
                    .thenReturn(p3Resolver);
        }
        return resolverFactory;
    }

    @Intercepted(false)
    private static class Subject {

        @Intercepted(false)
        CompletableFuture<Object> params(String p0, int p1, List<String> p2) {
            return CompletableFuture.completedFuture(null);
        }

    }
}
