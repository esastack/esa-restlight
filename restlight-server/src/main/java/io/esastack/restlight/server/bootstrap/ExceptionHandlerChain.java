/*
 * Copyright 2021 OPPO ESA Stack Project
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
package io.esastack.restlight.server.bootstrap;

import esa.commons.annotation.Internal;
import io.esastack.httpserver.core.RequestContext;
import io.esastack.restlight.server.internal.InternalExceptionHandler;

import java.util.concurrent.CompletableFuture;

/**
 * This is a chained {@link InternalExceptionHandler} which is used to handle throwable.
 */
@Internal
public interface ExceptionHandlerChain<CTX extends RequestContext> {

    /**
     * Handles the given {@link Throwable} by given {@code context}.
     *
     * @param context   context
     * @param th        throwable
     * @return          handled result
     */
    CompletableFuture<Void> handle(CTX context, Throwable th);

}

