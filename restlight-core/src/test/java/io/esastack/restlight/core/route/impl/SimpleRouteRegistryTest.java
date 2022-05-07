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
package io.esastack.restlight.core.route.impl;

import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.core.route.Route;
import org.junit.jupiter.api.Test;

import static io.esastack.restlight.core.route.Mapping.get;
import static io.esastack.restlight.core.route.Mapping.post;
import static io.esastack.restlight.core.route.Route.route;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SimpleRouteRegistryTest {

    protected AbstractRouteRegistry buildMapperRegistry() {
        return new SimpleRouteRegistry();
    }

    @Test
    void testRegisterRoute() {
        final AbstractRouteRegistry registry = buildMapperRegistry();
        registry.register(route(get("/foo")));
        registry.register(route(post("/foo")));
        assertEquals(2, registry.routes().size());
    }

    @Test
    void testDeregisterRoute() {
        final AbstractRouteRegistry registry = buildMapperRegistry();
        registry.register(route(get("/foo")));
        registry.register(route(post("/foo")));
        registry.deRegister(route(get("/foo")));
        assertEquals(1, registry.routes().size());
    }

    @Test
    void testDirectUrlMapping() {
        final AbstractRouteRegistry registry = buildMapperRegistry();
        registry.register(route(get("/foo")));

        final HttpRequest request = MockHttpRequest.aMockRequest().withUri("/foo").build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        final Route route = registry.route(context);
        assertNotNull(route);
        final HttpRequest request1 = MockHttpRequest.aMockRequest().withUri("/bar").build();
        context = new RequestContextImpl(request1, mock(HttpResponse.class));
        final Route route1 = registry.route(context);
        assertNull(route1);
    }

    @Test
    void testPatternUrlMapping() {
        final AbstractRouteRegistry registry = buildMapperRegistry();
        registry.register(route(get("/foo/{foo}")));
        registry.register(route(get("/foo/**")));
        final HttpRequest request = MockHttpRequest.aMockRequest().withUri("/foo/bar").build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        final Route route = registry.route(context);
        assertNotNull(route);
        final HttpRequest request1 = MockHttpRequest.aMockRequest().withUri("/foo/bar/baz").build();
        context = new RequestContextImpl(request1, mock(HttpResponse.class));
        final Route route1 = registry.route(context);
        assertNotNull(route1);
        final HttpRequest request2 = MockHttpRequest.aMockRequest().withUri("/aoa/bar/baz").build();
        context = new RequestContextImpl(request2, mock(HttpResponse.class));
        final Route route2 = registry.route(context);
        assertNull(route2);
    }
}
