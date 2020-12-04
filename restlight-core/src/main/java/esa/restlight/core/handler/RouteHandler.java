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
package esa.restlight.core.handler;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.interceptor.InternalInterceptor;

/**
 * This interface defines the invocation to the handler method(controller).
 * @see RouteHandler#invoke(AsyncRequest, AsyncResponse, Object...)
 */
public interface RouteHandler extends Handler {

    /**
     * Is this handler should be intercepted by {@link InternalInterceptor}.
     *
     * @return {@code true} if should be intercepted.
     */
    default boolean intercepted() {
        return true;
    }

    /**
     * Gets the name of scheduler which would be used to schedule the route.
     *
     * @return name of the scheduler.
     */
    String scheduler();

}
