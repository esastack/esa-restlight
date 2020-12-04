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
package esa.restlight.server.handler;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.server.schedule.ExecutorScheduler;
import esa.restlight.server.schedule.Scheduler;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface RestlightHandler {

    /**
     * Processes the given {@link AsyncRequest} and {@link AsyncResponse}.
     *
     * @param request  current request
     * @param response current response
     *
     * @return future
     */
    CompletableFuture<Void> process(AsyncRequest request, AsyncResponse response);

    /**
     * tcp connect event
     *
     * @param ctx ctx
     */
    default void onConnected(ChannelHandlerContext ctx) {

    }

    /**
     * shutdown event
     */
    default void shutdown() {

    }

    /**
     * Start event.
     */
    default void onStart() {

    }

    /**
     * Exposure scheduler for resource multiplexing.
     *
     * @return Executor the current handler is using, {@code null} if there's no scheduler used in this handler.
     * @deprecated use {@link #schedulers()}
     */
    @Deprecated
    default Executor executor() {
        List<Scheduler> executors = schedulers();
        if (executors == null || executors.isEmpty()) {
            return null;
        }
        for (int i = executors.size() - 1; i >= 0; i--) {
            Scheduler scheduler = executors.get(i);
            if (scheduler instanceof ExecutorScheduler) {
                return ((ExecutorScheduler) scheduler).executor();
            }
        }
        return null;
    }

    List<Scheduler> schedulers();

}
