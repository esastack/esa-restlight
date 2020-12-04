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

import esa.httpserver.core.AsyncRequest;
import esa.restlight.server.route.Route;
import esa.restlight.server.route.RouteRegistry;
import esa.restlight.test.mock.MockAsyncRequest;
import org.junit.jupiter.api.Test;

import static esa.restlight.server.route.Mapping.get;
import static esa.restlight.server.route.Route.route;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DefaultRouteRegistryTest {


    protected RouteRegistry buildMapperRegistry() {
        return new SimpleRouteRegistry();
    }

    @Test
    void testDirectUrlMapping() {
        final RouteRegistry registry = buildMapperRegistry();
        registry.registerRoute(route(get("/foo")));

        final AsyncRequest request = MockAsyncRequest.aMockRequest().withUri("/foo").build();
        final Route route = registry.toReadOnly().route(request);
        assertNotNull(route);
        final AsyncRequest request1 = MockAsyncRequest.aMockRequest().withUri("/bar").build();
        final Route route1 = registry.toReadOnly().route(request1);
        assertNull(route1);
    }

    @Test
    void testPatternUrlMapping() {
        final RouteRegistry registry = buildMapperRegistry();
        registry.registerRoute(route(get("/foo/{foo}")));
        registry.registerRoute(route(get("/foo/**")));
        final AsyncRequest request = MockAsyncRequest.aMockRequest().withUri("/foo/bar").build();
        final Route route = registry.toReadOnly().route(request);
        assertNotNull(route);
        final AsyncRequest request1 = MockAsyncRequest.aMockRequest().withUri("/foo/bar/baz").build();
        final Route route1 = registry.toReadOnly().route(request1);
        assertNotNull(route1);
    }

}
