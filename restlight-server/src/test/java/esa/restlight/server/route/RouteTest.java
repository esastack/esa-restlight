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

import esa.commons.concurrent.DirectExecutor;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.server.schedule.Schedulers;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class RouteTest {

    @Test
    void testEmptyRoute() {
        final Route route = Route.route();
        assertNotNull(route);
        assertFalse(route.handler().isPresent());
    }

    @Test
    void testRouteWithMapping() {
        final Mapping mapping = Mapping.mapping();
        final Route route = Route.route(mapping);
        assertSame(mapping, route.mapping());
    }

    @Test
    void testFromAnotherRoute() {
        final AtomicBoolean exe = new AtomicBoolean(false);
        final Route route = Route.route(Mapping.get())
                .handle(() -> exe.set(true))
                .schedule(Schedulers.fromExecutor("foo", DirectExecutor.INSTANCE))
                .handlerObject(new Object());

        final Route newRoute = Route.route(route);

        assertSame(route.mapping(), newRoute.mapping());
        assertSame(route.scheduler(), newRoute.scheduler());
        assertSame(route.handler().orElse(null), newRoute.handler().orElse(null));

        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        route.toExecution(request).handle(request, response);
        assertTrue(exe.get());

        exe.set(false);

        newRoute.toExecution(request).handle(request, response);

        assertTrue(exe.get());
    }

}
