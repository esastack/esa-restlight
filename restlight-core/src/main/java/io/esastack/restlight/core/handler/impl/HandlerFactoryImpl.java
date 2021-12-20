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
package io.esastack.restlight.core.handler.impl;

import esa.commons.Checks;
import esa.commons.ClassUtils;
import esa.commons.StringUtils;
import esa.commons.reflect.BeanUtils;
import esa.commons.reflect.ReflectionUtils;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.configure.Handlers;
import io.esastack.restlight.core.handler.HandlerContexts;
import io.esastack.restlight.core.handler.HandlerFactory;
import io.esastack.restlight.core.method.ConstructorParam;
import io.esastack.restlight.core.method.ConstructorParamImpl;
import io.esastack.restlight.core.method.FieldParam;
import io.esastack.restlight.core.method.FieldParamImpl;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.HandlerMethodImpl;
import io.esastack.restlight.core.method.MethodParam;
import io.esastack.restlight.core.method.MethodParamImpl;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.method.ResolvableParam;
import io.esastack.restlight.core.method.ResolvableParamPredicate;
import io.esastack.restlight.core.resolver.ContextResolver;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.util.ConstructorUtils;
import io.esastack.restlight.server.bootstrap.WebServerException;
import io.esastack.restlight.server.context.RequestContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerFactoryImpl implements HandlerFactory, HandlerContexts {

    private final ConcurrentHashMap<HandlerMethod, HandlerContext<?>> contexts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, ResolvableHandler> resolvableHandlers = new ConcurrentHashMap<>();
    private final HandlerContext<? extends RestlightOptions> defaultContext;
    private final Handlers handlers;

    public HandlerFactoryImpl(DeployContext<? extends RestlightOptions> defaultContext, Handlers handlers) {
        Checks.checkNotNull(defaultContext, "defaultContext");
        Checks.checkNotNull(handlers, "handlers");
        this.defaultContext = new HandlerContext<>(defaultContext);
        this.handlers = handlers;
    }

    @Override
    public Object instantiate(Class<?> clazz, RequestContext context) {
        return doInstantiate(clazz, getOrDefaultContext(clazz, null), context);
    }

    @Override
    public Object instantiate(Class<?> clazz, Method method, RequestContext context) {
        return doInstantiate(clazz, getOrDefaultContext(clazz, method), context);
    }

    @Override
    public Object getInstance(Class<?> clazz, RequestContext context) {
        for (Object singleton : handlers.getSingletons()) {
            if (ClassUtils.getUserType(singleton).equals(clazz)) {
                return singleton;
            }
        }

        // no way but to instantiate one
        return instantiate(clazz, context);
    }

    @Override
    public Object getInstance(Class<?> clazz, Method method, RequestContext context) {
        for (Object singleton : handlers.getSingletons()) {
            if (ClassUtils.getUserType(singleton).equals(clazz)) {
                return singleton;
            }
        }

        // no way but to instantiate one
        return instantiate(clazz, method, context);
    }

    @Override
    public void doInit(Object instance, RequestContext context) {
        Class<?> userType = ClassUtils.getUserType(instance);
        doInit0(instance, userType, getOrDefaultContext(userType, null), context);
    }

    @Override
    public void doInit(Object instance, Method method, RequestContext context) {
        Class<?> userType = ClassUtils.getUserType(instance);
        doInit0(instance, userType, getOrDefaultContext(userType, method), context);
    }

    @Override
    public void addContext(HandlerMethod handlerMethod, HandlerContext<? extends RestlightOptions> context) {
        this.contexts.putIfAbsent(HandlerMethodImpl.of(handlerMethod.beanType(), handlerMethod.method()), context);
    }

    @Override
    public HandlerContext<? extends RestlightOptions> getContext(HandlerMethod handlerMethod) {
        return this.contexts.get(handlerMethod);
    }

    protected Object doInstantiate(Class<?> clazz, HandlerContext<? extends RestlightOptions> handlerCtx,
                                   RequestContext context) {
        final ResolvableHandler resolvable = getResolvableHandler(clazz, handlerCtx);
        final ResolvableParam<ConstructorParam, ResolverWrap>[] consParams = resolvable.consParamResolvers;
        final Object[] args = new Object[consParams.length];
        for (int i = 0; i < consParams.length; i++) {
            ResolvableParam<ConstructorParam, ResolverWrap> resolvable0 = consParams[i];
            ConstructorParam param = resolvable0.param();
            //resolve args with resolver
            if (resolvable0.resolver() != null) {
                //it may return a null value
                try {
                    args[i] = resolvable0.resolver().resolve(handlerCtx, param, context);
                } catch (Exception e) {
                    //wrap exception
                    throw WebServerException.wrap(e);
                }
                continue;
            }
            if (args[i] == null) {
                throw WebServerException.badRequest(
                        StringUtils.concat("Could not resolve constructor parameter at index ",
                                String.valueOf(param.index()), " in ", resolvable.constructor +
                                        ": No suitable resolver for argument of type '",
                                param.type().getName(), "'"));
            }
        }

        try {
            return resolvable.constructor.newInstance(args);
        } catch (Throwable th) {
            throw WebServerException.badRequest("Could not instantiate class: [" + clazz + "]",
                    th);
        }
    }

    protected void doInit0(Object instance, Class<?> clazz, HandlerContext<? extends RestlightOptions> handlerCtx,
                           RequestContext context) {
        final ResolvableHandler resolvable = getResolvableHandler(clazz, handlerCtx);

        // set fields
        for (ResolvableParam<FieldParam, ResolverWrap> r : resolvable.fieldParamResolvers) {
            FieldParam param = r.param();
            //resolve args with resolver
            if (r.resolver() != null) {
                //it may return a null value
                try {
                    BeanUtils.setFieldValue(instance, param.name(),
                            r.resolver().resolve(handlerCtx, param, context));
                } catch (Exception e) {
                    //wrap exception
                    throw WebServerException.wrap(e);
                }
            }
        }

        // set methods
        for (ResolvableParam<MethodParam, ResolverWrap> r : resolvable.setterParamResolvers) {
            MethodParam param = r.param();
            //resolve args with resolver
            if (r.resolver() != null) {
                //it may return a null value
                try {
                    Object arg = r.resolver().resolve(handlerCtx, param, context);
                    ReflectionUtils.invokeMethod(param.method(), instance, arg);
                } catch (Exception e) {
                    //wrap exception
                    throw WebServerException.wrap(e);
                }
            }
        }
    }

    private ResolvableHandler getResolvableHandler(Class<?> clazz, HandlerContext<? extends RestlightOptions> context) {
        return resolvableHandlers.computeIfAbsent(clazz, clz -> new ResolvableHandler(clazz, context));
    }

    private HandlerContext<? extends RestlightOptions> getOrDefaultContext(Class<?> clazz, Method method) {
        if (clazz == null || method == null) {
            return defaultContext;
        }
        HandlerContext<? extends RestlightOptions> context = getContext(HandlerMethodImpl.of(clazz, method));
        if (context != null) {
            return context;
        } else {
            return defaultContext;
        }
    }

    private static class ResolvableHandler {

        private final Constructor<?> constructor;
        private final ResolvableParam<ConstructorParam, ResolverWrap>[] consParamResolvers;
        private final ResolvableParam<MethodParam, ResolverWrap>[] setterParamResolvers;
        private final ResolvableParam<FieldParam, ResolverWrap>[] fieldParamResolvers;

        private ResolvableHandler(Class<?> clazz, HandlerContext<? extends RestlightOptions> context) {
            assert context.paramPredicate().isPresent();
            assert context.resolverFactory().isPresent();
            ResolvableParamPredicate resolvable = context.paramPredicate().get();
            HandlerResolverFactory resolverFactory = context.resolverFactory().get();
            this.constructor = Objects.requireNonNull(ConstructorUtils.extractResolvable(clazz, resolvable));
            ReflectionUtils.makeConstructorAccessible(this.constructor);
            this.consParamResolvers = mergeConsParamResolvers(constructor, resolvable, resolverFactory);
            this.setterParamResolvers = mergeSetterParamResolvers(clazz, resolvable, resolverFactory);
            this.fieldParamResolvers = mergeFieldParamResolvers(clazz, resolvable, resolverFactory);
        }

        @SuppressWarnings("unchecked")
        private ResolvableParam<ConstructorParam, ResolverWrap>[] mergeConsParamResolvers(Constructor<?> constructor,
                                                                                          ResolvableParamPredicate
                                                                                                    resolvable,
                                                                                          HandlerResolverFactory
                                                                                                    factory) {
            List<ResolvableParam<ConstructorParam, ResolverWrap>> resolvers = new LinkedList<>();
            for (int i = 0; i < constructor.getParameterCount(); i++) {
                ConstructorParam param = new ConstructorParamImpl(constructor, i);
                if (!resolvable.test(param)) {
                    continue;
                }
                resolvers.add(getResolverWrap(param, factory));
            }
            return resolvers.toArray(new ResolvableParam[0]);
        }

        @SuppressWarnings("unchecked")
        private ResolvableParam<MethodParam, ResolverWrap>[] mergeSetterParamResolvers(Class<?> clazz,
                                                                                       ResolvableParamPredicate
                                                                                                 resolvable,
                                                                                       HandlerResolverFactory
                                                                                                 factory) {
            List<ResolvableParam<MethodParam, ResolverWrap>> resolvers = new LinkedList<>();
            ReflectionUtils.getAllDeclaredMethods(clazz).stream()
                    .filter(ReflectionUtils::isSetter)
                    .forEach(m -> {
                        MethodParam param = new MethodParamImpl(m, 0);
                        if (resolvable.test(param)) {
                            resolvers.add(getResolverWrap(param, factory));
                        }
                    });
            return resolvers.toArray(new ResolvableParam[0]);
        }

        @SuppressWarnings("unchecked")
        private ResolvableParam<FieldParam, ResolverWrap>[] mergeFieldParamResolvers(Class<?> clazz,
                                                                                     ResolvableParamPredicate
                                                                                             resolvable,
                                                                                     HandlerResolverFactory
                                                                                               factory) {
            List<ResolvableParam<FieldParam, ResolverWrap>> resolvers = new LinkedList<>();
            ReflectionUtils.getAllDeclaredFields(clazz)
                    .forEach(f -> {
                        FieldParam param = new FieldParamImpl(f);
                        if (resolvable.test(param)) {
                            resolvers.add(getResolverWrap(param, factory));
                        }
                    });
            return resolvers.toArray(new ResolvableParam[0]);
        }

        private <P extends Param> ResolvableParam<P, ResolverWrap> getResolverWrap(P param,
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
                    throw new IllegalArgumentException("There is no resolver to handle [" + param.toString() + "]");
                }
            }
        }

    }
}

