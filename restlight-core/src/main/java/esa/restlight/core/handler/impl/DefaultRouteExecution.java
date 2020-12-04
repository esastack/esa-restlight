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
package esa.restlight.core.handler.impl;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.interceptor.InternalInterceptor;
import esa.restlight.core.resolver.ExceptionResolver;
import esa.restlight.server.route.CompletionHandler;
import esa.restlight.server.util.Futures;
import io.netty.util.Signal;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Implementation of {@link esa.restlight.server.route.RouteExecution} that always wrap the {@code return value} to
 * {@link CompletableFuture}
 */
public class DefaultRouteExecution extends AbstractRouteExecution<RouteHandlerAdapter> {

    private static final CompletionException EXECUTION_NOT_ALLOWED
            = new CompletionException(Signal
            .valueOf("Execution not allowed by Interceptor.preHandle()"));

    private final List<InternalInterceptor> interceptors;
    private volatile int interceptorIndex = -1;
    private final boolean interceptorAbsent;
    private final CompletionHandler completionHandler;

    public DefaultRouteExecution(RouteHandlerAdapter handlerAdapter,
                                 List<InternalInterceptor> interceptors) {
        super(handlerAdapter);
        this.interceptors = interceptors;
        this.interceptorAbsent = interceptors == null || interceptors.isEmpty();
        this.completionHandler = (this::triggerAfterCompletion);
    }

    @Override
    public CompletableFuture<Void> handle(AsyncRequest request, AsyncResponse response) {
        return applyPreHandle(request, response)
                .thenApply(allowed -> {
                    if (!allowed) {
                        // break the CompletionStage
                        throw EXECUTION_NOT_ALLOWED;
                    } else {
                        return resolveArguments(request, response);
                    }
                })
                // invoke route
                .thenCompose(args -> invoke(request, response, args))
                // post
                .thenCompose(returnValue -> applyPostHandle(request, response)
                        .thenApply(v -> returnValue))
                // handle return value
                .thenAccept(returnValue -> handleReturnValue(returnValue, request, response))
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
    }

    @Override
    public ExceptionResolver<Throwable> exceptionHandler() {
        return handlerAdapter.exceptionResolver();
    }

    @Override
    public CompletionHandler completionHandler() {
        return completionHandler;
    }

    protected CompletableFuture<Boolean> applyPreHandle(AsyncRequest request, AsyncResponse response) {
        if (interceptorAbsent) {
            return Futures.completedFuture(Boolean.TRUE);
        }

        int i = 0;
        CompletableFuture<Boolean> future = null;
        do {
            final int index = i;
            if (future == null) {
                future = invokeInterceptor(request, response, index);
            } else {
                future = future.thenCompose(allowed -> {
                    if (allowed) {
                        return invokeInterceptor(request, response, index);
                    }
                    return Futures.completedFuture(allowed);
                });
            }
        } while (++i < interceptors.size());
        return future;
    }

    private CompletableFuture<Boolean> invokeInterceptor(AsyncRequest request, AsyncResponse response, int index) {
        return interceptors.get(index)
                .preHandle0(request, response, handlerAdapter.handler())
                .thenApply(allowed -> {
                    if (allowed) {
                        interceptorIndex = index;
                    }
                    return allowed;
                });
    }

    protected CompletableFuture<Void> applyPostHandle(AsyncRequest request, AsyncResponse response) {
        if (interceptorAbsent) {
            return Futures.completedFuture();
        }

        int i = 0;
        CompletableFuture<Void> future = null;
        do {
            final InternalInterceptor interceptor = interceptors.get(i);
            if (future == null) {
                future = interceptor.postHandle0(request, response, handlerAdapter.handler());
            } else {
                future = future.thenCompose(v -> interceptor.postHandle0(request, response, handlerAdapter.handler()));
            }
        } while (++i < interceptors.size());
        return future;
    }

    protected CompletableFuture<Void> triggerAfterCompletion(AsyncRequest request,
                                                             AsyncResponse response,
                                                             Throwable t) {
        if (interceptorAbsent) {
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
        CompletableFuture<Void> future = null;
        do {
            final InternalInterceptor interceptor = interceptors.get(i);
            if (future == null) {
                future = interceptor
                        .afterCompletion0(request, response, handlerAdapter.handler(), ex);
            } else {
                future = future.thenCompose(v -> interceptor
                        .afterCompletion0(request, response, handlerAdapter.handler(), ex));
            }
        } while (i-- > 0);

        return future;
    }

    @Override
    protected CompletableFuture<Object> transferToFuture(Object returnValue) {
        return Futures.completedFuture(returnValue);
    }
}
