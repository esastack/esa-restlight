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

import java.util.List;

/**
 * Interface defines a Registry for the {@link Route}s.
 */
public interface RouteRegistry {

    /**
     * Gets all {@link Route}s.
     *
     * @return routes
     */
    List<Route> routes();

    /**
     * Register the given {@link Route}.
     *
     * @param route route for registering
     */
    void register(Route route);

    /**
     * DeRegister the given {@link Route}.
     *
     * @param route route
     */
    void deRegister(Route route);

}
