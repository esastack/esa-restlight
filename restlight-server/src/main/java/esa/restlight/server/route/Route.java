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
import esa.restlight.server.route.impl.RouteImpl;
import esa.restlight.server.schedule.Scheduling;

import java.util.Optional;

/**
 * A Route maintains a {@link Mapping} which will be used to determine whether an {@link AsyncRequest} should be routed
 * to it.
 */
public interface Route extends Scheduling {

    /**
     * Creates an instance {@link RouteImpl} for building a new {@link Route}.
     *
     * @return {@link RouteImpl}
     */
    static RouteImpl route() {
        return new RouteImpl(null, null, null, null);
    }

    /**
     * Creates an instance {@link RouteImpl} and sets {@link #mapping()} to given value for building a new {@link
     * Route}.
     *
     * @param mapping mapping
     *
     * @return {@link RouteImpl}
     */
    static RouteImpl route(Mapping mapping) {
        return new RouteImpl(mapping, null, null, null);
    }

    /**
     * Creates an instance {@link RouteImpl} by given {@link Route} for building a new {@link Route}.
     *
     * @param another another
     *
     * @return {@link RouteImpl}
     */
    static RouteImpl route(Route another) {
        return new RouteImpl(another.mapping(),
                another::toExecution,
                another.scheduler(),
                another.handler().orElse(null));
    }

    /**
     * Returns the {@link Mapping} of this route.
     *
     * @return mapping
     */
    Mapping mapping();

    /**
     * Creates a {@link RouteExecution} by given {@link AsyncRequest} which has already bean routed to current route.
     *
     * @param request current request
     *
     * @return {@link RouteExecution} of this route
     */
    RouteExecution toExecution(AsyncRequest request);

    /**
     * Returns the handler object which depends on the implementation.
     *
     * @return handler object
     */
    default Optional<Object> handler() {
        return Optional.empty();
    }

}
