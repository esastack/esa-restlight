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

import esa.commons.Checks;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;

import java.util.concurrent.CompletableFuture;

abstract class AbstractInterceptorWrap<I extends InternalInterceptor> implements Interceptor {

    final I interceptor;

    AbstractInterceptorWrap(I interceptor) {
        Checks.checkNotNull(interceptor, "interceptor");
        this.interceptor = interceptor;
    }

    @Override
    public CompletableFuture<Boolean> preHandle0(AsyncRequest request, AsyncResponse response, Object handler) {
        return interceptor.preHandle0(request, response, handler);
    }

    @Override
    public boolean preHandle(AsyncRequest request, AsyncResponse response, Object handler) throws Exception {
        return interceptor.preHandle(request, response, handler);
    }

    @Override
    public CompletableFuture<Void> postHandle0(AsyncRequest request, AsyncResponse response, Object handler) {
        return interceptor.postHandle0(request, response, handler);
    }

    @Override
    public void postHandle(AsyncRequest request, AsyncResponse response, Object handler) throws Exception {
        interceptor.postHandle(request, response, handler);
    }

    @Override
    public CompletableFuture<Void> afterCompletion0(AsyncRequest request, AsyncResponse response, Object handler,
                                                    Exception ex) {
        return interceptor.afterCompletion0(request, response, handler, ex);
    }

    @Override
    public void afterCompletion(AsyncRequest request, AsyncResponse response, Object handler, Exception ex) {
        interceptor.afterCompletion(request, response, handler, ex);
    }

    @Override
    public int getOrder() {
        return interceptor.getOrder();
    }

    @Override
    public String toString() {
        return interceptor.toString();
    }
}
