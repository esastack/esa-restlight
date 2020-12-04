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
package esa.restlight.server.route;

import esa.httpserver.core.AsyncRequest;

import java.util.List;

/**
 * A {@link ReadOnlyRouteRegistry} is used to route a {@link AsyncRequest} to a specify {@link Route} which should be
 * registered ahead.
 * @see RouteRegistry
 */
public interface ReadOnlyRouteRegistry {

    /**
     * Gets the matching {@link Route} by the given request
     *
     * @param request request
     *
     * @return matched {@link Route} or {@code null} if missing
     */
    Route route(AsyncRequest request);


    /**
     * Get all handler method invokers.
     *
     * @return handlerMethodInvokers
     */
    List<Route> routes();
}
