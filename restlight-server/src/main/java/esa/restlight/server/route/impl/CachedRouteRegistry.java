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

import esa.commons.concurrent.UnsafeUtils;
import esa.httpserver.core.AsyncRequest;
import esa.restlight.server.route.ReadOnlyRouteRegistry;

import java.util.List;
import java.util.function.Function;

/**
 * Implementation of RouteRegistry.
 * <p>
 * As different as {@link SimpleRouteRegistry}, this implementation uses a cache of the Route to speed up the url
 * routing. The algorithm used in {@link CachedRouteRegistry} is same with the LFU(Least Frequently Used), but we do not
 * update the cache immediately when the element is accessed, instead, we update it in a certain probability(default to
 * 0.1 percent).
 */

public class CachedRouteRegistry extends AbstractRouteRegistry {

    private final int computeRate;

    public CachedRouteRegistry(int computeRate) {
        this.computeRate = computeRate;
    }

    @Override
    ReadOnlyRouteRegistry toReadOnly(List<RouteWrap> mappingLookup) {
        return new Cached(mappingLookup);
    }

    class Cached extends AbstractReadOnlyRouteRegistry<CountedRoute, CachedRoutes> {
        Cached(List<RouteWrap> mappingLookup) {
            super(mappingLookup);
        }

        @Override
        CachedRoutes toRoutes(CountedRoute[] routes) {
            return UnsafeUtils.hasUnsafe()
                    ? new UnsafeCachedRoutes(routes, computeRate)
                    : new DefaultCachedRoutes(routes, computeRate);
        }

        @Override
        Function<Integer, CountedRoute[]> toArray() {
            return CountedRoute[]::new;
        }

        @Override
        CountedRoute route(RouteWrap routeWrap) {
            return new CountedRoute(routeWrap);
        }

        @Override
        CountedRoute findFor(CountedRoute[] routes, AsyncRequest request) {
            CountedRoute found = super.findFor(routes, request);
            if (found != null) {
                this.routes.hit(found);
            }
            return found;
        }
    }
}
