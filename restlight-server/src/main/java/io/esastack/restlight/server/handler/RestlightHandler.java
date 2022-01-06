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
package io.esastack.restlight.server.handler;

import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.schedule.Scheduler;
import io.netty.channel.Channel;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface RestlightHandler {

    /**
     * Processes the given {@link RequestContext}.
     *
     * @param context  context
     * @return future
     */
    CompletionStage<Void> process(RequestContext context);

    /**
     * tcp connect event
     *
     * @param channel channel
     */
    default void onConnected(Channel channel) {

    }

    /**
     * tcp disconnect event
     *
     * @param channel   channel
     */
    default void onDisconnected(Channel channel) {

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

    List<Scheduler> schedulers();

}
