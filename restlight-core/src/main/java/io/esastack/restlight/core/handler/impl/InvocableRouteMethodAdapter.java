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

import esa.commons.collection.MultiValueMap;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.handler.HandlerInvoker;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.handler.RouteHandler;
import io.esastack.restlight.core.interceptor.Interceptor;
import io.esastack.restlight.core.interceptor.InterceptorPredicate;
import io.esastack.restlight.core.resolver.ExceptionResolver;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.server.route.RouteExecution;

import static io.esastack.restlight.core.handler.impl.SingletonExecutionHandler.buildInvoker;

public class InvocableRouteMethodAdapter extends RouteHandlerMethodAdapter {

    private final HandlerInvoker invoker;
    private final Object bean;

    public InvocableRouteMethodAdapter(DeployContext<? extends RestlightOptions> deployContext,
                                       HandlerMapping mapping,
                                       RouteHandler handler,
                                       HandlerResolverFactory handlerFactory,
                                       HandlerValueResolver handlerResolver,
                                       MultiValueMap<InterceptorPredicate, Interceptor> interceptors,
                                       ExceptionResolver<Throwable> exceptionResolver) {
        super(mapping, deployContext.paramPredicate().orElse(null), handlerFactory,
                handlerResolver, interceptors, exceptionResolver);
        this.bean = handler.bean();
        this.invoker = buildInvoker(handler, deployContext.handlerAdvicesFactory().orElse(null));
    }

    @Override
    public RouteExecution<RequestContext> toExecution(DeployContext<? extends RestlightOptions> ctx,
                                                      RequestContext context) {
        return new RouteExecutionImpl(mapping(), new SingletonRouteHandler(ctx, this,
                handlerResolver(), getMatchingInterceptors(context.request())), filters(),
                exceptionResolver());
    }

    HandlerInvoker handlerInvoker() {
        return invoker;
    }

    Object bean() {
        return bean;
    }
}

