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
import esa.restlight.server.route.Mapping;
import esa.restlight.server.route.Route;
import esa.restlight.server.route.RouteRegistry;
import esa.restlight.server.schedule.RequestTask;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultDispatcherHandlerTest {

    @Test
    void testRoute() {
        final RouteRegistry registry = mock(RouteRegistry.class);
        final DefaultDispatcherHandler dispatcher = new DefaultDispatcherHandler(registry,
                exceptionHandlers());

        final Route r = Route.route();
        when(registry.route(any())).thenReturn(r);
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        assertSame(r, dispatcher.route(request, response));
        verify(registry, times(1)).route(same(request));

        final List<Route> routes = Collections.singletonList(r);
        when(registry.routes()).thenReturn(routes);
        assertSame(routes, dispatcher.routes());
    }

    @Test
    void testHandleRejectWork() {
        final RouteRegistry registry = mock(RouteRegistry.class);
        final DefaultDispatcherHandler dispatcher = new DefaultDispatcherHandler(registry,
                exceptionHandlers());


        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        CompletableFuture<Void> cf = new CompletableFuture<>();
        final RequestTask task = mock(RequestTask.class);
        when(task.request()).thenReturn(request);
        when(task.response()).thenReturn(response);
        when(task.promise()).thenReturn(cf);


        dispatcher.handleRejectedWork(task, "foo");
        assertEquals(1, dispatcher.rejectCount());

        reset(task);
        cf = new CompletableFuture<>();
        when(task.request()).thenReturn(request);
        when(task.response()).thenReturn(response);
        when(task.promise()).thenReturn(cf);

        dispatcher.handleRejectedWork(task, "foo");
        assertTrue(cf.isDone());
        cf.join();
        assertTrue(response.isCommitted());
        assertEquals(429, response.status());
        assertNotNull(response.getSentData().toString(StandardCharsets.UTF_8));
        assertEquals(2, dispatcher.rejectCount());

        final MockAsyncResponse response1 = MockAsyncResponse.aMockResponse().build();
        when(task.response()).thenReturn(response1);
        response1.sendResult();
        final CompletableFuture<Void> cf1 = new CompletableFuture<>();
        when(task.promise()).thenReturn(cf1);
        dispatcher.handleRejectedWork(task, "foo");

        assertTrue(cf1.isDone());
        assertEquals(3, dispatcher.rejectCount());
    }

    @Test
    void testHandleUnfinishedWork() {
        final RouteRegistry registry = mock(RouteRegistry.class);
        final DefaultDispatcherHandler dispatcher = new DefaultDispatcherHandler(registry,
                exceptionHandlers());

        final RequestTask task1 = mock(RequestTask.class);
        when(task1.request()).thenReturn(MockAsyncRequest.aMockRequest().build());
        when(task1.response()).thenReturn(MockAsyncResponse.aMockResponse().build());
        when(task1.promise()).thenReturn(new CompletableFuture<>());

        final RequestTask task2 = mock(RequestTask.class);
        when(task2.request()).thenReturn(MockAsyncRequest.aMockRequest().build());
        when(task2.response()).thenReturn(MockAsyncResponse.aMockResponse().build());
        when(task2.promise()).thenReturn(new CompletableFuture<>());

        task2.response().sendResult();
        dispatcher.handleUnfinishedWorks(Arrays.asList(task1, task2));

        assertTrue(task1.promise().isDone());
        assertTrue(task1.response().isCommitted());
        assertEquals(HttpResponseStatus.SERVICE_UNAVAILABLE.code(), task1.response().status());
        assertTrue(task2.promise().isDone());
    }

    @Test
    void testServiceWithErrorInToExecution() {
        final RouteRegistry registry = mock(RouteRegistry.class);
        final DefaultDispatcherHandler dispatcher = new DefaultDispatcherHandler(registry,
                exceptionHandlers());

        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final CompletableFuture<Void> cf = new CompletableFuture<>();
        final Route r = mock(Route.class);
        when(r.toExecution(same(request))).thenThrow(new IllegalStateException());
        dispatcher.service(request, response, cf, r);

        assertTrue(cf.isDone());
        cf.join();
        assertTrue(response.isCommitted());
        assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), response.status());
    }

    @Test
    void testNormalService() {
        final RouteRegistry registry = mock(RouteRegistry.class);
        final DefaultDispatcherHandler dispatcher = new DefaultDispatcherHandler(registry,
                exceptionHandlers());

        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final CompletableFuture<Void> cf = new CompletableFuture<>();

        final Route r = Route.route(Mapping.get())
                .handle((req, res) -> req.setAttribute("h", 1))
                .onComplete((asyncRequest, asyncResponse, throwable) -> asyncRequest.setAttribute("c", 1));

        dispatcher.service(request, response, cf, r);
        assertTrue(cf.isDone());
        cf.join();
        assertTrue(response.isCommitted());
        assertEquals(HttpResponseStatus.OK.code(), response.status());
        assertEquals(1, request.getAttribute("h"));
        assertEquals(1, request.getAttribute("c"));
    }

    @Test
    void testServiceOnException() {
        final RouteRegistry registry = mock(RouteRegistry.class);
        final DefaultDispatcherHandler dispatcher = new DefaultDispatcherHandler(registry,
                exceptionHandlers());

        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final CompletableFuture<Void> cf = new CompletableFuture<>();

        final Route r = Route.route(Mapping.get())
                .handle((req, res) -> {
                    req.setAttribute("h", 1);
                    ExceptionUtils.throwException(new IllegalStateException("foo"));
                })
                .onError((asyncRequest, asyncResponse, throwable) -> {
                    if (throwable != null) {
                        request.setAttribute("e", 1);
                        ExceptionUtils.throwException(throwable);
                    }
                })
                .onComplete((asyncRequest, asyncResponse, throwable) -> {
                    if (throwable != null) {
                        asyncRequest.setAttribute("c", 1);
                    }
                });

        dispatcher.service(request, response, cf, r);
        assertTrue(cf.isDone());
        cf.join();
        assertTrue(response.isCommitted());
        assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), response.status());
        assertEquals(1, request.getAttribute("h"));
        assertEquals(1, request.getAttribute("e"));
        assertEquals(1, request.getAttribute("c"));
    }

    @Test
    void testServiceEndWith200WhenExceptionHandled() {
        final RouteRegistry registry = mock(RouteRegistry.class);
        final DefaultDispatcherHandler dispatcher = new DefaultDispatcherHandler(registry,
                exceptionHandlers());

        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final CompletableFuture<Void> cf = new CompletableFuture<>();

        final Route r = Route.route(Mapping.get())
                .handle((req, res) -> {
                    req.setAttribute("h", 1);
                    ExceptionUtils.throwException(new IllegalStateException("foo"));
                })
                .onError((asyncRequest, asyncResponse, throwable) -> {
                    if (throwable != null) {
                        request.setAttribute("e", 1);
                    }
                })
                .onComplete((asyncRequest, asyncResponse, throwable) -> {
                    if (throwable == null) {
                        asyncRequest.setAttribute("c", 1);
                    }
                });

        dispatcher.service(request, response, cf, r);
        assertTrue(cf.isDone());
        cf.join();
        assertTrue(response.isCommitted());
        assertEquals(HttpResponseStatus.OK.code(), response.status());
        assertEquals(1, request.getAttribute("h"));
        assertEquals(1, request.getAttribute("e"));
        assertEquals(1, request.getAttribute("c"));
    }

    @Test
    void testServiceOnHandleAsyncError() {
        final RouteRegistry registry = mock(RouteRegistry.class);
        final DefaultDispatcherHandler dispatcher = new DefaultDispatcherHandler(registry,
                exceptionHandlers());

        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final CompletableFuture<Void> cf = new CompletableFuture<>();

        final Route r = Route.route(Mapping.get())
                .handleAsync((req, res) -> {
                    req.setAttribute("h", 1);
                    throw new IllegalStateException("foo");
                })
                .onError((asyncRequest, asyncResponse, throwable) -> {
                    request.setAttribute("e", 1);
                })
                .onComplete((asyncRequest, asyncResponse, throwable) -> {
                    if (throwable != null) {
                        asyncRequest.setAttribute("c", 1);
                    }
                });

        dispatcher.service(request, response, cf, r);
        assertTrue(cf.isDone());
        cf.join();
        assertTrue(response.isCommitted());
        assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), response.status());
        assertEquals(1, request.getAttribute("h"));
        assertNull(request.getAttribute("e"));
        assertEquals(1, request.getAttribute("c"));
    }

    private List<DispatcherExceptionHandler> exceptionHandlers() {
        return Collections.singletonList(new DefaultDispatcherExceptionHandler());
    }
}
