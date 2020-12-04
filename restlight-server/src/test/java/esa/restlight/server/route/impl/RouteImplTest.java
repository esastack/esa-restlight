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
import esa.httpserver.core.AsyncResponse;
import esa.restlight.server.route.Mapping;
import esa.restlight.server.route.Route;
import esa.restlight.server.route.RouteExecution;
import esa.restlight.server.schedule.Scheduler;
import esa.restlight.server.schedule.Schedulers;
import esa.restlight.server.util.Futures;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RouteImplTest {

    @Test
    void testConstruct() {
        assertNotNull(Route.route());
        assertNotNull(Route.route().handler());
        assertFalse(Route.route().handler().isPresent());
        assertNotNull(Route.route().mapping());
        assertNull(Route.route().scheduler());


        assertNotNull(Route.route(Mapping.get("/foo")));
        assertNotNull(Route.route().mapping());
        assertEquals(Mapping.get("/foo"), Route.route(Mapping.get("/foo")).mapping());


        final AtomicInteger subject = new AtomicInteger(0);
        final Object handler = new Object();
        final Route route = Route.route(Mapping.get("/foo"))
                .handleAsync((request, response) -> {
                    subject.set(1);
                    return Futures.completedFuture();
                })
                .onErrorAsync((request, response, throwable) -> {
                    subject.set(2);
                    return Futures.completedFuture();
                })
                .onCompleteAsync((request, response, throwable) -> {
                    subject.set(3);
                    return Futures.completedFuture();
                })
                .schedule(Schedulers.io())
                .handlerObject(handler);
        final Route another = Route.route(route);
        assertRouteExecution(subject, another);
        assertEquals(Mapping.get("/foo"), another.mapping());
        assertEquals(Schedulers.io(), another.scheduler());
        assertTrue(another.handler().isPresent());
        assertEquals(handler, another.handler().get());
    }

    @Test
    void testArgs() {
        final Mapping mapping = Mapping.get("/foo");
        final Scheduler scheduler = Schedulers.io();
        final Object handler = new Object();
        final AtomicInteger subject = new AtomicInteger(0);
        final Route route = Route.route(mapping)
                .handleAsync((request, response) -> {
                    subject.set(1);
                    return Futures.completedFuture();
                })
                .onErrorAsync((request, response, throwable) -> {
                    subject.set(2);
                    return Futures.completedFuture();
                })
                .onCompleteAsync((request, response, throwable) -> {
                    subject.set(3);
                    return Futures.completedFuture();
                })
                .schedule(scheduler)
                .handlerObject(handler);
        assertRouteExecution(subject, route);
        assertEquals(mapping, route.mapping());
        assertEquals(scheduler, route.scheduler());
        assertTrue(route.handler().isPresent());
        assertEquals(handler, route.handler().get());

        final Route route1 = Route.route(mapping)
                .handleAsync((request) -> {
                    subject.set(1);
                    return Futures.completedFuture();
                })
                .onErrorAsync((request, throwable) -> {
                    subject.set(2);
                    return Futures.completedFuture();
                })
                .onCompleteAsync((request, throwable) -> {
                    subject.set(3);
                    return Futures.completedFuture();
                });
        assertRouteExecution(subject, route1);

        final Route route2 = Route.route(mapping)
                .handleAsync(() -> {
                    subject.set(1);
                    return Futures.completedFuture();
                })
                .onErrorAsync((throwable) -> {
                    subject.set(2);
                    return Futures.completedFuture();
                })
                .onCompleteAsync((throwable) -> {
                    subject.set(3);
                    return Futures.completedFuture();
                });
        assertRouteExecution(subject, route2);

        final Route route3 = Route.route(mapping)
                .handleAsync(() -> {
                    subject.set(1);
                    return Futures.completedFuture();
                })
                .onErrorAsync(() -> {
                    subject.set(2);
                    return Futures.completedFuture();
                })
                .onCompleteAsync(() -> {
                    subject.set(3);
                    return Futures.completedFuture();
                });
        assertRouteExecution(subject, route3);

        final Route route4 = Route.route(mapping)
                .handle((request, response) -> subject.set(1))
                .onError((request, response, throwable) -> subject.set(2))
                .onComplete((request, response, throwable) -> subject.set(3));
        assertRouteExecution(subject, route4);

        final Route route5 = Route.route(mapping)
                .handle((request) -> subject.set(1))
                .onError((request, throwable) -> subject.set(2))
                .onComplete((response, throwable) -> subject.set(3));
        assertRouteExecution(subject, route5);

        final Route route6 = Route.route(mapping)
                .handle(() -> subject.set(1))
                .onError((throwable) -> subject.set(2))
                .onComplete((throwable) -> subject.set(3));
        assertRouteExecution(subject, route6);

        final Route route7 = Route.route(mapping)
                .handle(() -> subject.set(1))
                .onError(() -> subject.set(2))
                .onComplete(() -> subject.set(3));
        assertRouteExecution(subject, route7);
    }

    @Test
    void testToString() {
        final Route r = Route.route();
        final String str = r.toString();
        assertNotNull(str);
        assertSame(str, r.toString());

        assertNotNull(Route.route(Mapping.get("/foo"))
                .schedule(Schedulers.io())
                .handlerObject(new Object()));
    }

    private void assertRouteExecution(AtomicInteger a,
                                      Route route) {
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final RouteExecution execution = route.toExecution(MockAsyncRequest.aMockRequest().build());
        execution.handle(request, response).join();
        assertEquals(1, a.get());
        execution.exceptionHandler().handleException(request, response, new RuntimeException()).join();
        assertEquals(2, a.get());
        execution.completionHandler().onComplete(request, response, new RuntimeException()).join();
        assertEquals(3, a.get());
    }

}
