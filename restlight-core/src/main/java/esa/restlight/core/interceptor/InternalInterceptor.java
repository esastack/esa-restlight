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
package esa.restlight.core.interceptor;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.util.Ordered;
import esa.restlight.server.util.Futures;

import java.util.concurrent.CompletableFuture;

/**
 * Common interceptor is designed to be executed around accessing target {@link esa.restlight.server.route.Route}. Be
 * different from {@link esa.restlight.server.handler.Filter}, the interceptor is more closer to handler and the
 * approximate order of interceptor and filter is: request &#8594;  filter1 &#8594; filter2 &#8594; xxx
 * &#8594; interceptor1 &#8594; interceptor2 &#8594; handler.
 */
public interface InternalInterceptor extends Ordered {

    /**
     * Asynchronous implementation of {@link #preHandle(AsyncRequest, AsyncResponse, Object)}, which will be called in
     * the lifecycle of the request(if matched) actually instead of {@link #preHandle(AsyncRequest, AsyncResponse,
     * Object)}.
     *
     * @param request  request
     * @param response response
     * @param handler  handler
     *
     * @return future result
     */
    default CompletableFuture<Boolean> preHandle0(AsyncRequest request,
                                                  AsyncResponse response,
                                                  Object handler) {
        CompletableFuture<Boolean> future;
        try {
            future = Futures.completedFuture(preHandle(request, response, handler));
        } catch (Throwable t) {
            future = Futures.completedExceptionally(t);
        }
        return future;
    }

    /**
     * Called before HandlerExecution invokes the handler.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  handler
     *
     * @return if the execution chain should proceed with the next interceptor or the handler itself. Else,
     * OldDispatcherHandler assumes that this interceptor has already dealt with the response itself.
     * @throws Exception in case of errors
     */
    default boolean preHandle(AsyncRequest request,
                              AsyncResponse response,
                              Object handler) throws Exception {
        return true;
    }

    /**
     * Asynchronous implementation of {@link #postHandle(AsyncRequest, AsyncResponse, Object)}, which will be called in
     * the lifecycle of the request(if matched). actually instead of {@link #preHandle(AsyncRequest, AsyncResponse,
     * Object)}
     *
     * @param request  request
     * @param response response
     * @param handler  handler
     *
     * @return future result
     */
    default CompletableFuture<Void> postHandle0(AsyncRequest request,
                                                AsyncResponse response,
                                                Object handler) {
        CompletableFuture<Void> future;
        try {
            postHandle(request, response, handler);
            future = Futures.completedFuture();
        } catch (Throwable t) {
            future = Futures.completedExceptionally(t);
        }
        return future;
    }

    /**
     * Called after HandlerExecution actually invoked the handler
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  handler
     *
     * @throws Exception in case of errors
     */
    default void postHandle(AsyncRequest request,
                            AsyncResponse response,
                            Object handler) throws Exception {

    }

    /**
     * Asynchronous implementation of {@link #afterCompletion(AsyncRequest, AsyncResponse, Object, Exception)}, which
     * will be called in the lifecycle of the request(if matched) actually instead of {@link #preHandle(AsyncRequest,
     * AsyncResponse, Object)}
     *
     * @param request  request
     * @param response response
     * @param handler  handler
     * @param ex       error
     *
     * @return future result
     */
    default CompletableFuture<Void> afterCompletion0(AsyncRequest request,
                                                     AsyncResponse response,
                                                     Object handler,
                                                     Exception ex) {
        CompletableFuture<Void> future;
        try {
            afterCompletion(request, response, handler, ex);
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
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  handler
     * @param ex       exception occurred
     */
    default void afterCompletion(AsyncRequest request,
                                 AsyncResponse response,
                                 Object handler,
                                 Exception ex) {

    }
}
