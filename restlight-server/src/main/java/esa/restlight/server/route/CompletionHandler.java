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
package esa.restlight.server.route;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface CompletionHandler {

    /**
     * Handles the completion event of request.
     *
     * @param request  current request
     * @param response current response
     * @param t        error
     *
     * @return future
     */
    CompletableFuture<Void> onComplete(AsyncRequest request, AsyncResponse response, Throwable t);

}
