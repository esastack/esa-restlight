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
package io.esastack.restlight.core.resolver.exception;

import esa.commons.Checks;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.handler.Handler;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.handler.impl.ExceptionHandlerExecution;
import io.esastack.restlight.core.handler.impl.ExecutionImpl;
import io.esastack.restlight.core.handler.impl.HandlerMethodAdapter;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.ExceptionResolver;
import io.esastack.restlight.server.util.Futures;

import java.util.concurrent.CompletableFuture;

public class ExecutionExceptionResolver implements ExceptionResolver<Throwable> {

    private final HandlerMethodAdapter<HandlerMethod> handlerMethod;
    private final HandlerValueResolver handlerResolver;
    private final Handler handler;

    public ExecutionExceptionResolver(HandlerMethodAdapter<HandlerMethod> handlerMethod,
                                      HandlerValueResolver handlerResolver,
                                      Handler handler) {
        Checks.checkNotNull(handlerMethod, "handlerMethod");
        Checks.checkNotNull(handlerResolver, "handlerResolver");
        Checks.checkNotNull(handler, "handler");
        this.handlerMethod = handlerMethod;
        this.handlerResolver = handlerResolver;
        this.handler = handler;
    }

    @Override
    public CompletableFuture<Void> handleException(RequestContext context,
                                                   Throwable ex) {
        try {
            final ExecutionImpl execution = new ExecutionImpl(new ExceptionHandlerExecution(handlerResolver,
                    handler, handlerMethod, ex));
            final CompletableFuture<Void> future = execution.executionHandler().handle(context);
            if (execution.completionHandler() == null) {
                return future;
            } else {
                return future.whenComplete((r, t) -> execution.completionHandler().onComplete(context,
                        Futures.unwrapCompletionException(t)));
            }
        } catch (Throwable t) {
            return Futures.completedExceptionally(t);
        }
    }

    @Override
    public String toString() {
        return handlerMethod.toString();
    }

}
