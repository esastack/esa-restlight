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

import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.mock.MockHttpResponse;
import io.esastack.restlight.server.route.Mapping;
import io.esastack.restlight.server.route.Route;
import io.esastack.restlight.server.route.RouteExecution;
import io.esastack.restlight.server.schedule.Scheduler;
import io.esastack.restlight.server.schedule.Schedulers;
import io.esastack.restlight.server.util.Futures;
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
                .handleAsync((ctx) -> {
                    subject.set(1);
                    return Futures.completedFuture();
                })
                .onErrorAsync((throwable) -> {
                    subject.set(2);
                    return Futures.completedFuture();
                })
                .onCompleteAsync(() -> {
                    subject.set(3);
                    return Futures.completedFuture();
                })
                .scheduler(Schedulers.io())
                .handler(handler);
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
                .handleAsync((ctx) -> {
                    subject.set(1);
                    return Futures.completedFuture();
                })
                .onErrorAsync((throwable) -> {
                    subject.set(2);
                    return Futures.completedFuture();
                })
                .onCompleteAsync(() -> {
                    subject.set(3);
                    return Futures.completedFuture();
                })
                .scheduler(scheduler)
                .handler(handler);
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
                .onErrorAsync((throwable) -> {
                    subject.set(2);
                    return Futures.completedFuture();
                })
                .onCompleteAsync(() -> {
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
                .onCompleteAsync(() -> {
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
                .handle((ctx) -> subject.set(1))
                .onError((throwable) -> subject.set(2))
                .onComplete(() -> subject.set(3));
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
                .scheduler(Schedulers.io())
                .handler(new Object()));
    }

    private void assertRouteExecution(AtomicInteger a,
                                      Route route) {
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        RequestContext context = new RequestContextImpl(request, response);
        final RouteExecution execution = route.executionFactory().create(context);
        execution.handle(context).toCompletableFuture().join();
        assertEquals(1, a.get());
        execution.exceptionHandler().handleException(context, new RuntimeException())
                .toCompletableFuture().join();
        assertEquals(2, a.get());
        execution.completionHandler().onComplete(context, new RuntimeException())
                .toCompletableFuture().join();
        assertEquals(3, a.get());
    }

}
