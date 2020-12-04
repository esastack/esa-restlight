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
package esa.restlight.server.route.impl;

import esa.commons.Checks;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import esa.httpserver.core.AsyncRequest;
import esa.restlight.server.route.ReadOnlyRouteRegistry;
import esa.restlight.server.route.Route;
import esa.restlight.server.route.RouteRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class AbstractRouteRegistry implements RouteRegistry {

    private static final Logger logger =
            LoggerFactory.getLogger(AbstractRouteRegistry.class);

    /**
     * all mappings
     */
    private final List<RouteWrap> mappingLookup = new ArrayList<>();

    @Override
    public void registerRoute(Route route) {
        Checks.checkNotNull(route, "route");
        final RouteWrap routeWrap = new RouteWrap(route);
        mappingLookup.forEach(registered -> {
            if (registered.predicate.mayAmbiguousWith(routeWrap.predicate)) {
                logger.warn("Found ambiguous route:\n{}\n{}", registered.route, route);
            }
        });
        mappingLookup.add(routeWrap);
        logger.debug("Registering {}", route);
    }

    @Override
    public Route route(AsyncRequest request) {
        return toReadOnly().route(request);
    }

    @Override
    public List<Route> routes() {
        return Collections.unmodifiableList(mappingLookup);
    }

    @Override
    public ReadOnlyRouteRegistry toReadOnly() {
        return toReadOnly(mappingLookup);
    }

    abstract ReadOnlyRouteRegistry toReadOnly(List<RouteWrap> mappingLookup);
}
