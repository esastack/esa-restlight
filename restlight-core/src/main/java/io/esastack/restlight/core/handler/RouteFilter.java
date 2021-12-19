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
package io.esastack.restlight.core.handler;

import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.server.context.RouteContext;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.route.Route;
import io.esastack.restlight.server.route.RouteExecution;
import io.esastack.restlight.server.schedule.Scheduler;

import java.util.concurrent.CompletableFuture;

/**
 * The {@link RouteFilter} is designed to handle the {@link HttpRequest} and {@link HttpResponse} as soon as
 * the {@link Route} has matched.
 */
public interface RouteFilter extends Ordered {

    /**
     * This callback method is invoked immediately after the {@code route} has matched.
     * We can't provide any guarantees about which {@link Scheduler} the method is scheduled on, maybe on the
     * {@code IOs}, or {@code BIZs}. So you mustn't block the method anywhere.
     * <p>
     * !NOTE: You mustn't modify the given {@code routes}, otherwise some unexpected error may occur.
     *
     * @param mapping   current mapping
     * @param context   context
     * @param next      next filter chain
     * @return promise, if {@code true} means the request will be handled by {@link RouteExecution}
     * continuously, otherwise the request will be terminated and the
     * {@link RouteContext#response()} will be returned directly.
     */
    CompletableFuture<Void> routed(HandlerMapping mapping, RouteContext context, RouteFilterChain next);

}

