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
package io.esastack.restlight.server.schedule;

import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.context.RequestContext;

import java.util.concurrent.CompletableFuture;

public interface RequestTask extends Runnable {

    /**
     * Get associated request
     *
     * @return request
     */
    default HttpRequest request() {
        return context().request();
    }

    /**
     * Get associated response
     *
     * @return response
     */
    default HttpResponse response() {
        return context().response();
    }

    /**
     * Obtains current {@link RequestContext}.
     *
     * @return  context
     */
    RequestContext context();

    /**
     * Get associated promise
     *
     * @return promise
     */
    CompletableFuture<Void> promise();
}
