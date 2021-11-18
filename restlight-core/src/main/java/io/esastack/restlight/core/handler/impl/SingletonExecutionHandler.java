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
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.handler.Handler;
import io.esastack.restlight.core.handler.HandlerAdvice;
import io.esastack.restlight.core.handler.HandlerAdvicesFactory;
import io.esastack.restlight.core.handler.HandlerInvoker;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.handler.LinkedHandlerInvoker;
import io.esastack.restlight.core.method.HandlerMethod;

abstract class SingletonExecutionHandler<H extends HandlerMethodAdapter> extends AbstractExecutionHandler<H> {

    private final HandlerInvoker invoker;
    private final Object bean;

    SingletonExecutionHandler(DeployContext<? extends RestlightOptions> deployContext,
                              H handlerMethod,
                              HandlerValueResolver handlerResolver,
                              Handler handler) {
        super(deployContext, handlerMethod, handlerResolver);
        Checks.checkNotNull(handler, "handler");
        this.bean = handler.bean();
        this.invoker = buildInvoker(handler, deployContext.handlerAdvicesFactory().orElse(null));
    }

    @Override
    protected Object resolveBean(DeployContext<? extends RestlightOptions> deployContext,
                                 HandlerMethod handler,
                                 RequestContext context) {
        return bean;
    }

    @Override
    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object bean) {
        return invoker;
    }

    static HandlerInvoker buildInvoker(Handler handler, HandlerAdvicesFactory handlerAdvicesFactory) {
        if (handlerAdvicesFactory != null) {
            HandlerAdvice[] handlerAdvices = handlerAdvicesFactory.getHandlerAdvices(handler);
            if (handlerAdvices != null && handlerAdvices.length > 0) {
                return LinkedHandlerInvoker.immutable(handlerAdvices, handler);
            }
        }
        return handler;
    }
}

