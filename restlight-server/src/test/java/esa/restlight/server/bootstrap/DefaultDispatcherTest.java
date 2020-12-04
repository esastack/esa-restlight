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
package esa.restlight.server.bootstrap;

import esa.commons.ExceptionUtils;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.server.route.Route;
import esa.restlight.server.route.RouteRegistry;
import esa.restlight.server.route.impl.SimpleRouteRegistry;
import esa.restlight.server.schedule.RequestTask;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static esa.restlight.server.route.Mapping.get;
import static esa.restlight.server.route.Mapping.post;
import static esa.restlight.server.route.Route.route;
import static org.junit.jupiter.api.Assertions.*;

class DefaultDispatcherTest {

    @Test
    void testEmptyRouteRegistry() {
        final RouteRegistry registry = new SimpleRouteRegistry();
        final DispatcherHandler dispatcher = new DefaultDispatcherHandler(registry);
        assertTrue(dispatcher.routes().isEmpty());
        final AsyncRequest req = MockAsyncRequest.aMockRequest().withUri("/foo").build();
        final AsyncResponse res = MockAsyncResponse.aMockResponse().build();
        assertNull(dispatcher.route(req, res));
    }

    @Test
    void testRoutes() {
        final RouteRegistry registry = new SimpleRouteRegistry();
        registry.registerRoute(route(get("/foo")));
        registry.registerRoute(route(post("/bar")));
        final DispatcherHandler dispatcher = new DefaultDispatcherHandler(registry);
        assertEquals(2, dispatcher.routes().size());
        assertNotNull(dispatcher.route(MockAsyncRequest.aMockRequest().withUri("/foo").build(),
                MockAsyncResponse.aMockResponse().build()));

        assertNotNull(dispatcher.route(
                MockAsyncRequest.aMockRequest()
                        .withUri("/bar")
                        .withMethod(HttpMethod.POST)
                        .build(),
                MockAsyncResponse.aMockResponse()
                        .build()));
    }

    @Test
    void testService() {
        final RouteRegistry registry = new SimpleRouteRegistry();
        final DispatcherHandler dispatcher = new DefaultDispatcherHandler(registry);
        final Req task = new Req();
        dispatcher.service(task.req, task.res, task.promise, Route.route());
        task.promise.join();
        assertEquals(200, task.res.status());
        assertTrue(task.promise.isDone());
        assertFalse(task.promise.isCompletedExceptionally());
    }

    @Test
    void testServiceWithException() {
        final RouteRegistry registry = new SimpleRouteRegistry();
        final DispatcherHandler dispatcher = new DefaultDispatcherHandler(registry);
        final Req task = new Req();
        dispatcher.service(task.req, task.res, task.promise, Route.route()
                .handle(() -> ExceptionUtils.throwException(new RuntimeException("a"))));
        task.promise.join();
        assertEquals(500, task.res.status());
        assertTrue(task.promise.isDone());
        assertFalse(task.promise.isCompletedExceptionally());
    }

    @Test
    void testServiceWithExceptionAndHandler() {
        final RouteRegistry registry = new SimpleRouteRegistry();
        final DispatcherHandler dispatcher = new DefaultDispatcherHandler(registry);
        final Req task = new Req();
        final AtomicBoolean complete = new AtomicBoolean(false);
        dispatcher.service(task.req, task.res, task.promise, Route.route()
                .handle(() -> ExceptionUtils.throwException(new RuntimeException("a")))
                .onError((request, response, throwable) -> response.setStatus(520))
                .onComplete(() -> complete.set(true)));
        task.promise.join();
        assertEquals(520, task.res.status());
        assertTrue(task.promise.isDone());
        assertFalse(task.promise.isCompletedExceptionally());
        assertTrue(complete.get());
    }

    @Test
    void testHandleRejected() {
        final RouteRegistry registry = new SimpleRouteRegistry();
        final DispatcherHandler dispatcher = new DefaultDispatcherHandler(registry);
        Req task = new Req();
        dispatcher.handleRejectedWork(task, "error");
        assertEquals(429, task.res.status());
        assertTrue(task.promise.isDone());
        assertFalse(task.promise.isCompletedExceptionally());
    }

    @Test
    void testHandleUnfinished() {
        final RouteRegistry registry = new SimpleRouteRegistry();
        final DispatcherHandler dispatcher = new DefaultDispatcherHandler(registry);
        final List<RequestTask> tasks = Arrays.asList(new Req(), new Req());
        dispatcher.handleUnfinishedWorks(tasks);
        tasks.forEach(t -> {
            assertEquals(HttpResponseStatus.SERVICE_UNAVAILABLE.code(), ((Req) t).res.status());
            assertTrue(((Req) t).promise.isDone());
            assertFalse(((Req) t).promise.isCompletedExceptionally());
        });
    }

    @Test
    void testHandleUnexpectedError() {
        final RouteRegistry registry = new SimpleRouteRegistry();
        final DispatcherHandler dispatcher = new DefaultDispatcherHandler(registry);
        final Req task = new Req();
        dispatcher.handleUnexpectedError(task.req, task.res, new RuntimeException("foo"), task.promise);
        assertEquals(500, task.res.status());
        assertTrue(task.promise.isDone());
        assertFalse(task.promise.isCompletedExceptionally());

        final Req task1 = new Req();
        dispatcher.handleUnexpectedError(task1.req, task1.res, WebServerException.BAD_REQUEST, task1.promise);
        assertEquals(400, task1.res.status());
        assertTrue(task1.promise.isDone());
        assertFalse(task1.promise.isCompletedExceptionally());
    }

    private static class Req implements RequestTask {

        private final AsyncRequest req;
        private final AsyncResponse res;
        private final CompletableFuture<Void> promise;

        private Req() {
            this.req = MockAsyncRequest.aMockRequest().build();
            this.res = MockAsyncResponse.aMockResponse().build();
            this.promise = new CompletableFuture<>();
        }

        @Override
        public AsyncRequest request() {
            return req;
        }

        @Override
        public AsyncResponse response() {
            return res;
        }

        @Override
        public CompletableFuture<Void> promise() {
            return promise;
        }

        @Override
        public void run() {
        }
    }

}
