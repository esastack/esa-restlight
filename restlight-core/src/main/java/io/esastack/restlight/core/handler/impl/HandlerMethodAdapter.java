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
package io.esastack.restlight.core.handler.impl;

import esa.commons.Checks;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.MethodParam;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.method.ResolvableParam;
import io.esastack.restlight.core.method.ResolvableParamPredicate;
import io.esastack.restlight.core.resolver.ContextResolver;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.RequestEntityResolver;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.core.util.RouteUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * An adapter of {@link HandlerMethod} which holds the {@link ParamResolver}s, {@link ContextResolver}s or
 * {@link RequestEntityResolver} to resolve method parameters.
 *
 * @param <H>   generic handler method
 */
public class HandlerMethodAdapter<H extends HandlerMethod> implements HandlerMethod {

    private final HandlerContext<? extends RestlightOptions> context;
    private final H handlerMethod;
    private final ResolvableParam<MethodParam, ResolverWrap>[] methodParamResolvers;
    private final boolean concurrent;

    public HandlerMethodAdapter(HandlerContext<? extends RestlightOptions> context, H handlerMethod) {
        Checks.checkNotNull(context, "context");
        Checks.checkNotNull(handlerMethod, "handlerMethod");
        this.handlerMethod = handlerMethod;
        this.concurrent = RouteUtils.isConcurrent(handlerMethod);
        this.context = context;
        assert context.paramPredicate().isPresent();
        assert context.resolverFactory().isPresent();
        this.methodParamResolvers = mergeResolversOfMethod(handlerMethod, context.paramPredicate().get(),
                context.resolverFactory().get());
    }

    @Override
    public <A extends Annotation> A getMethodAnnotation(Class<A> annotationType) {
        return handlerMethod.getMethodAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> boolean hasMethodAnnotation(Class<A> annotationType) {
        return handlerMethod.hasMethodAnnotation(annotationType);
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
    public Class<? extends HttpResponseSerializer> serializer() {
        return handlerMethod.serializer();
    }

    @Override
    public String toString() {
        return handlerMethod.toString();
    }

    @SuppressWarnings("unchecked")
    private ResolvableParam<MethodParam, ResolverWrap>[] mergeResolversOfMethod(HandlerMethod handlerMethod,
                                                                                ResolvableParamPredicate
                                                                                        resolvable,
                                                                                HandlerResolverFactory factory) {
        MethodParam[] params = handlerMethod.parameters();
        List<ResolvableParam<MethodParam, ResolverWrap>> resolvers = new LinkedList<>();
        for (MethodParam param : params) {
            if (resolvable.test(param)) {
                resolvers.add(getResolverWrap(param, factory));
            }
        }
        return resolvers.toArray(new ResolvableParam[0]);
    }

    <P extends Param> ResolvableParam<P, ResolverWrap> getResolverWrap(P param,
                                                                       HandlerResolverFactory factory) {
        ParamResolver paramResolver = factory.getParamResolver(param);
        if (paramResolver != null) {
            return new ResolvableParam<>(param, new AdvisedParamResolver(paramResolver,
                    factory.getParamResolverAdvices(param, paramResolver)));
        } else {
            ContextResolver contextResolver = factory.getContextResolver(param);
            if (contextResolver != null) {
                return new ResolvableParam<>(param, new ContextResolverWrap(contextResolver));
            } else {
                List<RequestEntityResolver> requestEntityResolvers = factory.getRequestEntityResolvers(param);
                if (requestEntityResolvers.isEmpty()) {
                    throw new IllegalArgumentException("There is no resolver to handle param: ["
                            + param.toString() + "]");
                } else {
                    return new ResolvableParam<>(param, new AdvisedRequestEntityResolver(handlerMethod, param,
                            requestEntityResolvers, factory.getRequestEntityResolverAdvices(handlerMethod)));
                }
            }
        }
    }

    ResolvableParam<MethodParam, ResolverWrap>[] paramResolvers() {
        return methodParamResolvers;
    }

    HandlerContext<? extends RestlightOptions> context() {
        return this.context;
    }

    boolean isConcurrent() {
        return concurrent;
    }

    H handlerMethod() {
        return handlerMethod;
    }
}
