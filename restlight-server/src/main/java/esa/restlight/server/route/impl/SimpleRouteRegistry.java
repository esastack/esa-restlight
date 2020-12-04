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

import esa.restlight.server.route.ReadOnlyRouteRegistry;

import java.util.List;
import java.util.function.Function;

public class SimpleRouteRegistry extends AbstractRouteRegistry {

    @Override
    ReadOnlyRouteRegistry toReadOnly(List<RouteWrap> mappingLookup) {
        return new ReadOnly(mappingLookup);
    }

    static class ReadOnly extends AbstractReadOnlyRouteRegistry<RouteWrap, FixedRoutes> {

        ReadOnly(List<RouteWrap> mappingLookup) {
            super(mappingLookup);
        }

        @Override
        FixedRoutes toRoutes(RouteWrap[] routes) {
            return new FixedRoutes(routes);
        }

        @Override
        Function<Integer, RouteWrap[]> toArray() {
            return RouteWrap[]::new;
        }

        @Override
        RouteWrap route(RouteWrap routeWrap) {
            return routeWrap;
        }
    }

    static class FixedRoutes implements Routes<RouteWrap> {

        private final RouteWrap[] routeWraps;

        FixedRoutes(RouteWrap[] routeWraps) {
            this.routeWraps = routeWraps;
        }

        @Override
        public RouteWrap[] lookup() {
            return routeWraps;
        }
    }
}
