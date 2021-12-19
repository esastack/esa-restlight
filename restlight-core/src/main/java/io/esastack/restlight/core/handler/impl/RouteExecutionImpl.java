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
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.LinkedRouteFilterChain;
import io.esastack.restlight.core.handler.RouteFilter;
import io.esastack.restlight.core.interceptor.InternalInterceptor;
import io.esastack.restlight.core.resolver.ExceptionResolver;
import io.esastack.restlight.server.core.RoutedRequest;
import io.esastack.restlight.server.context.impl.RouteContextImpl;
import io.esastack.restlight.server.core.impl.RoutedRequestImpl;
import io.esastack.restlight.server.route.CompletionHandler;
import io.esastack.restlight.server.route.ExceptionHandler;
import io.esastack.restlight.server.route.ExecutionHandler;
import io.esastack.restlight.server.route.RouteExecution;
import io.esastack.restlight.server.util.Futures;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RouteExecutionImpl implements RouteExecution {

    private final HandlerMapping mapping;
    private final AbstractRouteHandler handler;
    private final ExecutionHandler execution;
    private final CompletionHandler completionHandler;
    private final ExceptionResolver<Throwable> exceptionResolver;

    RouteExecutionImpl(HandlerMapping mapping, AbstractRouteHandler handler,
                       List<RouteFilter> filters, ExceptionResolver<Throwable> exceptionResolver) {
        Checks.checkNotNull(mapping, "mapping");
        Checks.checkNotNull(handler, "handler");
        this.mapping = mapping;
        this.handler = handler;
        this.execution = toExecution(handler, filters);
        this.exceptionResolver = exceptionResolver;
        this.completionHandler = this::triggerAfterCompletion;
    }

    @Override
    public ExecutionHandler executionHandler() {
        return execution;
    }

    @Override
    public CompletionHandler completionHandler() {
        return completionHandler;
    }

    @Override
    public ExceptionHandler<Throwable> exceptionHandler() {
        return exceptionResolver;
    }

    private ExecutionHandler toExecution(ExecutionHandler execution,
                                         List<RouteFilter> filters) {
        if (filters == null || filters.isEmpty()) {
            return execution;
        }
        LinkedRouteFilterChain chain = LinkedRouteFilterChain.immutable(filters, execution::handle);
        return context -> {
            RoutedRequest request = new RoutedRequestImpl(context.request());
            return chain.doNext(mapping, new RouteContextImpl(context, request, context.response()));
        };
    }

    private CompletableFuture<Void> triggerAfterCompletion(io.esastack.restlight.server.context.RequestContext context,
                                                           Throwable t) {
        if (handler.interceptorAbsent) {
            return Futures.completedFuture();
        }

        final Exception ex;
        if (t instanceof Exception) {
            ex = (Exception) t;
        } else if (t == null) {
            ex = null;
        } else {
            // Unhandled Throwable error.
            return Futures.completedExceptionally(new Error("Unexpected throwable.", t));
        }

        int i = handler.interceptorIndex;
        if (i < 0) {
            return Futures.completedFuture();
        }
        CompletableFuture<Void> future = null;
        do {
            final InternalInterceptor interceptor = handler.interceptors.get(i);
            if (future == null) {
                future = interceptor
                        .afterCompletion0(context, handler.bean, ex);
            } else {
                future = future.thenCompose(v -> interceptor
                        .afterCompletion0(context, handler.bean, ex));
            }
        } while (i-- > 0);

        return future;
    }
}
