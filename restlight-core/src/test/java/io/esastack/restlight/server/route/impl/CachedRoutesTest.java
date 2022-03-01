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
package io.esastack.restlight.server.route.impl;

import io.esastack.restlight.server.route.Mapping;
import io.esastack.restlight.server.route.Route;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CachedRoutesTest {

    @Test
    void testHitsAndLookup() {
        doTest(routes -> {
            CachedRoutes cachedRoutes = new UnsafeCachedRoutes(1000);
            Arrays.stream(routes).forEach(cachedRoutes::add);
            return cachedRoutes;
        });
        doTest(routes -> {
            CachedRoutes cachedRoutes = new DefaultCachedRoutes(1000);
            Arrays.stream(routes).forEach(cachedRoutes::add);
            return cachedRoutes;
        });
    }

    private void doTest(Function<CountedRoute[], CachedRoutes> func) {
        final CountedRoute[] crs = new CountedRoute[3];
        final CountedRoute r0 = new CountedRoute(Route.route(Mapping.get("/foo")));
        final CountedRoute r1 = new CountedRoute(Route.route(Mapping.get("/bar")));
        final CountedRoute r2 = new CountedRoute(Route.route(Mapping.get("/baz")));
        crs[0] = r0;
        crs[1] = r1;
        crs[2] = r2;
        CachedRoutes routes = func.apply(crs);
        assertArrayEquals(crs, routes.lookup());
        routes.hit(r2);
        assertEquals(r2, routes.lookup()[0]);
        routes.hit(r1);
        routes.hit(r1);
        assertEquals(r1, routes.lookup()[0]);
        assertEquals(r2, routes.lookup()[1]);
    }

}
