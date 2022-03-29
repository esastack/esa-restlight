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

import io.esastack.restlight.core.handler.Handler;
import io.esastack.restlight.core.handler.HandlerAdvice;
import io.esastack.restlight.core.handler.HandlerAdvicesFactory;
import io.esastack.restlight.core.handler.HandlerInvoker;
import io.esastack.restlight.core.handler.LinkedHandlerInvoker;
import io.esastack.restlight.core.handler.LinkedRouteFilterChain;
import io.esastack.restlight.core.handler.RouteFilter;
import io.esastack.restlight.core.handler.RouteHandler;
import io.esastack.restlight.core.interceptor.InternalInterceptor;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.impl.RouteContextImpl;
import io.esastack.restlight.server.core.impl.RoutedRequestImpl;
import io.esastack.restlight.server.route.CompletionHandler;
import io.esastack.restlight.server.route.ExceptionHandler;
import io.esastack.restlight.server.route.RouteExecution;
import io.esastack.restlight.server.util.Futures;
import io.netty.util.Signal;

import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

abstract class AbstractRouteExecution extends AbstractExecution<RouteHandlerMethodAdapter> implements RouteExecution {

    private static final CompletionException EXECUTION_NOT_ALLOWED
            = new CompletionException(Signal.valueOf("Execution not allowed by Interceptor.preHandle()"));

    private final List<InternalInterceptor> interceptors;
    private final boolean interceptorAbsent;
    private final CompletionHandler completionHandler;
    private volatile RouteHandler handler;
    private volatile int interceptorIndex = -1;

    AbstractRouteExecution(RouteHandlerMethodAdapter handlerMethod, List<InternalInterceptor> interceptors) {
        super(handlerMethod.handlerResolver(), handlerMethod);
        this.interceptors = interceptors;
        this.interceptorAbsent = interceptors == null || interceptors.isEmpty();
        this.completionHandler = (this::triggerAfterCompletion);
    }

    static HandlerInvoker buildInvoker(HandlerMethod method, Object instance,
                                       HandlerAdvicesFactory handlerAdvicesFactory) {
        Handler handler = new HandlerImpl(method, instance);
        if (handlerAdvicesFactory != null) {
            HandlerAdvice[] handlerAdvices = handlerAdvicesFactory.getHandlerAdvices(handler);
            if (handlerAdvices != null && handlerAdvices.length > 0) {
                return LinkedHandlerInvoker.immutable(handlerAdvices, handler);
            }
        }
        return handler;
    }

    @Override
    public CompletionStage<Void> handle(RequestContext context) {
        RouteFilter[] filters = handlerMethod().filters();
        if (filters.length == 0) {
            return doHandle(context);
        }
        LinkedRouteFilterChain chain = LinkedRouteFilterChain.immutable(filters, this::doHandle);
        return chain.doNext(handlerMethod().mapping(), new RouteContextImpl(context.attrs(),
                new RoutedRequestImpl(context.request()), context.response()));
    }

    private CompletionStage<Void> doHandle(RequestContext context) {
        try {
            final Object bean = resolveBean(handlerMethod(), context);
            this.handler = new RouteHandlerImpl(handlerMethod().handlerMethod(), bean);
            return applyPreHandle(context, this.handler)
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

    @Override
    public CompletionHandler completionHandler() {
        return this.completionHandler;
    }

    @Override
    public ExceptionHandler<Throwable> exceptionHandler() {
        return handlerMethod().exceptionResolver();
    }

    CompletionStage<Boolean> applyPreHandle(RequestContext context, Object bean) {
        if (interceptorAbsent) {
            return Futures.completedFuture(Boolean.TRUE);
        }

        int i = 0;
        CompletionStage<Boolean> future = null;
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

    private CompletionStage<Boolean> invokeInterceptor(RequestContext context, Object bean, int index) {
        return interceptors.get(index)
                .preHandle(context, bean)
                .thenApply(allowed -> {
                    if (allowed) {
                        interceptorIndex = index;
                    }
                    return allowed;
                });
    }

    CompletionStage<Void> applyPostHandle(RequestContext context, Object bean) {
        if (interceptorAbsent) {
            return Futures.completedFuture();
        }

        int i = 0;
        CompletionStage<Void> future = null;
        do {
            final InternalInterceptor interceptor = interceptors.get(i);
            if (future == null) {
                future = interceptor.postHandle(context, bean);
            } else {
                future = future.thenCompose(v -> interceptor.postHandle(context, bean));
            }
        } while (++i < interceptors.size());
        return future;
    }

    CompletionStage<Void> triggerAfterCompletion(RequestContext context, Throwable t) {
        if (this.interceptorAbsent) {
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

        int i = interceptorIndex;
        if (i < 0) {
            return Futures.completedFuture();
        }
        CompletionStage<Void> future = null;
        do {
            final InternalInterceptor interceptor = interceptors.get(i);
            if (future == null) {
                future = interceptor
                        .afterCompletion(context, handler, ex);
            } else {
                future = future.thenCompose(v -> interceptor.afterCompletion(context, handler, ex));
            }
        } while (i-- > 0);

        return future;
    }
}

