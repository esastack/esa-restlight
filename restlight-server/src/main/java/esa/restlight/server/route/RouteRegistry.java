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

/**
 * Interface defines a Registry for the {@link Route}s. This interface allows the application to register {@link Route}s
 * and transfer current {@link RouteRegistry} to a {@link ReadOnlyRouteRegistry} after initializing which means it is
 * not allowed to register any {@link Route} to this registry.
 * @see ReadOnlyRouteRegistry
 */
public interface RouteRegistry extends ReadOnlyRouteRegistry {

    /**
     * Register the given {@link Route}.
     *
     * @param route route for registering
     */
    void registerRoute(Route route);

    /**
     * Transfers current registry to a {@link ReadOnlyRouteRegistry}
     *
     * @return transferred mapper registry
     */
    default ReadOnlyRouteRegistry toReadOnly() {
        return this;
    }

}
