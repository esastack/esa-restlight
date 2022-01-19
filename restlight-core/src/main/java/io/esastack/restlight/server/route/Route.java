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
package io.esastack.restlight.server.route;

import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.route.impl.RouteImpl;
import io.esastack.restlight.server.schedule.Scheduler;
import io.esastack.restlight.server.schedule.Scheduling;

/**
 * A Route maintains a {@link Mapping} which will be used to determine whether an {@link HttpRequest} should be routed
 * to it.
 */
public interface Route extends Routing, Scheduling {

    /**
     * Creates an instance {@link RouteImpl} for building a new {@link Route}.
     *
     * @return {@link RouteImpl}
     */
    static RouteImpl route(Scheduler scheduler) {
        return new RouteImpl(null,
                null,
                scheduler,
                null);
    }

    /**
     * Creates an instance {@link RouteImpl} for building a new {@link Route}.
     *
     * @return  {@link RouteImpl}
     */
    static RouteImpl route() {
        return new RouteImpl(null, null, null, null);
    }

    /**
     * Creates an instance {@link RouteImpl} for building a new {@link Route}.
     *
     * @return  {@link RouteImpl}
     */
    static RouteImpl route(Mapping mapping) {
        return new RouteImpl(mapping, null, null, null);
    }

    /**
     * Creates an instance of {@link RouteImpl} from given {@code another}.
     *
     * @param another   other
     * @return  {@link RouteImpl}.
     */
    static RouteImpl route(Route another) {
        return new RouteImpl(another.mapping(),
                another.executionFactory(),
                another.scheduler(),
                another.handler().orElse(null));
    }

    /**
     * Obtains the {@link ExecutionFactory} to handle the request.
     *
     * @return      execution handler
     */
    ExecutionFactory executionFactory();

}
