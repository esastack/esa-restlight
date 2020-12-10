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
package esa.restlight.server.bootstrap;

import esa.commons.annotation.Internal;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.server.route.Route;
import esa.restlight.server.schedule.RequestTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * DispatcherHandler
 * <p>
 * A full processing of a quest is spited into 2 phase:
 * <p>
 * 1. find {@link Route} and handle not found. 2. process {@link Route}.
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
     * this is the first phase of request processing which should find a {@link Route} for current request and handle
     * the not found event if there's no {@link Route} found in current context.
     *
     * @param request  request
     * @param response response
     * @return route
     */
    Route route(AsyncRequest request, AsyncResponse response);

    /**
     * process for request
     *
     * @param request  request
     * @param response response
     * @param promise  promise
     * @param route    route
     */
    void service(AsyncRequest request,
                 AsyncResponse response,
                 CompletableFuture<Void> promise,
                 Route route);

    /**
     * Handle for unexpected error.
     *
     * @param request  request
     * @param response response
     * @param error    error
     * @param promise  promise
     */
    void handleUnexpectedError(AsyncRequest request,
                               AsyncResponse response,
                               Throwable error,
                               CompletableFuture<Void> promise);

    /**
     * Handle the biz task rejected.
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
