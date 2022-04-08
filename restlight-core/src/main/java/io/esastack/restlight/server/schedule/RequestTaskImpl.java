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

import io.esastack.restlight.server.context.RequestContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

class RequestTaskImpl implements RequestTask {

    private final RequestContext ctx;
    private final CompletableFuture<Void> promise;
    private final Runnable r;

    private RequestTaskImpl(RequestContext ctx,
                            CompletableFuture<Void> promise,
                            Runnable r) {
        this.ctx = ctx;
        this.promise = promise;
        this.r = r;
    }

    static RequestTaskImpl newRequestTask(RequestContext context,
                                          CompletableFuture<Void> promise,
                                          Runnable r) {
        return new RequestTaskImpl(context, promise, r);
    }

    @Override
    public void run() {
        r.run();
    }

    @Override
    public RequestContext context() {
        return ctx;
    }

    @Override
    public CompletionStage<Void> promise() {
        return promise;
    }

    @Override
    public String toString() {
        return "RequestTask{uri='" + ctx.request().uri() + "'method='" + ctx.request().method() + "'}";
    }
}
