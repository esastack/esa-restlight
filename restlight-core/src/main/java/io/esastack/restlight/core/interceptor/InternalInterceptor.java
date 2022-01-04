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
package io.esastack.restlight.core.interceptor;

import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.handler.Filter;
import io.esastack.restlight.server.route.Route;
import io.esastack.restlight.server.util.Futures;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Common interceptor is designed to be executed around accessing target {@link Route}. Be
 * different from {@link Filter}, the interceptor is more closer to handler and the
 * approximate order of interceptor and filter is: request &#8594;  filter1 &#8594; filter2 &#8594; xxx
 * &#8594; interceptor1 &#8594; interceptor2 &#8594; handler.
 */
public interface InternalInterceptor extends Ordered {

    /**
     * Asynchronous implementation of {@link #preHandle(RequestContext, Object)}, which will be called in
     * the lifecycle of the request(if matched) actually instead of {@link #preHandle(RequestContext, Object)}.
     *
     * @param context request context
     * @param handler handler
     * @return future result
     */
    default CompletionStage<Boolean> preHandle0(RequestContext context, Object handler) {
        CompletableFuture<Boolean> future;
        try {
            future = Futures.completedFuture(preHandle(context, handler));
        } catch (Throwable t) {
            future = Futures.completedExceptionally(t);
        }
        return future;
    }

    /**
     * Called before HandlerExecution invokes the handler.
     *
     * @param context current HTTP request context
     * @param handler handler
     * @return if the execution chain should proceed with the next interceptor or the handler itself. Else,
     * OldDispatcherHandler assumes that this interceptor has already dealt with the response itself.
     * @throws Exception in case of errors
     */
    default boolean preHandle(RequestContext context, Object handler) throws Exception {
        return true;
    }

    /**
     * Asynchronous implementation of {@link #postHandle(RequestContext, Object)}, which will be called in
     * the lifecycle of the request(if matched). actually instead of {@link #preHandle(RequestContext, Object)}
     *
     * @param context request context
     * @param handler handler
     * @return future result
     */
    default CompletionStage<Void> postHandle0(RequestContext context, Object handler) {
        CompletableFuture<Void> future;
        try {
            postHandle(context, handler);
            future = Futures.completedFuture();
        } catch (Throwable t) {
            future = Futures.completedExceptionally(t);
        }
        return future;
    }

    /**
     * Called after HandlerExecution actually invoked the handler
     *
     * @param context current HTTP request context
     * @param handler handler
     * @throws Exception in case of errors
     */
    default void postHandle(RequestContext context, Object handler) throws Exception {

    }

    /**
     * Asynchronous implementation of {@link #afterCompletion(RequestContext, Object, Exception)}, which
     * will be called in the lifecycle of the request(if matched) actually instead of {@link #preHandle(RequestContext,
     * Object)}
     *
     * @param context request context
     * @param handler handler
     * @param ex      error
     * @return future result
     */
    default CompletionStage<Void> afterCompletion0(RequestContext context,
                                                   Object handler,
                                                   Exception ex) {
        CompletableFuture<Void> future;
        try {
            afterCompletion(context, handler, ex);
            future = Futures.completedFuture();
        } catch (Throwable t) {
            future = Futures.completedExceptionally(t);
        }
        return future;
    }

    /**
     * Callback after completion of request processing
     * <p>
     * Note: Will only be called if this interceptor's preHandle method has successfully completed and returned true!
     *
     * @param context current HTTP request context
     * @param handler handler
     * @param ex      exception occurred
     */
    default void afterCompletion(RequestContext context,
                                 Object handler,
                                 Exception ex) {

    }
}
