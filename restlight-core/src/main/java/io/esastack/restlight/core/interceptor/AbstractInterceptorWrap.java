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

import esa.commons.Checks;
import io.esastack.restlight.server.context.RequestContext;

import java.util.concurrent.CompletionStage;

abstract class AbstractInterceptorWrap<I extends InternalInterceptor> implements Interceptor {

    final I interceptor;

    AbstractInterceptorWrap(I interceptor) {
        Checks.checkNotNull(interceptor, "interceptor");
        this.interceptor = interceptor;
    }

    @Override
    public CompletionStage<Boolean> preHandle0(RequestContext context, Object handler) {
        return interceptor.preHandle0(context, handler);
    }

    @Override
    public boolean preHandle(RequestContext context, Object handler) throws Exception {
        return interceptor.preHandle(context, handler);
    }

    @Override
    public CompletionStage<Void> postHandle0(RequestContext context, Object handler) {
        return interceptor.postHandle0(context, handler);
    }

    @Override
    public void postHandle(RequestContext context, Object handler) throws Exception {
        interceptor.postHandle(context, handler);
    }

    @Override
    public CompletionStage<Void> afterCompletion0(RequestContext context, Object handler, Exception ex) {
        return interceptor.afterCompletion0(context, handler, ex);
    }

    @Override
    public void afterCompletion(RequestContext context, Object handler, Exception ex) {
        interceptor.afterCompletion(context, handler, ex);
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
