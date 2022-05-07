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
package io.esastack.restlight.core.server;

import esa.commons.ExceptionUtils;
import esa.commons.collection.AttributeKey;
import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.dispatcher.DispatcherHandler;
import io.esastack.restlight.core.dispatcher.DispatcherHandlerImpl;
import io.esastack.restlight.core.dispatcher.IExceptionHandler;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.core.mock.MockHttpResponse;
import io.esastack.restlight.core.route.Mapping;
import io.esastack.restlight.core.route.Route;
import io.esastack.restlight.core.route.impl.RoutableRegistry;
import io.esastack.restlight.core.server.processor.schedule.RequestTask;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DispatcherHandlerImplTest {

    @Test
    void testRoute() {
        final RoutableRegistry registry = mock(RoutableRegistry.class);
        final DispatcherHandler dispatcher = new DispatcherHandlerImpl(registry,
                exceptionHandlers().toArray(new IExceptionHandler[0]));

        final Route r = Route.route();
        when(registry.route(any())).thenReturn(r);
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext context = new RequestContextImpl(request, response);
        assertSame(r, dispatcher.route(context));
        verify(registry, times(1)).route(same(context));

        final List<Route> routes = Collections.singletonList(r);
        when(registry.routes()).thenReturn(routes);
        assertSame(routes.size(), dispatcher.routes().size());
    }

    @Test
    void testHandleRejectWork() {
        final RoutableRegistry registry = mock(RoutableRegistry.class);
        final DispatcherHandler dispatcher = new DispatcherHandlerImpl(registry,
                exceptionHandlers().toArray(new IExceptionHandler[0]));

        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
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
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.code(), response.status());
        assertNotNull(response.entity());
        assertEquals(2, dispatcher.rejectCount());
    }

    @Test
    void testHandleUnfinishedWork() {
        final RoutableRegistry registry = mock(RoutableRegistry.class);
        final DispatcherHandler dispatcher = new DispatcherHandlerImpl(registry,
                exceptionHandlers().toArray(new IExceptionHandler[0]));

        final RequestTask task1 = mock(RequestTask.class);
        when(task1.request()).thenReturn(MockHttpRequest.aMockRequest().build());
        when(task1.response()).thenReturn(MockHttpResponse.aMockResponse().build());
        when(task1.promise()).thenReturn(new CompletableFuture<>());

        final RequestTask task2 = mock(RequestTask.class);
        when(task2.request()).thenReturn(MockHttpRequest.aMockRequest().build());
        when(task2.response()).thenReturn(MockHttpResponse.aMockResponse().build());
        when(task2.promise()).thenReturn(new CompletableFuture<>());

        dispatcher.handleUnfinishedWorks(Arrays.asList(task1, task2));
        assertTrue(task1.promise().toCompletableFuture().isDone());
        assertEquals(HttpResponseStatus.SERVICE_UNAVAILABLE.code(), task1.response().status());
        assertTrue(task2.promise().toCompletableFuture().isDone());
        assertEquals(HttpResponseStatus.SERVICE_UNAVAILABLE.code(), task2.response().status());
    }

    @Test
    void testServiceWithErrorInToExecution() {
        final RoutableRegistry registry = mock(RoutableRegistry.class);
        final DispatcherHandler dispatcher = new DispatcherHandlerImpl(registry,
                exceptionHandlers().toArray(new IExceptionHandler[0]));

        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final MockHttpResponse response = MockHttpResponse.aMockResponse().build();
        final CompletableFuture<Void> cf = new CompletableFuture<>();
        final Route r = mock(Route.class);

        when(r.executionFactory()).thenReturn(ctx -> context -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException());
            return future;
        });
        dispatcher.service(new RequestContextImpl(request, response), cf, r);

        assertTrue(cf.isDone());
        cf.join();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.code(), response.status());
    }

    @Test
    void testNormalService() {
        final RoutableRegistry registry = mock(RoutableRegistry.class);
        final DispatcherHandler dispatcher = new DispatcherHandlerImpl(registry,
                exceptionHandlers().toArray(new IExceptionHandler[0]));

        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final MockHttpResponse response = MockHttpResponse.aMockResponse().build();
        final CompletableFuture<Void> cf = new CompletableFuture<>();

        final RequestContext context = new RequestContextImpl(request, response);
        final Route r = Route.route(Mapping.get())
                .handle((ctx) -> ctx.attrs().attr(AttributeKey.valueOf("h")).set(1))
                .onComplete((ctx, throwable) -> ctx.attrs().attr(AttributeKey.valueOf("c")).set(1));

        dispatcher.service(context, cf, r);
        assertTrue(cf.isDone());
        cf.join();
        assertEquals(HttpResponseStatus.OK.code(), response.status());
        assertEquals(1, context.attrs().attr(AttributeKey.valueOf("h")).get());
        assertEquals(1, context.attrs().attr(AttributeKey.valueOf("c")).get());
    }

    @Test
    void testServiceOnException() {
        final RoutableRegistry registry = mock(RoutableRegistry.class);
        final DispatcherHandler dispatcher = new DispatcherHandlerImpl(registry,
                exceptionHandlers().toArray(new IExceptionHandler[0]));

        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final MockHttpResponse response = MockHttpResponse.aMockResponse().build();
        final CompletableFuture<Void> cf = new CompletableFuture<>();

        final RequestContext context = new RequestContextImpl(request, response);
        final Route r = Route.route(Mapping.get())
                .handle((ctx) -> {
                    ctx.attrs().attr(AttributeKey.valueOf("h")).set(1);
                    ExceptionUtils.throwException(new IllegalStateException("foo"));
                })
                .onError((ctx, throwable) -> {
                    if (throwable != null) {
                        ctx.attrs().attr(AttributeKey.valueOf("e")).set(1);
                        ExceptionUtils.throwException(throwable);
                    }
                })
                .onComplete((ctx, throwable) -> {
                    if (throwable != null) {
                        ctx.attrs().attr(AttributeKey.valueOf("c")).set(1);
                    }
                });

        dispatcher.service(context, cf, r);
        assertTrue(cf.isDone());
        cf.join();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.code(), response.status());
        assertEquals(1, context.attrs().attr(AttributeKey.valueOf("h")).get());
        assertEquals(1, context.attrs().attr(AttributeKey.valueOf("e")).get());
        assertEquals(1, context.attrs().attr(AttributeKey.valueOf("c")).get());
        assertEquals("ex", context.attrs().attr(AttributeKey.valueOf("ex")).get());
    }

    @Test
    void testServiceEndWith200WhenExceptionHandled() {
        final RoutableRegistry registry = mock(RoutableRegistry.class);
        final DispatcherHandler dispatcher = new DispatcherHandlerImpl(registry,
                exceptionHandlers().toArray(new IExceptionHandler[0]));

        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final MockHttpResponse response = MockHttpResponse.aMockResponse().build();
        final CompletableFuture<Void> cf = new CompletableFuture<>();

        final Route r = Route.route(Mapping.get())
                .handle((ctx) -> {
                    ctx.attrs().attr(AttributeKey.valueOf("h")).set(1);
                    ExceptionUtils.throwException(new IllegalStateException("foo"));
                })
                .onError((ctx, throwable) -> {
                    if (throwable != null) {
                        ctx.attrs().attr(AttributeKey.valueOf("e")).set(1);
                    }
                })
                .onComplete((ctx, throwable) -> {
                    if (throwable == null) {
                        ctx.attrs().attr(AttributeKey.valueOf("c")).set(1);
                    }
                });

        final RequestContext context = new RequestContextImpl(request, response);
        dispatcher.service(context, cf, r);
        assertTrue(cf.isDone());
        cf.join();
        assertEquals(HttpResponseStatus.OK.code(), response.status());
        assertEquals(1, context.attrs().attr(AttributeKey.valueOf("h")).get());
        assertEquals(1, context.attrs().attr(AttributeKey.valueOf("e")).get());
        assertEquals(1, context.attrs().attr(AttributeKey.valueOf("c")).get());
        assertEquals("ex", context.attrs().attr(AttributeKey.valueOf("ex")).get());
    }

    @Test
    void testServiceOnHandleAsyncError() {
        final RoutableRegistry registry = mock(RoutableRegistry.class);
        final DispatcherHandler dispatcher = new DispatcherHandlerImpl(registry,
                exceptionHandlers().toArray(new IExceptionHandler[0]));

        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final MockHttpResponse response = MockHttpResponse.aMockResponse().build();
        final CompletableFuture<Void> cf = new CompletableFuture<>();

        final Route r = Route.route(Mapping.get())
                .handleAsync(ctx -> {
                    ctx.attrs().attr(AttributeKey.valueOf("h")).set(1);
                    throw new IllegalStateException("foo");
                })
                .onError((ctx, throwable) -> {
                    if (throwable != null) {
                        ctx.attrs().attr(AttributeKey.valueOf("e")).set(1);
                    }
                })
                .onComplete((ctx, throwable) -> {
                    if (throwable != null) {
                        ctx.attrs().attr(AttributeKey.valueOf("c")).set(1);
                    }
                });

        final RequestContext context = new RequestContextImpl(request, response);
        dispatcher.service(context, cf, r);
        assertTrue(cf.isDone());
        cf.join();

        assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), response.status());
        assertEquals(1, context.attrs().attr(AttributeKey.valueOf("h")).get());
        assertNull(context.attrs().attr(AttributeKey.valueOf("e")).get());
        assertEquals(1, context.attrs().attr(AttributeKey.valueOf("c")).get());
        assertNull(context.attrs().attr(AttributeKey.valueOf("ex")).get());
    }

    private List<IExceptionHandler> exceptionHandlers() {
        return Collections.singletonList((context, th, next) -> {
            context.attrs().attr(AttributeKey.valueOf("ex")).set("ex");
            return next.handle(context, th);
        });
    }
}
