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

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.handler.HandlerInvoker;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.interceptor.InternalInterceptor;
import io.esastack.restlight.core.method.HandlerMethod;

import java.util.List;

public class SingletonRouteHandler extends AbstractRouteHandler {

    private final InvocableRouteMethodAdapter invocableMethod;

    public SingletonRouteHandler(DeployContext<? extends RestlightOptions> deployContext,
                                 InvocableRouteMethodAdapter invocableMethod,
                                 HandlerValueResolver handlerResolver,
                                 List<InternalInterceptor> interceptors) {
        super(deployContext, invocableMethod, handlerResolver, interceptors);
        this.invocableMethod = invocableMethod;
    }

    @Override
    protected Object resolveBean(DeployContext<? extends RestlightOptions> deployContext,
                                 HandlerMethod handler, RequestContext context) {
        return invocableMethod.bean();
    }

    @Override
    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object bean) {
        return invocableMethod.handlerInvoker();
    }
}

