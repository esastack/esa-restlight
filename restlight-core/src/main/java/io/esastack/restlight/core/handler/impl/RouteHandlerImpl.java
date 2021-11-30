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

import esa.commons.StringUtils;
import io.esastack.restlight.core.handler.HandlerInvoker;
import io.esastack.restlight.core.handler.RouteHandler;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.RouteHandlerMethod;

/**
 * Default implementation of {@link RouteHandler}.
 */
public class RouteHandlerImpl extends HandlerImpl implements RouteHandler {

    public RouteHandlerImpl(RouteHandlerMethod handlerMethod, Object bean) {
        super(handlerMethod, bean);
    }

    public RouteHandlerImpl(HandlerMethod handlerMethod, Object bean, HandlerInvoker invoker) {
        super(handlerMethod, bean, invoker);
    }

    @Override
    public RouteHandlerMethod handlerMethod() {
        return (RouteHandlerMethod) super.handlerMethod();
    }

    @Override
    public String toString() {
        return StringUtils.concat("{Controller => ", this.handlerMethod().beanType().getName(),
                ", Method => ", this.handlerMethod().method().toGenericString(), "}");
    }

}
