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
import esa.commons.StringUtils;
import io.esastack.restlight.core.handler.Handler;
import io.esastack.restlight.core.handler.HandlerInvoker;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.server.context.RequestContext;

/**
 * Default implementation of {@link Handler}.
 */
public class HandlerImpl implements Handler {

    private final HandlerMethod handlerMethod;
    private final Object object;
    private final HandlerInvoker invoker;

    public HandlerImpl(HandlerMethod handlerMethod, Object bean) {
        this(handlerMethod, bean, new HandlerInvokerImpl(handlerMethod, bean));
    }

    public HandlerImpl(HandlerMethod handlerMethod, Object bean, HandlerInvoker invoker) {
        Checks.checkNotNull(handlerMethod, "handlerMethod");
        Checks.checkNotNull(bean, "bean");
        Checks.checkNotNull(invoker, "invoker");
        this.handlerMethod = handlerMethod;
        this.object = bean;
        this.invoker = invoker;
    }

    @Override
    public Object bean() {
        return object;
    }

    @Override
    public Object invoke(RequestContext context, Object[] args) throws Throwable {
        return invoker.invoke(context, args);
    }

    @Override
    public HandlerMethod handlerMethod() {
        return handlerMethod;
    }

    @Override
    public String toString() {
        return StringUtils.concat("{Handler => ", handlerMethod.beanType().getName(), ", Method => ",
                handlerMethod.method().toGenericString(), "}");
    }
}
