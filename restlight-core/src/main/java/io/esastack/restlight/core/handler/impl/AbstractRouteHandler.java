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

import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.interceptor.InternalInterceptor;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.util.Futures;
import io.netty.util.Signal;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

abstract class AbstractRouteHandler extends AbstractExecutionHandler<RouteHandlerMethodAdapter> {

    private static final CompletionException EXECUTION_NOT_ALLOWED
            = new CompletionException(Signal.valueOf("Execution not allowed by Interceptor.preHandle()"));

    final List<InternalInterceptor> interceptors;
    final boolean interceptorAbsent;
    volatile int interceptorIndex = -1;
    volatile Object bean;

    AbstractRouteHandler(HandlerValueResolver handlerResolver,
                         RouteHandlerMethodAdapter handlerMethod,
                         List<InternalInterceptor> interceptors) {
        super(handlerResolver, handlerMethod);
        this.interceptors = interceptors;
        this.interceptorAbsent = interceptors == null || interceptors.isEmpty();
    }

    @Override
    public CompletableFuture<Void> handle(RequestContext context) {
        try {
            final Object bean = resolveBean(handlerMethod(), context);
            this.bean = new RouteHandlerImpl(handlerMethod().handlerMethod(), bean);
            return applyPreHandle(context, this.bean)
                    .thenApply(allowed -> {
                        if (!allowed) {
                            // break the CompletionStage
                            throw EXECUTION_NOT_ALLOWED;
                        } else {
                            return resolveArgs(context);
                        }
                    })
                    // invoke route
                    .thenCompose(args -> invoke(context, bean, args))
                    // post
                    .thenCompose(returnValue -> applyPostHandle(context, bean)
                            .thenApply(v -> returnValue))
                    // handle return value
                    .thenCompose(returnValue -> resolveReturnValue(returnValue, context))
                    .exceptionally(t -> {
                        if (t == EXECUTION_NOT_ALLOWED) {
                            // ignore it
                            return null;
                        } else if (t instanceof CompletionException) {
                            throw (CompletionException) t;
                        } else {
                            throw new CompletionException(t);
                        }
                    });
        } catch (Throwable th) {
            return Futures.completedExceptionally(th);
        }
    }

    protected CompletableFuture<Boolean> applyPreHandle(RequestContext context, Object bean) {
        if (interceptorAbsent) {
            return Futures.completedFuture(Boolean.TRUE);
        }

        int i = 0;
        CompletableFuture<Boolean> future = null;
        do {
            final int index = i;
            if (future == null) {
                future = invokeInterceptor(context, bean, index);
            } else {
                future = future.thenCompose(allowed -> {
                    if (allowed) {
                        return invokeInterceptor(context, bean, index);
                    }
                    return Futures.completedFuture(false);
                });
            }
        } while (++i < interceptors.size());
        return future;
    }

    private CompletableFuture<Boolean> invokeInterceptor(RequestContext context, Object bean, int index) {
        return interceptors.get(index)
                .preHandle0(context, bean)
                .thenApply(allowed -> {
                    if (allowed) {
                        interceptorIndex = index;
                    }
                    return allowed;
                });
    }

    protected CompletableFuture<Void> applyPostHandle(RequestContext context, Object bean) {
        if (interceptorAbsent) {
            return Futures.completedFuture();
        }

        int i = 0;
        CompletableFuture<Void> future = null;
        do {
            final InternalInterceptor interceptor = interceptors.get(i);
            if (future == null) {
                future = interceptor.postHandle0(context, bean);
            } else {
                future = future.thenCompose(v -> interceptor.postHandle0(context, bean));
            }
        } while (++i < interceptors.size());
        return future;
    }
}

