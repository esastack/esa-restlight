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
package io.esastack.restlight.server.route;

import io.esastack.httpserver.core.HttpRequest;
import io.esastack.httpserver.core.RequestContext;

/**
 * A RouteExecution is used to handle current {@link HttpRequest}'s lifecycle after routing this {@link HttpRequest}.
 */
public interface RouteExecution<CTX extends RequestContext> extends Execution<CTX> {

    /**
     * Returns an instance of {@link ExceptionHandler} to handle the error occurred in request's lifecycle.
     *
     * @return handler to handle exception, {@code null} for nothing to do.
     */
    default ExceptionHandler<CTX, Throwable> exceptionHandler() {
        return null;
    }
}
