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
import io.esastack.restlight.core.handler.*;
import io.esastack.restlight.core.interceptor.Interceptor;
import io.esastack.restlight.core.interceptor.InterceptorPredicate;
import io.esastack.restlight.core.interceptor.InternalInterceptor;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.resolver.exception.ExceptionResolver;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.route.RouteExecution;

import java.util.List;

public class PrototypeRouteMethod extends RouteHandlerMethodAdapter {

    public PrototypeRouteMethod(HandlerMapping mapping,
                                HandlerContext context,
                                HandlerValueResolver handlerResolver,
                                MultiValueMap<InterceptorPredicate, Interceptor> interceptors,
                                ExceptionResolver<Throwable> exceptionResolver) {
        super(mapping, context, handlerResolver, interceptors, exceptionResolver);
    }

    @Override
    public RouteExecution toExecution(RequestContext context) {
        return new PrototypeRouteExecution(this, getMatchingInterceptors(context));
    }

    private static class PrototypeRouteExecution extends AbstractRouteExecution {

        private final HandlerFactory handlerFactory;
        private final HandlerAdvicesFactory handlerAdvicesFactory;

        private PrototypeRouteExecution(RouteHandlerMethodAdapter handlerMethod,
                                        List<InternalInterceptor> interceptors) {
            super(handlerMethod, interceptors);
            this.handlerFactory = handlerMethod.context().handlerFactory()
                    .orElseThrow(() -> new IllegalStateException("handlerFactory is null"));
            this.handlerAdvicesFactory = handlerMethod.context().handlerAdvicesFactory()
                    .orElseThrow(() -> new IllegalStateException("handlerAdvicesFactory is null"));
        }

        @Override
        protected Object resolveBean(HandlerMethod handler, RequestContext context) {
            return handlerFactory.instantiateThenInit(handler.beanType(), handler.method(), context);
        }

        @Override
        protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
            return buildInvoker(handlerMethod, instance, handlerAdvicesFactory);
        }
    }
}

