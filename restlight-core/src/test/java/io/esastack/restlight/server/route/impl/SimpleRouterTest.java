/*
 * Copyright 2022 OPPO ESA Stack Project
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

import io.esastack.commons.net.http.HttpMethod;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.mock.MockHttpRequest;
import org.junit.jupiter.api.Test;

import static io.esastack.restlight.server.route.Mapping.get;
import static io.esastack.restlight.server.route.Mapping.post;
import static io.esastack.restlight.server.route.Route.route;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class SimpleRouterTest {

    protected AbstractRouter buildRouter() {
        return new SimpleRouter();
    }

    @Test
    void testAddRoute() {
        final AbstractRouter router = buildRouter();
        router.add(new RouteWrap(route(get("/foo"))));
        router.add(new RouteWrap(route(post("/foo"))));
        assertEquals(2, router.mappingLookups.size());
    }

    @Test
    void testRemoveRoute() {
        final AbstractRouter router = buildRouter();
        router.add(new RouteWrap(route(get("/foo"))));
        router.add(new RouteWrap(route(post("/foo"))));
        assertEquals(2, router.mappingLookups.size());
        router.remove(new RouteWrap(route(post("/foo"))));
        assertEquals(1, router.mappingLookups.size());
    }

    @Test
    void testMatchByUrl() {
        final AbstractRouter router = buildRouter();
        router.add(new RouteWrap(route(get("/foo"))));
        router.add(new RouteWrap(route(post("/foo"))));
        HttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/foo")
                .withMethod(HttpMethod.GET)
                .build();
        assertEquals(new RouteWrap(route(get("/foo"))),
                router.matchByUri(new RequestContextImpl(request, mock(HttpResponse.class))));

        router.remove(new RouteWrap(route(post("/foo"))));
        assertEquals(new RouteWrap(route(get("/foo"))),
                router.matchByUri(new RequestContextImpl(request, mock(HttpResponse.class))));

        request = MockHttpRequest.aMockRequest()
                .withUri("/foo")
                .withMethod(HttpMethod.POST)
                .build();

        assertNull(router.matchByUri(new RequestContextImpl(request, mock(HttpResponse.class))));
    }

    @Test
    void testMatchByAll() {
        SimpleRouter router = new SimpleRouter();
        router.add(new RouteWrap(route(get("/foo"))));
        router.add(new RouteWrap(route(post("/foo"))));
        HttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/foo")
                .withMethod(HttpMethod.GET)
                .build();
        assertEquals(new RouteWrap(route(get("/foo"))),
                router.matchAll(new RequestContextImpl(request, mock(HttpResponse.class))));

        router.remove(new RouteWrap(route(post("/foo"))));
        assertEquals(new RouteWrap(route(get("/foo"))),
                router.matchAll(new RequestContextImpl(request, mock(HttpResponse.class))));

        request = MockHttpRequest.aMockRequest()
                .withUri("/foo")
                .withMethod(HttpMethod.POST)
                .build();

        assertNull(router.matchAll(new RequestContextImpl(request, mock(HttpResponse.class))));
    }
}
