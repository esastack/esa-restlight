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
package esa.restlight.core.handler.impl;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.interceptor.InternalInterceptor;
import esa.restlight.server.util.Futures;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.buffer.ByteBufUtil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultRouteExecutionTest {

    @Test
    void testProps() {
        final RouteHandlerAdapter mock = mock(RouteHandlerAdapter.class);
        final DefaultRouteExecution execution = new DefaultRouteExecution(mock, null);
        execution.exceptionHandler();
        verify(mock, times(1)).exceptionResolver();
        assertNotNull(execution.completionHandler());
        assertEquals("foo", execution.transferToFuture("foo").join());
    }


    @Test
    void testApplyWithNoneInterceptor() {
        final RouteHandlerAdapter mock = mock(RouteHandlerAdapter.class);
        final DefaultRouteExecution execution = new DefaultRouteExecution(mock, null);
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        assertTrue(execution.applyPreHandle(request, response).join());
        CompletableFuture<Void> cf = execution.applyPostHandle(request, response);
        assertTrue(cf.isDone());
        assertFalse(cf.isCompletedExceptionally());
        cf = execution.triggerAfterCompletion(request, response, null);
        assertTrue(cf.isDone());
        assertFalse(cf.isCompletedExceptionally());
    }


    @Test
    void testApplyPreHandle() {
        final RouteHandlerAdapter mock = mock(RouteHandlerAdapter.class);

        final List<Integer> reached = new CopyOnWriteArrayList<>();
        final List<InternalInterceptor> interceptors = Arrays.asList(new InternalInterceptor() {
            @Override
            public boolean preHandle(AsyncRequest request, AsyncResponse response, Object handler) {
                reached.add(0);
                return true;
            }
        }, new InternalInterceptor() {
            @Override
            public boolean preHandle(AsyncRequest request, AsyncResponse response, Object handler) {
                reached.add(1);
                return false;
            }
        }, new InternalInterceptor() {
            @Override
            public boolean preHandle(AsyncRequest request, AsyncResponse response, Object handler) {
                reached.add(2);
                return true;
            }
        });
        final DefaultRouteExecution execution = new DefaultRouteExecution(mock, interceptors);
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        assertFalse(execution.applyPreHandle(request, response).join());
        assertEquals(2, reached.size());
        assertEquals(0, reached.get(0).intValue());
        assertEquals(1, reached.get(1).intValue());
    }

    @Test
    void testApplyPreHandleAsync() {
        final RouteHandlerAdapter mock = mock(RouteHandlerAdapter.class);

        final List<Integer> reached = new CopyOnWriteArrayList<>();
        final List<InternalInterceptor> interceptors = Arrays.asList(new InternalInterceptor() {
            @Override
            public CompletableFuture<Boolean> preHandle0(AsyncRequest request, AsyncResponse response, Object handler) {

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
            public CompletableFuture<Boolean> preHandle0(AsyncRequest request, AsyncResponse response, Object handler) {

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
            public CompletableFuture<Boolean> preHandle0(AsyncRequest request, AsyncResponse response, Object handler) {

                return CompletableFuture.supplyAsync(() -> {
                    reached.add(2);
                    return true;
                });
            }
        });
        final DefaultRouteExecution execution = new DefaultRouteExecution(mock, interceptors);
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        assertFalse(execution.applyPreHandle(request, response).join());
        assertEquals(2, reached.size());
        assertEquals(0, reached.get(0).intValue());
        assertEquals(1, reached.get(1).intValue());
    }

    @Test
    void testApplyPostHandle() {
        final RouteHandlerAdapter mock = mock(RouteHandlerAdapter.class);

        final List<Integer> reached = new CopyOnWriteArrayList<>();
        final List<InternalInterceptor> interceptors = Arrays.asList(new InternalInterceptor() {
            @Override
            public void postHandle(AsyncRequest request, AsyncResponse response, Object handler) {
                reached.add(0);
            }
        }, new InternalInterceptor() {
            @Override
            public void postHandle(AsyncRequest request, AsyncResponse response, Object handler) {
                reached.add(1);
            }
        });
        final DefaultRouteExecution execution = new DefaultRouteExecution(mock, interceptors);
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        execution.applyPostHandle(request, response).join();
        assertEquals(2, reached.size());
        assertEquals(0, reached.get(0).intValue());
        assertEquals(1, reached.get(1).intValue());
    }

    @Test
    void testApplyPostHandleAsync() {
        final RouteHandlerAdapter mock = mock(RouteHandlerAdapter.class);

        final List<Integer> reached = new CopyOnWriteArrayList<>();
        final List<InternalInterceptor> interceptors = Arrays.asList(new InternalInterceptor() {
            @Override
            public CompletableFuture<Void> postHandle0(AsyncRequest request, AsyncResponse response, Object handler) {
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
            public CompletableFuture<Void> postHandle0(AsyncRequest request, AsyncResponse response, Object handler) {
                return CompletableFuture.runAsync(() -> reached.add(1));
            }
        });
        final DefaultRouteExecution execution = new DefaultRouteExecution(mock, interceptors);
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        execution.applyPostHandle(request, response).join();
        assertEquals(2, reached.size());
        assertEquals(0, reached.get(0).intValue());
        assertEquals(1, reached.get(1).intValue());
    }

    @Test
    void testTriggerAfterCompletionWithNoneException() {
        final RouteHandlerAdapter mock = mock(RouteHandlerAdapter.class);

        final AtomicBoolean reached = new AtomicBoolean(false);
        final List<InternalInterceptor> interceptors = Collections.singletonList(new InternalInterceptor() {
            @Override
            public void afterCompletion(AsyncRequest request, AsyncResponse response, Object handler, Exception t) {
                reached.set(true);
            }
        });
        final DefaultRouteExecution execution = new DefaultRouteExecution(mock, interceptors);
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final CompletableFuture<Void> cf = execution.triggerAfterCompletion(request, response, new Error());
        assertTrue(cf.isDone());
        assertTrue(cf.isCompletedExceptionally());
        assertFalse(reached.get());
    }

    @Test
    void testTriggerAfterCompletion() {
        final RouteHandlerAdapter mock = mock(RouteHandlerAdapter.class);

        final List<Integer> reached = new CopyOnWriteArrayList<>();
        final List<InternalInterceptor> interceptors = Arrays.asList(new InternalInterceptor() {
            @Override
            public void afterCompletion(AsyncRequest request, AsyncResponse response, Object handler, Exception t) {
                reached.add(0);
            }
        }, new InternalInterceptor() {
            @Override
            public void afterCompletion(AsyncRequest request, AsyncResponse response, Object handler, Exception t) {
                reached.add(1);
            }
        });
        final DefaultRouteExecution execution = new DefaultRouteExecution(mock, interceptors);
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        execution.applyPreHandle(request, response).join();
        execution.triggerAfterCompletion(request, response, new IllegalStateException()).join();

        assertEquals(2, reached.size());
        assertEquals(1, reached.get(0).intValue());
        assertEquals(0, reached.get(1).intValue());
    }

    @Test
    void testTriggerAfterCompletionWithInterruptedPreHandle() {
        final RouteHandlerAdapter mock = mock(RouteHandlerAdapter.class);

        final List<Integer> reached = new CopyOnWriteArrayList<>();
        final List<InternalInterceptor> interceptors = Arrays.asList(new InternalInterceptor() {

            @Override
            public void afterCompletion(AsyncRequest request, AsyncResponse response, Object handler, Exception t) {
                reached.add(0);
            }
        }, new InternalInterceptor() {

            @Override
            public boolean preHandle(AsyncRequest request, AsyncResponse response, Object handler) {
                return false;
            }

            @Override
            public void afterCompletion(AsyncRequest request, AsyncResponse response, Object handler, Exception t) {
                reached.add(1);
            }
        });
        final DefaultRouteExecution execution = new DefaultRouteExecution(mock, interceptors);
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        execution.applyPreHandle(request, response).join();
        execution.triggerAfterCompletion(request, response, new IllegalStateException()).join();

        assertEquals(1, reached.size());
        assertEquals(0, reached.get(0).intValue());
    }

    @Test
    void testTriggerAfterCompletionAsync() {
        final RouteHandlerAdapter mock = mock(RouteHandlerAdapter.class);

        final List<Integer> reached = new CopyOnWriteArrayList<>();
        final List<InternalInterceptor> interceptors = Arrays.asList(new InternalInterceptor() {
            @Override
            public CompletableFuture<Void> afterCompletion0(AsyncRequest request,
                                                            AsyncResponse response,
                                                            Object handler,
                                                            Exception t) {
                return CompletableFuture.runAsync(() -> reached.add(0));
            }
        }, new InternalInterceptor() {
            @Override
            public CompletableFuture<Void> afterCompletion0(AsyncRequest request,
                                                            AsyncResponse response,
                                                            Object handler, Exception t) {
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
        final DefaultRouteExecution execution = new DefaultRouteExecution(mock, interceptors);
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        execution.applyPreHandle(request, response).join();
        execution.triggerAfterCompletion(request, response, new IllegalStateException()).join();
        assertEquals(2, reached.size());
        assertEquals(1, reached.get(0).intValue());
        assertEquals(0, reached.get(1).intValue());
    }

    @Test
    void testHandle() throws Throwable {
        final RouteHandlerAdapter mock = mock(RouteHandlerAdapter.class);
        final DefaultRouteExecution execution = new DefaultRouteExecution(mock, null);
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        when(mock.params()).thenReturn(new HandlerAdapter.ResolvableParam[0]);
        when(mock.invoke(any(), any(), any())).thenReturn("foo");
        when(mock.returnValueResolver())
                .thenReturn((returnValue, request1, response1) -> String.valueOf(returnValue).getBytes());
        execution.handle(request, response).join();
        assertArrayEquals("foo".getBytes(), ByteBufUtil.getBytes(response.getSentData()));
    }

    @Test
    void testHandleWithError() throws Throwable {
        final RouteHandlerAdapter mock = mock(RouteHandlerAdapter.class);
        final DefaultRouteExecution execution = new DefaultRouteExecution(mock, null);
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        when(mock.params()).thenReturn(new HandlerAdapter.ResolvableParam[0]);

        final Throwable e = new IllegalStateException();
        when(mock.invoke(any(), any(), any())).thenThrow(e);
        final AtomicReference<Throwable> ret = new AtomicReference<>();
        execution.handle(request, response).exceptionally(t -> {
            ret.set(t);
            return null;
        });
        assertEquals(e, Futures.unwrapCompletionException(ret.get()));
    }

    @Test
    void testHandleWithExecutionNotAllowed() {
        final RouteHandlerAdapter mock = mock(RouteHandlerAdapter.class);
        final DefaultRouteExecution execution =
                new DefaultRouteExecution(mock, Collections.singletonList(new InternalInterceptor() {

                    @Override
                    public boolean preHandle(AsyncRequest request, AsyncResponse response, Object handler) {
                        return false;
                    }
                }));
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        CompletableFuture<Void> cf = execution.handle(request, response);
        assertTrue(cf.isDone());
        assertFalse(cf.isCompletedExceptionally());
    }

}
