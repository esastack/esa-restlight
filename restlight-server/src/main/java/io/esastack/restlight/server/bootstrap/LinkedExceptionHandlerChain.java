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

import esa.commons.Checks;
import esa.commons.annotation.Internal;
import io.esastack.httpserver.core.RequestContext;
import io.esastack.restlight.server.internal.InternalExceptionHandler;
import io.esastack.restlight.server.util.Futures;

import java.util.concurrent.CompletableFuture;

@Internal
public class LinkedExceptionHandlerChain<CTX extends RequestContext> implements ExceptionHandlerChain<CTX> {

    private final InternalExceptionHandler<CTX> handler;
    private final ExceptionHandlerChain<CTX> next;

    private LinkedExceptionHandlerChain(InternalExceptionHandler<CTX> handler, ExceptionHandlerChain<CTX> next) {
        this.handler = handler;
        this.next = next;
    }

    public static <C extends RequestContext> ExceptionHandlerChain<C> immutable(
            InternalExceptionHandler<C>[] handlers) {
        Checks.checkNotNull(handlers, "handlers");
        ExceptionHandlerChain<C> chain = (context, th) -> Futures.completedExceptionally(th);
        int i = handlers.length - 1;
        while (i >= 0) {
            chain = new LinkedExceptionHandlerChain<>(handlers[i], chain);
            i--;
        }

        return chain;
    }

    @Override
    public CompletableFuture<Void> handle(CTX context, Throwable th) {
        return handler.handle(context, th, next);
    }
}

