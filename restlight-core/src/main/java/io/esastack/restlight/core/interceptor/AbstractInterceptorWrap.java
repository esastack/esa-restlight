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
    public CompletionStage<Boolean> preHandle(RequestContext context, Object handler) {
        return interceptor.preHandle(context, handler);
    }

    @Override
    public CompletionStage<Void> postHandle(RequestContext context, Object handler) {
        return interceptor.postHandle(context, handler);
    }

    @Override
    public CompletionStage<Void> afterCompletion(RequestContext context, Object handler, Exception ex) {
        return interceptor.afterCompletion(context, handler, ex);
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
