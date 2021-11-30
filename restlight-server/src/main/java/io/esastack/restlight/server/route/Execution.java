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
import io.esastack.httpserver.core.HttpResponse;
import io.esastack.httpserver.core.RequestContext;

/**
 * A Execution is used to handle current {@link HttpRequest}'s lifecycle after routing this {@link HttpRequest}.
 */
public interface Execution<CTX extends RequestContext> {

    /**
     * Handles current {@link HttpRequest}, this method must make sure that current {@link HttpResponse} has been
     * committed or else Restlight will handle it for you which probably means a error.
     *
     * @return execution handler
     */
    ExecutionHandler<CTX> executionHandler();

    /**
     * Returns an instance of {@link CompletionHandler} to handle the completion event of current request.
     *
     * @return handler to handle completion event. {@code null} for nothing to do.
     */
    default CompletionHandler completionHandler() {
        return null;
    }

}
