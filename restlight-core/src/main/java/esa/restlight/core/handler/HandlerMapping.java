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
package esa.restlight.core.handler;

import esa.restlight.server.route.Mapping;

/**
 * Interface defines a couple elements of a handler.
 */
public interface HandlerMapping {

    /**
     * Gets the {@link Mapping}.
     *
     * @return mapping
     */
    Mapping mapping();

    /**
     * Gets the {@link RouteHandler}.
     *
     * @return route handler
     */
    RouteHandler handler();
}
