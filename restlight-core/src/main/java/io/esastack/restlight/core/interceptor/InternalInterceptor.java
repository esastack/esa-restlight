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

import java.util.concurrent.CompletionStage;

/**
 * Common interceptor is designed to be executed around accessing target {@link Route}. Be
 * different from {@link Filter}, the interceptor is more closer to handler and the
 * approximate order of interceptor and filter is: request &#8594;  filter1 &#8594; filter2 &#8594; xxx
 * &#8594; interceptor1 &#8594; interceptor2 &#8594; handler.
 */
public interface InternalInterceptor extends Ordered {

    /**
     * Will be called before HandlerExecution invokes the handler in the lifecycle of the request(if matched).
     *
     * @param context request context
     * @param handler handler
     * @return future result
     */
    default CompletionStage<Boolean> preHandle(RequestContext context, Object handler) {
        return Futures.completedFuture(Boolean.TRUE);
    }

    /**
     * Will be called after HandlerExecution invokes the handler in the lifecycle of the request(if matched).
     *
     * @param context request context
     * @param handler handler
     * @return future result
     */
    default CompletionStage<Void> postHandle(RequestContext context, Object handler) {
        return Futures.completedFuture();
    }

    /**
     * Callback after completion of request processing in the lifecycle of the request(if matched).
     * <p>
     * Note: Will only be called if this interceptor's preHandle method has successfully completed and returned true!
     *
     * @param context request context
     * @param handler handler
     * @param ex      error
     * @return future result
     */
    default CompletionStage<Void> afterCompletion(RequestContext context,
                                                  Object handler,
                                                  Exception ex) {
        return Futures.completedFuture();
    }
}
