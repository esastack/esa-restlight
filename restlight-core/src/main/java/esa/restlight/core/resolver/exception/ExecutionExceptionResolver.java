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
package esa.restlight.core.resolver.exception;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.handler.impl.ExceptionHandlerExecution;
import esa.restlight.core.handler.impl.HandlerAdapter;
import esa.restlight.core.resolver.ExceptionResolver;
import esa.restlight.server.route.Execution;
import esa.restlight.server.util.Futures;

import java.util.concurrent.CompletableFuture;

public class ExecutionExceptionResolver implements ExceptionResolver<Throwable> {

    private final HandlerAdapter handlerAdapter;

    public ExecutionExceptionResolver(HandlerAdapter handlerAdapter) {
        this.handlerAdapter = handlerAdapter;
    }

    @Override
    public CompletableFuture<Void> handleException(AsyncRequest request,
                                                   AsyncResponse response,
                                                   Throwable ex) {
        try {
            Execution execution = new ExceptionHandlerExecution(handlerAdapter, ex);

            final CompletableFuture<Void> future = execution.handle(request, response);
            if (execution.completionHandler() == null) {
                return future;
            } else {
                return future.whenComplete((r, t) -> execution.completionHandler().onComplete(request, response,
                        Futures.unwrapCompletionException(t)));
            }
        } catch (Throwable t) {
            return Futures.completedExceptionally(t);
        }
    }

    @Override
    public String toString() {
        return handlerAdapter.toString();
    }
}
