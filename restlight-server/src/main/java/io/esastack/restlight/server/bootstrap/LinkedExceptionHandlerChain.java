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
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.util.Futures;

import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

@Internal
public class LinkedExceptionHandlerChain implements ExceptionHandlerChain {

    private final IExceptionHandler handler;
    private final ExceptionHandlerChain next;

    private LinkedExceptionHandlerChain(IExceptionHandler handler, ExceptionHandlerChain next) {
        this.handler = handler;
        this.next = next;
    }

    public static ExceptionHandlerChain immutable(IExceptionHandler[] handlers) {
        Checks.checkNotNull(handlers, "handlers");
        ExceptionHandlerChain chain = (context, th) -> {
            if (th != null) {
                return Futures.completedExceptionally(th);
            } else {
                return Futures.completedFuture();
            }
        };

        int i = handlers.length - 1;
        while (i >= 0) {
            chain = new LinkedExceptionHandlerChain(handlers[i], chain);
            i--;
        }

        return chain;
    }

    public static ExceptionHandlerChain immutable(IExceptionHandler[] handlers,
                                                  BiFunction<RequestContext, Throwable,
                                                          CompletionStage<Void>> action) {
        Checks.checkNotNull(handlers, "handlers");
        ExceptionHandlerChain chain = action::apply;

        int i = handlers.length - 1;
        while (i >= 0) {
            chain = new LinkedExceptionHandlerChain(handlers[i], chain);
            i--;
        }

        return chain;
    }

    @Override
    public CompletionStage<Void> handle(RequestContext context, Throwable th) {
        return handler.handle(context, th, next);
    }
}

