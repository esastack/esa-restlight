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
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.handler.Handler;
import io.esastack.restlight.core.handler.HandlerInvoker;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.MethodParam;

public class ExceptionHandlerExecution extends AbstractExecutionHandler<HandlerMethodAdapter<HandlerMethod>> {

    private final Handler handler;
    private final Throwable th;

    public ExceptionHandlerExecution(HandlerValueResolver handlerResolver,
                                     Handler handler,
                                     HandlerMethodAdapter<HandlerMethod> handlerMethod,
                                     Throwable th) {
        super(handlerResolver, handlerMethod);
        Checks.checkNotNull(handler, "handler");
        this.handler = handler;
        this.th = th;
    }

    @Override
    protected Object resolveFixedArg(MethodParam parameter, RequestContext context) {
        if (parameter.type().isInstance(th)) {
            return th;
        }
        return super.resolveFixedArg(parameter, context);
    }

    @Override
    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
        return this.handler.bean();
    }

    @Override
    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
        return this.handler;
    }
}

