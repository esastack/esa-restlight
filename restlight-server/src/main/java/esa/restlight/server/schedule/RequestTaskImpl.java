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
package esa.restlight.server.schedule;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;

import java.util.concurrent.CompletableFuture;

class RequestTaskImpl implements RequestTask {

    private final AsyncRequest req;
    private final AsyncResponse res;
    private final CompletableFuture<Void> promise;
    private final Runnable r;

    private RequestTaskImpl(AsyncRequest req,
                            AsyncResponse res,
                            CompletableFuture<Void> promise,
                            Runnable r) {
        this.req = req;
        this.res = res;
        this.promise = promise;
        this.r = r;
    }

    static RequestTaskImpl newRequestTask(AsyncRequest req,
                                          AsyncResponse resp,
                                          CompletableFuture<Void> promise,
                                          Runnable r) {
        return new RequestTaskImpl(req, resp, promise, r);
    }

    @Override
    public void run() {
        r.run();
    }

    @Override
    public AsyncRequest request() {
        return req;
    }

    @Override
    public AsyncResponse response() {
        return res;
    }

    @Override
    public CompletableFuture<Void> promise() {
        return promise;
    }

    @Override
    public String toString() {
        return "RequestTask{uri='" + req.uri() + "\'method='" + req.method() + "\'}";
    }
}
