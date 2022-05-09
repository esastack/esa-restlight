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
package io.esastack.restlight.core.route;

import io.esastack.restlight.core.context.RequestContext;

import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface CompletionHandler {

    /**
     * Handles the completion event of request.
     *
     * @param context current context
     * @param t       error
     * @return future
     */
    CompletionStage<Void> onComplete(RequestContext context, Throwable t);

}