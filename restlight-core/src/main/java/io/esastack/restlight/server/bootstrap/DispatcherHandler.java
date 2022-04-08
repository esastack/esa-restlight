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

import esa.commons.annotation.Internal;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.route.Route;
import io.esastack.restlight.server.schedule.RequestTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * DispatcherHandler
 * <p>
 * A full processing of a request is spited into 2 phase:
 * <p>
 * 1. find {@link Route} by {@link #route(RequestContext)}.
 * 2. process request by {@link Route#executionFactory()},
 * see {@link #service(RequestContext, CompletableFuture, Route)}.
 */
@Internal
public interface DispatcherHandler {

    /**
     * Get all handler method invokers.
     *
     * @return handlerMethodInvokers
     */
    List<Route> routes();

    /**
     * This is the first phase of request processing which should find a {@link Route} for current request and handle
     * the not found event if there's no {@link Route} found in current context.
     *
     * @param context context
     * @return routes, which may be {@code null}.
     */
    Route route(RequestContext context);

    /**
     * process for request
     *
     * @param context context
     * @param promise promise
     * @param route   routes
     */
    void service(RequestContext context,
                 CompletionStage<Void> promise,
                 Route route);

    /**
     * Handle the {@code task} rejected by BIZ threads.
     *
     * @param task   task
     * @param reason error message
     */
    void handleRejectedWork(RequestTask task, String reason);

    /**
     * Handle the biz task unfinished. this event will fired when server is about stopping.
     *
     * @param unfinishedWorkList tasks
     */
    void handleUnfinishedWorks(List<RequestTask> unfinishedWorkList);

    /**
     * shutdown event
     */
    default void shutdown() {

    }

    /**
     * biz thread pool reject counts
     *
     * @return long
     */
    long rejectCount();

}

