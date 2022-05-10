/*
 * Copyright 2022 OPPO ESA Stack Project
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
package io.esastack.restlight.core.server.processor;

import esa.commons.Checks;
import io.esastack.restlight.core.dispatcher.ExceptionHandlerChain;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.server.Connection;
import io.esastack.restlight.core.server.processor.schedule.Scheduler;
import io.esastack.restlight.core.util.Futures;
import io.esastack.restlight.core.util.PromiseUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public abstract class AbstractRestlightHandler implements RestlightHandler {

    private final RestlightHandler underlying;
    private final ExceptionHandlerChain exceptionHandler;

    public AbstractRestlightHandler(RestlightHandler underlying,
                                    ExceptionHandlerChain handlerChain) {
        Checks.checkNotNull(underlying, "underlying");
        Checks.checkNotNull(handlerChain, "handlerChain");
        this.underlying = underlying;
        this.exceptionHandler = handlerChain;
    }

    @Override
    public CompletionStage<Void> process(RequestContext context) {
        CompletableFuture<Void> promise = new CompletableFuture<>();
        underlying.process(context)
                .whenComplete((v, th) -> {
                    if (th != null) {
                        handleException(context, Futures.unwrapCompletionException(th), promise);
                    } else {
                        promise.complete(v);
                    }
                });

        return promise;
    }

    @Override
    public void onConnectionInit(Connection connection) {
        underlying.onConnectionInit(connection);
    }

    @Override
    public void onConnected(Connection connection) {
        underlying.onConnected(connection);
    }

    @Override
    public void onDisconnected(Connection connection) {
        underlying.onDisconnected(connection);
    }

    @Override
    public void shutdown() {
        underlying.shutdown();
    }

    @Override
    public void onStart() {
        underlying.onStart();
    }

    @Override
    public List<Scheduler> schedulers() {
        return underlying.schedulers();
    }

    /**
     * Whether the exception occurred in given {@link RequestContext} should be handleable or not.
     *
     * @param context context
     * @param th      th
     * @return {@code true} if should be handled, otherwise {@code false}.
     */
    protected abstract boolean isHandleable(RequestContext context, Throwable th);

    private void handleException(RequestContext context, Throwable th,
                                 CompletableFuture<Void> promise) {
        if (isHandleable(context, th)) {
            exceptionHandler.handle(context, th)
                    .whenComplete((v, t) -> {
                        if (t != null) {
                            PromiseUtils.setFailure(promise, th);
                        } else {
                            PromiseUtils.setSuccess(promise);
                        }
                    });
        } else {
            promise.completeExceptionally(th);
        }
    }
}

