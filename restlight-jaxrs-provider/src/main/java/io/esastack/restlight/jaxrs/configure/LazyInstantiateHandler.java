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
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.handler.HandlerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

class LazyInstantiateHandler implements InvocationHandler {

    private final HandlerFactory factory;
    private final Class<?> clazz;
    private Object target;
    private boolean initialized;

    LazyInstantiateHandler(Class<?> clazz,
                           DeployContext<? extends RestlightOptions> context) {
        Checks.checkNotNull(clazz, "clazz");
        Checks.checkNotNull(context, "context");
        assert context.handlerFactory().isPresent();
        this.clazz = clazz;
        this.factory = context.handlerFactory().get();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(getInstanceThenInit(), args);
    }

    private Object getInstanceThenInit() {
        Object instance = getInstance();
        if (initialized) {
            factory.doInit(instance, null);
            initialized = true;
        }
        return instance;
    }

    protected Object getInstance() {
        if (target == null) {
            target = factory.instantiate(clazz, null);
        }
        return target;
    }
}

