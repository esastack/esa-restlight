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
package io.esastack.restlight.jaxrs.configure;

import esa.commons.reflect.BeanUtils;
import esa.commons.reflect.ReflectionUtils;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.configure.Handlers;
import io.esastack.restlight.core.handler.impl.HandlerContext;
import io.esastack.restlight.core.handler.impl.HandlerFactoryImpl;
import io.esastack.restlight.core.method.ConstructorParam;
import io.esastack.restlight.core.method.ConstructorParamImpl;
import io.esastack.restlight.core.method.FieldParam;
import io.esastack.restlight.core.method.FieldParamImpl;
import io.esastack.restlight.core.method.MethodParam;
import io.esastack.restlight.core.method.MethodParamImpl;
import io.esastack.restlight.core.method.ResolvableParam;
import io.esastack.restlight.core.method.ResolvableParamPredicate;
import io.esastack.restlight.core.resolver.ContextResolver;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.util.ConstructorUtils;
import io.esastack.restlight.server.bootstrap.WebServerException;
import io.esastack.restlight.server.context.RequestContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class JaxrsHandlerFactory extends HandlerFactoryImpl {

    private final ConcurrentHashMap<Class<?>, ResolvableProvider> resolvableProviders = new ConcurrentHashMap<>();

    public JaxrsHandlerFactory(DeployContext<? extends RestlightOptions> defaultContext, Handlers handlers) {
        super(defaultContext, handlers);
    }

    @Override
    protected Object doInstantiate(Class<?> clazz, HandlerContext<? extends RestlightOptions> handlerCtx,
                                   RequestContext context) {
        if (context != null) {
            return super.doInstantiate(clazz, handlerCtx, context);
        } else {
            final ResolvableProvider resolvable = getResolvableProvider(clazz, handlerCtx);
            Object[] consArgs = new Object[resolvable.constructor.getParameterCount()];
            ResolvableParam<ConstructorParam, ContextResolver>[] consParams = resolvable.consParamResolvers;
            int index = 0;
            for (ResolvableParam<ConstructorParam, ContextResolver> param : consParams) {
                try {
                    consArgs[index++] = param.resolver().resolve(param.param(), handlerCtx);
                } catch (Throwable th) {
                    //wrap exception
                    throw WebServerException.wrap(th);
                }
            }
            try {
                return resolvable.constructor.newInstance(consArgs);
            } catch (InvocationTargetException ex) {
                throw new IllegalStateException("Could not instantiate provider class: [" + clazz + "]",
                        ex.getTargetException());
            } catch (Exception ex) {
                throw new IllegalStateException("Could not instantiate provider class: [" + clazz + "]", ex);
            }
        }
    }

    @Override
    protected void doInit0(Object instance, Class<?> clazz, HandlerContext<? extends RestlightOptions> handlerCtx,
                           RequestContext context) {
        if (context != null) {
            super.doInit0(instance, clazz, handlerCtx, context);
        } else {
            final ResolvableProvider resolvable = getResolvableProvider(clazz, handlerCtx);
            for (ResolvableParam<MethodParam, ContextResolver> r : resolvable.setterParamResolvers) {
                MethodParam param = r.param();
                //resolve args with resolver
                if (r.resolver() != null) {
                    //it may return a null value
                    try {
                        Object arg = r.resolver().resolve(param, handlerCtx);
                        ReflectionUtils.invokeMethod(param.method(), instance, arg);
                    } catch (InvocationTargetException ex) {
                        throw new IllegalStateException("Failed to invoke method: [" + param.method() + "]",
                                ex.getTargetException());
                    } catch (Exception ex) {
                        throw WebServerException.wrap(ex);
                    }
                }
            }

            for (ResolvableParam<FieldParam, ContextResolver> r : resolvable.fieldParamResolvers) {
                final FieldParam param = r.param();
                //resolve args with resolver
                if (r.resolver() != null) {
                    try {
                        BeanUtils.setFieldValue(instance, param.name(),
                                r.resolver().resolve(param, handlerCtx));
                    } catch (Exception e) {
                        //wrap exception
                        throw WebServerException.wrap(e);
                    }
                }
            }
        }
    }

    private ResolvableProvider getResolvableProvider(Class<?> clazz,
                                                     DeployContext<? extends RestlightOptions> context) {
        return resolvableProviders.computeIfAbsent(clazz, clz -> new ResolvableProvider(clazz, context));
    }

    private static class ResolvableProvider {

        private final Constructor<?> constructor;
        private final ResolvableParam<ConstructorParam, ContextResolver>[] consParamResolvers;
        private final ResolvableParam<MethodParam, ContextResolver>[] setterParamResolvers;
        private final ResolvableParam<FieldParam, ContextResolver>[] fieldParamResolvers;

        private ResolvableProvider(Class<?> clazz, DeployContext<? extends RestlightOptions> context) {
            assert context.paramPredicate().isPresent();
            assert context.resolverFactory().isPresent();
            ResolvableParamPredicate resolvable = context.paramPredicate().get();
            HandlerResolverFactory resolverFactory = context.resolverFactory().get();
            this.constructor = Objects.requireNonNull(ConstructorUtils.extractResolvable(clazz, resolvable));
            this.consParamResolvers = contextResolversOfCons(constructor, resolvable, resolverFactory);
            this.setterParamResolvers = contextResolversOfSetter(clazz, resolvable, resolverFactory);
            this.fieldParamResolvers = contextResolversOfField(clazz, resolvable, resolverFactory);
        }

        @SuppressWarnings("unchecked")
        private ResolvableParam<ConstructorParam, ContextResolver>[] contextResolversOfCons(Constructor<?> constructor,
                                                                                           ResolvableParamPredicate
                                                                                                   predicate,
                                                                                           HandlerResolverFactory
                                                                                                   factory) {
            List<ResolvableParam<ConstructorParam, ContextResolver>> params = new LinkedList<>();
            for (int i = 0; i < constructor.getParameterCount(); i++) {
                ConstructorParam param = new ConstructorParamImpl(constructor, i);
                if (predicate.test(param)) {
                    params.add(new ResolvableParam<>(param, factory.getContextResolver(param)));
                }
            }
            return params.toArray(new ResolvableParam[0]);
        }

        @SuppressWarnings("unchecked")
        private ResolvableParam<MethodParam, ContextResolver>[] contextResolversOfSetter(Class<?> clazz,
                                                                                         ResolvableParamPredicate
                                                                                                 predicate,
                                                                                         HandlerResolverFactory
                                                                                                         factory) {

            List<ResolvableParam<MethodParam, ContextResolver>> params = new LinkedList<>();
            ReflectionUtils.getAllDeclaredMethods(clazz).stream()
                    .filter(ReflectionUtils::isSetter)
                    .forEach(m -> {
                        MethodParam param = new MethodParamImpl(m, 0);
                        if (predicate.test(param)) {
                            params.add(new ResolvableParam<>(param, factory.getContextResolver(param)));
                        }
                    });
            return params.toArray(new ResolvableParam[0]);
        }

        @SuppressWarnings("unchecked")
        private ResolvableParam<FieldParam, ContextResolver>[] contextResolversOfField(Class<?> clazz,
                                                                                       ResolvableParamPredicate
                                                                                               predicate,
                                                                                       HandlerResolverFactory
                                                                                                       factory) {
            List<ResolvableParam<FieldParam, ContextResolver>> params = new LinkedList<>();
            ReflectionUtils.getAllDeclaredFields(clazz)
                    .forEach(f -> {
                        FieldParam param = new FieldParamImpl(f);
                        if (predicate.test(param)) {
                            params.add(new ResolvableParam<>(param, factory.getContextResolver(param)));
                        }
                    });
            return params.toArray(new ResolvableParam[0]);
        }
    }
}

