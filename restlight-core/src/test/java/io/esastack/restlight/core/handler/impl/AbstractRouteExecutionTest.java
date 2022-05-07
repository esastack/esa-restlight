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
package io.esastack.restlight.core.handler.impl;

import esa.commons.collection.AttributeKey;
import io.esastack.restlight.core.handler.HandlerInvoker;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.filter.RouteFilter;
import io.esastack.restlight.core.interceptor.InternalInterceptor;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.ResolvableParam;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.mock.MockHttpResponse;
import io.esastack.restlight.core.util.Futures;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractRouteExecutionTest {

    @Test
    void testConstruct() throws Exception {
        final RouteHandlerMethodAdapter mock = mock(RouteHandlerMethodAdapter.class);
        final MockHandlerData mockHandlerData = new MockHandlerData();
        List<InternalInterceptor> interceptors = new ArrayList<>();
        when(mock.context()).thenReturn(mockHandlerData.context());
        when(mock.handlerResolver()).thenReturn(mock(HandlerValueResolver.class));
        final AbstractRouteExecution execution =
                new AbstractRouteExecution(mock, interceptors) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return null;
                    }
                };
        execution.exceptionHandler();
        verify(mock, times(1)).exceptionResolver();
        assertNotNull(execution.completionHandler());
    }


    @Test
    void testApplyWithNoneInterceptor() throws Exception {
        final RouteHandlerMethodAdapter mock = mock(RouteHandlerMethodAdapter.class);
        final MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.context()).thenReturn(mockHandlerData.context());
        when(mock.handlerResolver()).thenReturn(mock(HandlerValueResolver.class));
        final AbstractRouteExecution execution =
                new AbstractRouteExecution(mock, null) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return null;
                    }
                };
        assertTrue(execution.applyPreHandle(mock(RequestContext.class), mock(Object.class))
                .toCompletableFuture()
                .join());
        CompletableFuture<Void> cf = execution.applyPostHandle(mock(RequestContext.class), mock(Object.class))
                .toCompletableFuture();
        assertTrue(cf.isDone());
        assertFalse(cf.isCompletedExceptionally());
        cf = execution.triggerAfterCompletion(mock(RequestContext.class), null).toCompletableFuture();
        assertTrue(cf.isDone());
        assertFalse(cf.isCompletedExceptionally());
    }


    @Test
    void testApplyPreHandle() throws Exception {
        final List<Integer> reached = new ArrayList<>();
        final List<InternalInterceptor> interceptors = Arrays.asList(new InternalInterceptor() {
            @Override
            public CompletionStage<Boolean> preHandle(RequestContext ctx, Object handler) {
                reached.add(0);
                return Futures.completedFuture(Boolean.TRUE);
            }
        }, new InternalInterceptor() {
            @Override
            public CompletionStage<Boolean> preHandle(RequestContext ctx, Object handler) {
                reached.add(1);
                return Futures.completedFuture(Boolean.FALSE);
            }
        }, new InternalInterceptor() {
            @Override
            public CompletionStage<Boolean> preHandle(RequestContext ctx, Object handler) {
                reached.add(2);
                return Futures.completedFuture(Boolean.TRUE);
            }
        });

        final RouteHandlerMethodAdapter mock = mock(RouteHandlerMethodAdapter.class);
        final MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.context()).thenReturn(mockHandlerData.context());
        when(mock.handlerResolver()).thenReturn(mock(HandlerValueResolver.class));
        final AbstractRouteExecution execution =
                new AbstractRouteExecution(mock, interceptors) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return null;
                    }
                };

        assertFalse(execution.applyPreHandle(mock(RequestContext.class), mock(Object.class))
                .toCompletableFuture()
                .join());
        assertEquals(2, reached.size());
        assertEquals(0, reached.get(0).intValue());
        assertEquals(1, reached.get(1).intValue());
    }

    @Test
    void testApplyPreHandleAsync() throws Exception {
        final List<Integer> reached = new ArrayList<>();
        final List<InternalInterceptor> interceptors = Arrays.asList(new InternalInterceptor() {
            @Override
            public CompletableFuture<Boolean> preHandle(RequestContext ctx, Object handler) {

                return CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    reached.add(0);
                    return true;
                });
            }
        }, new InternalInterceptor() {
            @Override
            public CompletableFuture<Boolean> preHandle(RequestContext ctx, Object handler) {

                return CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    reached.add(1);
                    return false;
                });
            }
        }, new InternalInterceptor() {
            @Override
            public CompletableFuture<Boolean> preHandle(RequestContext ctx, Object handler) {

                return CompletableFuture.supplyAsync(() -> {
                    reached.add(2);
                    return true;
                });
            }
        });
        final RouteHandlerMethodAdapter mock = mock(RouteHandlerMethodAdapter.class);
        final MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.context()).thenReturn(mockHandlerData.context());
        when(mock.handlerResolver()).thenReturn(mock(HandlerValueResolver.class));
        final AbstractRouteExecution execution =
                new AbstractRouteExecution(mock, interceptors) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return null;
                    }
                };

        assertFalse(execution.applyPreHandle(mock(RequestContext.class), mock(Object.class))
                .toCompletableFuture()
                .join());
        assertEquals(2, reached.size());
        assertEquals(0, reached.get(0).intValue());
        assertEquals(1, reached.get(1).intValue());
    }

    @Test
    void testApplyPostHandle() throws Exception {
        final List<Integer> reached = new ArrayList<>();
        final List<InternalInterceptor> interceptors = Arrays.asList(new InternalInterceptor() {
            @Override
            public CompletionStage<Void> postHandle(RequestContext ctx, Object handler) {
                reached.add(0);
                return Futures.completedFuture();
            }
        }, new InternalInterceptor() {
            @Override
            public CompletionStage<Void> postHandle(RequestContext ctx, Object handler) {
                reached.add(1);
                return Futures.completedFuture();
            }
        });
        final RouteHandlerMethodAdapter mock = mock(RouteHandlerMethodAdapter.class);
        final MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.context()).thenReturn(mockHandlerData.context());
        when(mock.handlerResolver()).thenReturn(mock(HandlerValueResolver.class));
        final AbstractRouteExecution execution =
                new AbstractRouteExecution(mock, interceptors) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return null;
                    }
                };

        execution.applyPostHandle(mock(RequestContext.class), mock(Object.class))
                .toCompletableFuture()
                .join();
        assertEquals(2, reached.size());
        assertEquals(0, reached.get(0).intValue());
        assertEquals(1, reached.get(1).intValue());
    }

    @Test
    void testApplyPostHandleAsync() throws Exception {
        final List<Integer> reached = new ArrayList<>();
        final List<InternalInterceptor> interceptors = Arrays.asList(new InternalInterceptor() {
            @Override
            public CompletableFuture<Void> postHandle(RequestContext ctx, Object handler) {
                return CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    reached.add(0);
                });
            }
        }, new InternalInterceptor() {
            @Override
            public CompletableFuture<Void> postHandle(RequestContext ctx, Object handler) {
                return CompletableFuture.runAsync(() -> reached.add(1));
            }
        });
        final RouteHandlerMethodAdapter mock = mock(RouteHandlerMethodAdapter.class);
        final MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.context()).thenReturn(mockHandlerData.context());
        when(mock.handlerResolver()).thenReturn(mock(HandlerValueResolver.class));
        final AbstractRouteExecution execution =
                new AbstractRouteExecution(mock, interceptors) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return null;
                    }
                };
        execution.applyPostHandle(mock(RequestContext.class), mock(Object.class))
                .toCompletableFuture()
                .join();
        assertEquals(2, reached.size());
        assertEquals(0, reached.get(0).intValue());
        assertEquals(1, reached.get(1).intValue());
    }

    @Test
    void testTriggerAfterCompletionWithNoneException() throws Exception {
        final AtomicBoolean reached = new AtomicBoolean(false);
        final List<InternalInterceptor> interceptors = Collections.singletonList(new InternalInterceptor() {
            @Override
            public CompletionStage<Void> afterCompletion(RequestContext ctx, Object handler, Exception t) {
                reached.set(true);
                return Futures.completedFuture();
            }
        });
        final RouteHandlerMethodAdapter mock = mock(RouteHandlerMethodAdapter.class);
        final MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.context()).thenReturn(mockHandlerData.context());
        when(mock.handlerResolver()).thenReturn(mock(HandlerValueResolver.class));
        final AbstractRouteExecution execution =
                new AbstractRouteExecution(mock, interceptors) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return null;
                    }
                };
        final CompletableFuture<Void> cf = execution.triggerAfterCompletion(mock(RequestContext.class),
                new Error()).toCompletableFuture();
        assertTrue(cf.isDone());
        assertTrue(cf.isCompletedExceptionally());
        assertFalse(reached.get());
    }

    @Test
    void testTriggerAfterCompletion() throws Exception {
        final List<Integer> reached = new ArrayList<>();
        final List<InternalInterceptor> interceptors = Arrays.asList(new InternalInterceptor() {
            @Override
            public CompletionStage<Void> afterCompletion(RequestContext ctx, Object handler, Exception t) {
                reached.add(0);
                return Futures.completedFuture();
            }
        }, new InternalInterceptor() {
            @Override
            public CompletionStage<Void> afterCompletion(RequestContext ctx, Object handler, Exception t) {
                reached.add(1);
                return Futures.completedFuture();
            }
        });
        final RouteHandlerMethodAdapter mock = mock(RouteHandlerMethodAdapter.class);
        final MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.context()).thenReturn(mockHandlerData.context());
        when(mock.handlerResolver()).thenReturn(mock(HandlerValueResolver.class));
        final AbstractRouteExecution execution =
                new AbstractRouteExecution(mock, interceptors) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return null;
                    }
                };
        execution.applyPreHandle(mock(RequestContext.class), mock(Object.class))
                .toCompletableFuture()
                .join();
        execution.triggerAfterCompletion(mock(RequestContext.class), new IllegalStateException())
                .toCompletableFuture()
                .join();

        assertEquals(2, reached.size());
        assertEquals(1, reached.get(0).intValue());
        assertEquals(0, reached.get(1).intValue());
    }

    @Test
    void testTriggerAfterCompletionWithInterruptedPreHandle() throws Exception {
        final List<Integer> reached = new ArrayList<>();
        final List<InternalInterceptor> interceptors = Arrays.asList(new InternalInterceptor() {

            @Override
            public CompletionStage<Void> afterCompletion(RequestContext ctx, Object handler, Exception t) {
                reached.add(0);
                return Futures.completedFuture();
            }
        }, new InternalInterceptor() {

            @Override
            public CompletionStage<Boolean> preHandle(RequestContext ctx, Object handler) {
                return Futures.completedFuture(Boolean.FALSE);
            }

            @Override
            public CompletionStage<Void> afterCompletion(RequestContext ctx, Object handler, Exception t) {
                reached.add(1);
                return Futures.completedFuture();
            }
        });
        final RouteHandlerMethodAdapter mock = mock(RouteHandlerMethodAdapter.class);
        final MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.context()).thenReturn(mockHandlerData.context());
        when(mock.handlerResolver()).thenReturn(mock(HandlerValueResolver.class));
        final AbstractRouteExecution execution =
                new AbstractRouteExecution(mock, interceptors) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return null;
                    }
                };
        execution.applyPreHandle(mock(RequestContext.class), mock(Object.class))
                .toCompletableFuture()
                .join();
        execution.triggerAfterCompletion(mock(RequestContext.class), new IllegalStateException())
                .toCompletableFuture()
                .join();

        assertEquals(1, reached.size());
        assertEquals(0, reached.get(0).intValue());
    }

    @Test
    void testTriggerAfterCompletionAsync() throws Exception {

        final List<Integer> reached = new ArrayList<>();
        final List<InternalInterceptor> interceptors = Arrays.asList(new InternalInterceptor() {
            @Override
            public CompletableFuture<Void> afterCompletion(RequestContext context,
                                                           Object handler,
                                                           Exception ex) {
                return CompletableFuture.runAsync(() -> reached.add(0));
            }
        }, new InternalInterceptor() {
            @Override
            public CompletableFuture<Void> afterCompletion(RequestContext context,
                                                           Object handler,
                                                           Exception ex) {
                return CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    reached.add(1);
                });
            }
        });
        final RouteHandlerMethodAdapter mock = mock(RouteHandlerMethodAdapter.class);
        final MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.context()).thenReturn(mockHandlerData.context());
        when(mock.handlerResolver()).thenReturn(mock(HandlerValueResolver.class));
        final AbstractRouteExecution execution =
                new AbstractRouteExecution(mock, interceptors) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return null;
                    }
                };
        execution.applyPreHandle(mock(RequestContext.class), mock(Object.class)).toCompletableFuture().join();
        execution.triggerAfterCompletion(mock(RequestContext.class), new IllegalStateException())
                .toCompletableFuture()
                .join();
        assertEquals(2, reached.size());
        assertEquals(1, reached.get(0).intValue());
        assertEquals(0, reached.get(1).intValue());
    }

    @Test
    void testHandle() throws Throwable {
        final RouteHandlerMethodAdapter mock = mock(RouteHandlerMethodAdapter.class);
        final MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.handlerMethod()).thenReturn(mockHandlerData.handlerMethod());
        when(mockHandlerData.resolverFactory().getFutureTransfer(any())).thenReturn(
                (context, value) -> CompletableFuture.completedFuture(value)
        );
        when(mock.filters()).thenReturn(new RouteFilter[0]);
        when(mock.context()).thenReturn(mockHandlerData.context());
        when(mock.paramResolvers()).thenReturn(new ResolvableParam[0]);
        when(mock.handlerResolver())
                .thenReturn((returnValue, ctx) -> {
                    ctx.response().entity(returnValue.toString() + "oo");
                    return CompletableFuture.completedFuture(null);
                });
        final AbstractRouteExecution execution =
                new AbstractRouteExecution(mock, null) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return mock(Object.class);
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return (context, args) -> "foo";
                    }
                };

        HttpResponse response = MockHttpResponse.aMockResponse().build();
        execution.handle(new RequestContextImpl(mock(HttpRequest.class), response)).toCompletableFuture().join();
        assertEquals("foooo", response.entity());
    }

    @Test
    void testHandleWithError() throws Throwable {
        final RouteHandlerMethodAdapter mock = mock(RouteHandlerMethodAdapter.class);
        final MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.handlerMethod()).thenReturn(mockHandlerData.handlerMethod());
        when(mockHandlerData.resolverFactory().getFutureTransfer(any())).thenReturn(
                (context, value) -> CompletableFuture.completedFuture(value)
        );
        when(mock.filters()).thenReturn(new RouteFilter[0]);
        when(mock.context()).thenReturn(mockHandlerData.context());
        when(mock.paramResolvers()).thenReturn(new ResolvableParam[0]);
        when(mock.handlerResolver())
                .thenReturn((returnValue, ctx) -> {
                    ctx.response().entity(returnValue.toString() + "oo");
                    return CompletableFuture.completedFuture(null);
                });
        final Throwable e = new IllegalStateException();
        final AbstractRouteExecution execution =
                new AbstractRouteExecution(mock, null) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return mock(Object.class);
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return (context, args) -> {
                            throw e;
                        };
                    }
                };

        final AtomicReference<Throwable> ret = new AtomicReference<>();
        execution.handle(mock(RequestContext.class)).exceptionally(t -> {
            ret.set(t);
            return null;
        });
        assertEquals(e, Futures.unwrapCompletionException(ret.get()));
    }

    @Test
    void testHandleWithExecutionNotAllowed() throws Exception {
        final RouteHandlerMethodAdapter mock = mock(RouteHandlerMethodAdapter.class);
        final MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.handlerMethod()).thenReturn(mockHandlerData.handlerMethod());
        when(mockHandlerData.resolverFactory().getFutureTransfer(any())).thenReturn(
                (context, value) -> CompletableFuture.completedFuture(value)
        );
        when(mock.filters()).thenReturn(new RouteFilter[0]);
        when(mock.context()).thenReturn(mockHandlerData.context());
        when(mock.paramResolvers()).thenReturn(new ResolvableParam[0]);
        when(mock.handlerResolver())
                .thenReturn((returnValue, ctx) -> {
                    ctx.response().entity(returnValue.toString() + "oo");
                    return CompletableFuture.completedFuture(null);
                });
        final Throwable e = new IllegalStateException();
        final AbstractRouteExecution execution =
                new AbstractRouteExecution(mock, Collections.singletonList(new InternalInterceptor() {

                    @Override
                    public CompletionStage<Boolean> preHandle(RequestContext ctx, Object handler) {
                        return Futures.completedFuture(Boolean.FALSE);
                    }
                })) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return mock(Object.class);
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return (context, args) -> {
                            Thread.sleep(1000L);
                            throw e;
                        };
                    }
                };

        CompletableFuture<Void> cf = execution.handle(mock(RequestContext.class)).toCompletableFuture();
        assertTrue(cf.isDone());
        assertFalse(cf.isCompletedExceptionally());
    }

    @Test
    void testNormalFilters() throws Exception {
        final RouteHandlerMethodAdapter mock = mock(RouteHandlerMethodAdapter.class);
        final MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.handlerMethod()).thenReturn(mockHandlerData.handlerMethod());
        when(mockHandlerData.resolverFactory().getFutureTransfer(any())).thenReturn(
                (context, value) -> CompletableFuture.completedFuture(value)
        );
        final RouteFilter[] filters = new RouteFilter[2];
        filters[0] = (mapping, context, next) -> {
            context.attrs().attr(AttributeKey.stringKey("filter0")).set("filter0");
            return next.doNext(mapping, context);
        };
        filters[1] = (mapping, context, next) -> {
            context.attrs().attr(AttributeKey.stringKey("filter1")).set("filter1");
            return next.doNext(mapping, context);
        };
        when(mock.filters()).thenReturn(filters);
        when(mock.context()).thenReturn(mockHandlerData.context());
        when(mock.paramResolvers()).thenReturn(new ResolvableParam[0]);
        when(mock.handlerResolver())
                .thenReturn((returnValue, ctx) -> {
                    ctx.response().entity(returnValue.toString() + "oo");
                    return CompletableFuture.completedFuture(null);
                });
        final AbstractRouteExecution execution =
                new AbstractRouteExecution(mock, null) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return mock(Object.class);
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return (context, args) -> "foo";
                    }
                };

        HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext ctx = new RequestContextImpl(mock(HttpRequest.class), response);
        execution.handle(ctx).toCompletableFuture().join();
        assertEquals("foooo", response.entity());
        assertEquals("filter0", ctx.attrs().attr(AttributeKey.stringKey("filter0")).get());
        assertEquals("filter1", ctx.attrs().attr(AttributeKey.stringKey("filter1")).get());
    }

    @Test
    void testErrorInFilters() throws Exception {
        final RouteHandlerMethodAdapter mock = mock(RouteHandlerMethodAdapter.class);
        final MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.handlerMethod()).thenReturn(mockHandlerData.handlerMethod());
        when(mockHandlerData.resolverFactory().getFutureTransfer(any())).thenReturn(
                (context, value) -> CompletableFuture.completedFuture(value)
        );
        RouteFilter[] filters = new RouteFilter[1];
        filters[0] = (mapping, context, next) -> {
            throw new IllegalStateException("The method need to be implemented!");
        };
        when(mock.filters()).thenReturn(filters);
        when(mock.context()).thenReturn(mockHandlerData.context());
        when(mock.paramResolvers()).thenReturn(new ResolvableParam[0]);
        when(mock.handlerResolver())
                .thenReturn((returnValue, ctx) -> {
                    ctx.response().entity(returnValue.toString() + "oo");
                    return CompletableFuture.completedFuture(null);
                });
        final AbstractRouteExecution execution =
                new AbstractRouteExecution(mock, null) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return mock(Object.class);
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return (context, args) -> "foo";
                    }
                };

        HttpResponse response = MockHttpResponse.aMockResponse().build();
        Throwable ex = null;
        try {
            execution.handle(new RequestContextImpl(mock(HttpRequest.class), response)).toCompletableFuture().join();
        } catch (Throwable e) {
            ex = e;
        }
        assertNotNull(ex);
        assertNull(response.entity());
    }

    @Test
    void testAsyncErrorInFilters() throws Exception {
        final RouteHandlerMethodAdapter mock = mock(RouteHandlerMethodAdapter.class);
        final MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.handlerMethod()).thenReturn(mockHandlerData.handlerMethod());
        when(mockHandlerData.resolverFactory().getFutureTransfer(any())).thenReturn(
                (context, value) -> CompletableFuture.completedFuture(value)
        );
        RouteFilter[] filters = new RouteFilter[1];
        filters[0] = (mapping, context, next) -> Futures.completedExceptionally(new IllegalStateException());
        when(mock.filters()).thenReturn(filters);
        when(mock.context()).thenReturn(mockHandlerData.context());
        when(mock.paramResolvers()).thenReturn(new ResolvableParam[0]);
        when(mock.handlerResolver())
                .thenReturn((returnValue, ctx) -> {
                    ctx.response().entity(returnValue.toString() + "oo");
                    return CompletableFuture.completedFuture(null);
                });
        final AbstractRouteExecution execution =
                new AbstractRouteExecution(mock, null) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return mock(Object.class);
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return (context, args) -> "foo";
                    }
                };

        HttpResponse response = MockHttpResponse.aMockResponse().build();
        CompletionStage<?> ret =
                execution.handle(new RequestContextImpl(mock(HttpRequest.class), response));

        assertTrue(ret.toCompletableFuture().isCompletedExceptionally());
        assertNull(response.entity());
    }

    @Test
    void testEndInFilters() throws Exception {
        final RouteHandlerMethodAdapter mock = mock(RouteHandlerMethodAdapter.class);
        final MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.handlerMethod()).thenReturn(mockHandlerData.handlerMethod());
        when(mockHandlerData.resolverFactory().getFutureTransfer(any())).thenReturn(
                (context, value) -> CompletableFuture.completedFuture(value)
        );
        final RouteFilter[] filters = new RouteFilter[2];
        filters[0] = (mapping, context, next) -> {
            context.attrs().attr(AttributeKey.stringKey("filter0")).set("filter0");
            context.response().entity("aaa");
            return CompletableFuture.completedFuture(null);
        };
        filters[1] = (mapping, context, next) -> {
            context.attrs().attr(AttributeKey.stringKey("filter1")).set("filter1");
            return next.doNext(mapping, context);
        };
        when(mock.filters()).thenReturn(filters);
        when(mock.context()).thenReturn(mockHandlerData.context());
        when(mock.paramResolvers()).thenReturn(new ResolvableParam[0]);
        when(mock.handlerResolver())
                .thenReturn((returnValue, ctx) -> {
                    ctx.response().entity(returnValue.toString() + "oo");
                    return CompletableFuture.completedFuture(null);
                });
        final AbstractRouteExecution execution =
                new AbstractRouteExecution(mock, null) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return mock(Object.class);
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return (context, args) -> "foo";
                    }
                };

        HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext ctx = new RequestContextImpl(mock(HttpRequest.class), response);
        execution.handle(ctx).toCompletableFuture().join();
        assertEquals("aaa", response.entity());
        assertEquals("filter0", ctx.attrs().attr(AttributeKey.stringKey("filter0")).get());
        assertNull(ctx.attrs().attr(AttributeKey.stringKey("filter1")).get());
    }
}
