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
import esa.commons.collection.MultiValueMap;
import io.esastack.restlight.core.handler.HandlerInvoker;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.interceptor.Interceptor;
import io.esastack.restlight.core.interceptor.InterceptorPredicate;
import io.esastack.restlight.core.interceptor.InternalInterceptor;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.ExceptionResolver;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.route.RouteExecution;

import java.util.List;

public class SingletonRouteMethod extends RouteHandlerMethodAdapter {

    private final Object singleton;
    private final HandlerInvoker invoker;

    public SingletonRouteMethod(HandlerMapping mapping,
                                HandlerContext context,
                                HandlerValueResolver handlerResolver,
                                MultiValueMap<InterceptorPredicate, Interceptor> interceptors,
                                ExceptionResolver<Throwable> exceptionResolver) {
        super(mapping, context, handlerResolver, interceptors, exceptionResolver);
        this.singleton = mapping.bean().orElseThrow(() -> new IllegalStateException("bean is null"));

        Checks.checkState(this.context().handlerAdvicesFactory().isPresent(),
                "handlerAdvicesFactory is null");
        this.invoker = AbstractRouteExecution.buildInvoker(this, singleton,
                this.context().handlerAdvicesFactory().get());
    }

    @Override
    public RouteExecution toExecution(RequestContext context) {
        return new SingletonRouteExecution(this,
                getMatchingInterceptors(context),
                singleton,
                invoker);
    }

    private static class SingletonRouteExecution extends AbstractRouteExecution {

        private final Object singleton;
        private final HandlerInvoker invoker;

        private SingletonRouteExecution(RouteHandlerMethodAdapter handlerMethod,
                                        List<InternalInterceptor> interceptors,
                                        Object singleton,
                                        HandlerInvoker invoker) {
            super(handlerMethod, interceptors);
            this.singleton = singleton;
            this.invoker = invoker;
        }

        @Override
        protected Object resolveBean(HandlerMethod handler, RequestContext context) {
            return singleton;
        }

        @Override
        protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
            return invoker;
        }
    }
}

