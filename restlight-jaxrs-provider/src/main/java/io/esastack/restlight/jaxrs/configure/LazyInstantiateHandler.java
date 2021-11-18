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

import esa.commons.Checks;
import esa.commons.reflect.BeanUtils;
import esa.commons.reflect.ReflectionUtils;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.method.ConstructorParam;
import io.esastack.restlight.core.method.FieldParam;
import io.esastack.restlight.core.method.MethodParam;
import io.esastack.restlight.core.method.ResolvableParam;
import io.esastack.restlight.core.method.ResolvableParamPredicate;
import io.esastack.restlight.core.resolver.ContextResolver;
import io.esastack.restlight.server.bootstrap.WebServerException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;

import static io.esastack.restlight.core.util.ConstructorUtils.extractResolvable;

class LazyInstantiateHandler implements InvocationHandler {

    private final DeployContext<? extends RestlightOptions> deployContext;
    private final ResolvableParamPredicate predicate;
    private final Class<?> clazz;
    private final Constructor<?> constructor;
    private Object target;
    private boolean injected;

    LazyInstantiateHandler(Class<?> clazz,
                           DeployContext<? extends RestlightOptions> deployContext) {
        Checks.checkNotNull(clazz, "clazz");
        Checks.checkNotNull(deployContext, "deployContext");
        Checks.checkState(deployContext.paramPredicate().isPresent());
        this.clazz = clazz;
        this.predicate = deployContext.paramPredicate().get();
        this.constructor = Objects.requireNonNull(extractResolvable(clazz, predicate));
        this.deployContext = deployContext;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (target == null) {
            target = doInstantiate();
            resolveInjectableFieldsAndSetters(target);
        }
        return method.invoke(target, args);
    }

    protected Object doInstantiate() throws Exception {
        if (!deployContext.resolverFactory().isPresent()) {
            throw new IllegalStateException("The HandlerResolverFactory hasn't instantiated when begin to " +
                    " instantiate class: [" + clazz + "]");
        }
        final Object[] consArgs = new Object[constructor.getParameterCount()];
        ResolvableParam<ConstructorParam, ContextResolver>[] consParams = ResolverUtils
                .contextResolversOfCons(constructor, predicate, deployContext.resolverFactory().get());
        int index = 0;
        for (ResolvableParam<ConstructorParam, ContextResolver> param : consParams) {
            consArgs[index++] = param.resolver().resolve(param.param(), deployContext);
        }
        return constructor.newInstance(consArgs);
    }

    private void resolveInjectableFieldsAndSetters(Object target) {
        if (injected) {
            return;
        }
        if (!deployContext.resolverFactory().isPresent()) {
            throw new IllegalStateException("The HandlerResolverFactory hasn't instantiated when begin to " +
                    " instantiate class: [" + clazz + "]");
        }

        ResolvableParam<MethodParam, ContextResolver>[] setterParams = ResolverUtils
                .contextResolversOfSetter(clazz, predicate, deployContext.resolverFactory().get());
        for (ResolvableParam<MethodParam, ContextResolver> resolvable : setterParams) {
            final MethodParam param = resolvable.param();
            //resolve args with resolver
            if (resolvable.resolver() != null) {
                //it may return a null value
                try {
                    Object arg = resolvable.resolver().resolve(param, deployContext);
                    ReflectionUtils.invokeMethod(param.method(), target, arg);
                } catch (Exception e) {
                    //wrap exception
                    throw WebServerException.wrap(e);
                }
            }
        }

        ResolvableParam<FieldParam, ContextResolver>[] fieldParams = ResolverUtils
                .contextResolversOfField(clazz, predicate, deployContext.resolverFactory().get());
        for (ResolvableParam<FieldParam, ContextResolver> resolvable : fieldParams) {
            final FieldParam param = resolvable.param();
            //resolve args with resolver
            if (resolvable.resolver() != null) {
                try {
                    BeanUtils.setFieldValue(target, param.name(),
                            resolvable.resolver().resolve(param, deployContext));
                } catch (Exception e) {
                    //wrap exception
                    throw WebServerException.wrap(e);
                }
            }
        }
        this.injected = true;
    }
}

