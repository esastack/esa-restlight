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
package io.esastack.restlight.server.schedule;

import esa.commons.Checks;
import io.esastack.restlight.server.bootstrap.ExceptionHandlerChain;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.handler.RestlightHandler;
import io.esastack.restlight.server.util.Futures;
import io.esastack.restlight.server.util.PromiseUtils;
import io.netty.channel.Channel;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ExceptionHandledRestlightHandler implements RestlightHandler {

    private final RestlightHandler underlying;
    private final ExceptionHandlerChain exceptionHandler;

    public ExceptionHandledRestlightHandler(RestlightHandler underlying,
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
    public void onConnected(Channel channel) {
        underlying.onConnected(channel);
    }

    @Override
    public void onDisconnected(Channel channel) {
        underlying.onDisconnected(channel);
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

    private void handleException(RequestContext context, Throwable th,
                                 CompletableFuture<Void> promise) {
        exceptionHandler.handle(context, th)
                .whenComplete((v, t) -> {
                    if (t != null) {
                        PromiseUtils.setFailure(promise, th);
                    } else {
                        PromiseUtils.setSuccess(promise);
                    }
                });
    }
}

