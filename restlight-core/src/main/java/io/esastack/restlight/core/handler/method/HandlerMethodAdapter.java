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
package io.esastack.restlight.core.handler.method;

import esa.commons.Checks;
import io.esastack.restlight.core.resolver.param.AdvisedHttpParamResolver;
import io.esastack.restlight.core.resolver.entity.request.AdvisedRequestEntityParamResolver;
import io.esastack.restlight.core.handler.impl.HandlerContext;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.context.ContextResolver;
import io.esastack.restlight.core.resolver.context.ContextParamResolver;
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolver;
import io.esastack.restlight.core.resolver.factory.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.param.HttpParamResolver;
import io.esastack.restlight.core.util.RouteUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * An adapter of {@link HandlerMethod} which holds the {@link HttpParamResolver}s, {@link ContextResolver}s or
 * {@link RequestEntityResolver} to resolve method parameters.
 *
 * @param <H> generic handler method
 */
public class HandlerMethodAdapter<H extends HandlerMethod> implements HandlerMethod {

    private final HandlerContext context;
    private final H handlerMethod;
    private final ResolvableParam<MethodParam, ParamResolver>[] paramResolvers;
    private final boolean concurrent;

    public HandlerMethodAdapter(HandlerContext context, H handlerMethod) {
        Checks.checkNotNull(context, "context");
        Checks.checkNotNull(handlerMethod, "handlerMethod");
        this.handlerMethod = handlerMethod;
        this.concurrent = RouteUtils.isConcurrent(handlerMethod);
        this.context = context;
        this.paramResolvers = buildParamResolvers(handlerMethod,
                context.paramPredicate().orElseThrow(() -> new IllegalStateException("paramPredicate is null")),
                context.resolverFactory().orElseThrow(() -> new IllegalStateException("resolverFactory is null")));
        // binds ResponseEntityResolvers and ResponseEntityResolverAdvices to current handler method timely.
        if (context.resolverFactory().isPresent()) {
            context.resolverFactory().get().getResponseEntityResolvers(handlerMethod);
            context.resolverFactory().get().getResponseEntityResolverAdvices(handlerMethod);
        }
    }

    @Override
    public <A extends Annotation> A getMethodAnnotation(Class<A> annotationType, boolean recursive) {
        return handlerMethod.getMethodAnnotation(annotationType, recursive);
    }

    @Override
    public <A extends Annotation> A getClassAnnotation(Class<A> annotationType, boolean recursive) {
        return handlerMethod.getClassAnnotation(annotationType, recursive);
    }

    @Override
    public Class<?> beanType() {
        return handlerMethod.beanType();
    }

    @Override
    public Method method() {
        return handlerMethod.method();
    }

    @Override
    public MethodParam[] parameters() {
        return handlerMethod.parameters();
    }

    @Override
    public String toString() {
        return handlerMethod.toString();
    }

    @SuppressWarnings("unchecked")
    private ResolvableParam<MethodParam, ParamResolver>[] buildParamResolvers(HandlerMethod handlerMethod,
                                                                              ResolvableParamPredicate
                                                                                        resolvable,
                                                                              HandlerResolverFactory factory) {
        MethodParam[] params = handlerMethod.parameters();
        List<ResolvableParam<MethodParam, ParamResolver>> resolvers = new LinkedList<>();
        for (MethodParam param : params) {
            if (resolvable.test(param)) {
                resolvers.add(getResolverWrap(param, factory));
            }
        }
        return resolvers.toArray(new ResolvableParam[0]);
    }

    <P extends Param> ResolvableParam<P, ParamResolver> getResolverWrap(P param,
                                                                        HandlerResolverFactory factory) {
        // get fix resolver
        ResolvableParam<P, ParamResolver> fixedResolverWrap = getFixedResolverWrap(param, factory);
        if (fixedResolverWrap != null) {
            return fixedResolverWrap;
        }

        ContextResolver contextResolver = factory.getContextResolver(param);
        if (contextResolver != null) {
            return new ResolvableParam<>(param, new ContextParamResolver(contextResolver));
        } else {
            HttpParamResolver httpParamResolver = factory.getParamResolver(param);
            if (httpParamResolver != null) {
                return new ResolvableParam<>(param, new AdvisedHttpParamResolver(httpParamResolver,
                        factory.getParamResolverAdvices(param, httpParamResolver)));
            } else {
                List<RequestEntityResolver> requestEntityResolvers = factory.getRequestEntityResolvers(param);
                if (requestEntityResolvers.isEmpty()) {
                    throw new IllegalArgumentException("There is no suitable resolver to handle param: ["
                            + param.toString() + "]");
                } else {
                    return new ResolvableParam<>(param, new AdvisedRequestEntityParamResolver(param,
                            requestEntityResolvers, factory.getRequestEntityResolverAdvices(param)));
                }
            }
        }
    }

    /**
     * provide the custom fixed {@link ResolvableParam}.
     *
     * @param param param
     * @param factory factory
     * @return custom {@link ResolvableParam}
     */
    protected <P extends Param> ResolvableParam<P, ParamResolver> getFixedResolverWrap(P param,
                                                                                       HandlerResolverFactory factory) {
        return null;
    }

    public ResolvableParam<MethodParam, ParamResolver>[] paramResolvers() {
        return paramResolvers;
    }

    public HandlerContext context() {
        return this.context;
    }

    public boolean isConcurrent() {
        return concurrent;
    }

    public H handlerMethod() {
        return handlerMethod;
    }
}
