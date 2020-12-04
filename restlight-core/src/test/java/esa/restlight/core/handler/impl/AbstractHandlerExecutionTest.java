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
import esa.restlight.core.method.MethodParam;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.server.bootstrap.WebServerException;
import esa.restlight.server.util.Futures;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AbstractHandlerExecutionTest {

    @Test
    void testResolveEmptyArguments() {
        final HandlerAdapter mock = mock(HandlerAdapter.class);
        final AbstractHandlerExecution<HandlerAdapter> execution = new AbstractHandlerExecution<HandlerAdapter>(mock) {
            @Override
            protected CompletableFuture<Object> transferToFuture(Object returnValue) {
                return Futures.completedFuture(returnValue);
            }
        };
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        when(mock.params()).thenReturn(new HandlerAdapter.ResolvableParam[0]);
        final Object[] args = execution.resolveArguments(request, response);
        assertNotNull(args);
        assertEquals(0, args.length);
    }

    @Test
    void testResolveFixArguments() {
        final HandlerAdapter mock = mock(HandlerAdapter.class);
        final AbstractHandlerExecution<HandlerAdapter> execution = new AbstractHandlerExecution<HandlerAdapter>(mock) {
            @Override
            protected CompletableFuture<Object> transferToFuture(Object returnValue) {
                return Futures.completedFuture(returnValue);
            }

            @Override
            protected Object resolveFixedArg(MethodParam parameter, AsyncRequest request, AsyncResponse response) {
                return "foo";
            }
        };
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final MethodParam param = mock(MethodParam.class);
        //noinspection unchecked
        when(param.type()).thenReturn((Class) Object.class);
        final HandlerAdapter.ResolvableParam[] params = new HandlerAdapter.ResolvableParam[1];
        params[0] = new HandlerAdapter.ResolvableParam(param, null);
        when(mock.params()).thenReturn(params);

        final Object[] args = execution.resolveArguments(request, response);
        assertNotNull(args);
        assertEquals(1, args.length);
        assertEquals("foo", args[0]);
    }

    @Test
    void testResolveArguments() throws Exception {
        final HandlerAdapter mock = mock(HandlerAdapter.class);
        final AbstractHandlerExecution<HandlerAdapter> execution = new AbstractHandlerExecution<HandlerAdapter>(mock) {
            @Override
            protected CompletableFuture<Object> transferToFuture(Object returnValue) {
                return Futures.completedFuture(returnValue);
            }
        };
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final MethodParam param = mock(MethodParam.class);
        final ArgumentResolver argResolver = mock(ArgumentResolver.class);
        when(argResolver.resolve(any(), any())).thenReturn("foo");
        final HandlerAdapter.ResolvableParam[] params = new HandlerAdapter.ResolvableParam[1];
        params[0] = new HandlerAdapter.ResolvableParam(param, argResolver);
        when(mock.params())
                .thenReturn(params);
        final Object[] args = execution.resolveArguments(request, response);
        assertNotNull(args);
        assertEquals(1, args.length);
        assertEquals("foo", args[0]);
    }


    @Test
    void testResolveArgumentsWithNullArgumentResolver() {
        final HandlerAdapter mock = mock(HandlerAdapter.class);
        final AbstractHandlerExecution<HandlerAdapter> execution = new AbstractHandlerExecution<HandlerAdapter>(mock) {
            @Override
            protected CompletableFuture<Object> transferToFuture(Object returnValue) {
                return Futures.completedFuture(returnValue);
            }
        };
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final MethodParam param = mock(MethodParam.class);
        //noinspection unchecked
        when(param.type()).thenReturn((Class) Object.class);
        final HandlerAdapter.ResolvableParam[] params = new HandlerAdapter.ResolvableParam[1];
        params[0] = new HandlerAdapter.ResolvableParam(param, null);
        when(mock.params())
                .thenReturn(params);
        assertThrows(WebServerException.class, () -> execution.resolveArguments(request, response));
    }

    @Test
    void testResolveArgumentsWithErrorArgumentResolver() throws Exception {
        final HandlerAdapter mock = mock(HandlerAdapter.class);
        final AbstractHandlerExecution<HandlerAdapter> execution = new AbstractHandlerExecution<HandlerAdapter>(mock) {
            @Override
            protected CompletableFuture<Object> transferToFuture(Object returnValue) {
                return Futures.completedFuture(returnValue);
            }
        };
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final MethodParam param = mock(MethodParam.class);
        final ArgumentResolver argResolver = mock(ArgumentResolver.class);
        when(argResolver.resolve(any(), any())).thenThrow(new IllegalStateException());
        //noinspection unchecked
        when(param.type()).thenReturn((Class) Object.class);
        final HandlerAdapter.ResolvableParam[] params = new HandlerAdapter.ResolvableParam[1];
        params[0] = new HandlerAdapter.ResolvableParam(param, argResolver);
        when(mock.params())
                .thenReturn(params);
        assertThrows(WebServerException.class, () -> execution.resolveArguments(request, response));
    }

    @Test
    void testNormalInvoke() throws Throwable {
        final HandlerAdapter mock = mock(HandlerAdapter.class);
        final AbstractHandlerExecution<HandlerAdapter> execution = new AbstractHandlerExecution<HandlerAdapter>(mock) {
            @Override
            protected CompletableFuture<Object> transferToFuture(Object returnValue) {
                return Futures.completedFuture(returnValue);
            }
        };
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        when(mock.invoke(any(), any(), any())).thenReturn("foo");
        when(mock.isConcurrent()).thenReturn(false);
        final Object ret = execution.invoke(request, response, null).join();
        assertEquals("foo", ret);
    }

    @Test
    void testAsyncInvoke() throws Throwable {
        final HandlerAdapter mock = mock(HandlerAdapter.class);
        final AbstractHandlerExecution<HandlerAdapter> execution = new AbstractHandlerExecution<HandlerAdapter>(mock) {
            @SuppressWarnings("unchecked")
            @Override
            protected CompletableFuture<Object> transferToFuture(Object returnValue) {
                return (CompletableFuture<Object>) returnValue;
            }
        };
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        when(mock.invoke(any(), any(), any())).thenReturn(CompletableFuture.completedFuture("foo"));
        when(mock.isConcurrent()).thenReturn(true);
        final Object ret = execution.invoke(request, response, null).join();
        assertEquals("foo", ret);
    }

    @Test
    void testAsyncInvokeButReturnNull() throws Throwable {
        final HandlerAdapter mock = mock(HandlerAdapter.class);
        final AbstractHandlerExecution<HandlerAdapter> execution = new AbstractHandlerExecution<HandlerAdapter>(mock) {
            @SuppressWarnings("unchecked")
            @Override
            protected CompletableFuture<Object> transferToFuture(Object returnValue) {
                return (CompletableFuture<Object>) returnValue;
            }
        };
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        when(mock.invoke(any(), any(), any())).thenReturn(null);
        when(mock.isConcurrent()).thenReturn(true);
        final Object ret = execution.invoke(request, response, null).join();
        assertNull(ret);
        assertEquals(500, response.status());
    }

    @Test
    void testInvokeError() throws Throwable {
        final HandlerAdapter mock = mock(HandlerAdapter.class);
        final AbstractHandlerExecution<HandlerAdapter> execution = new AbstractHandlerExecution<HandlerAdapter>(mock) {
            @SuppressWarnings("unchecked")
            @Override
            protected CompletableFuture<Object> transferToFuture(Object returnValue) {
                return (CompletableFuture<Object>) returnValue;
            }
        };
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        when(mock.invoke(any(), any(), any())).thenThrow(new IllegalStateException());
        final CompletableFuture<Object> ret = execution.invoke(request, response, null);
        assertNotNull(ret);
        assertTrue(ret.isCompletedExceptionally());
    }

    @Test
    void testHandleReturnValue() {
        final HandlerAdapter mock = mock(HandlerAdapter.class);
        final AbstractHandlerExecution<HandlerAdapter> execution = new AbstractHandlerExecution<HandlerAdapter>(mock) {
            @Override
            protected CompletableFuture<Object> transferToFuture(Object returnValue) {
                return Futures.completedFuture(returnValue);
            }
        };
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        when(mock.returnValueResolver())
                .thenReturn((returnValue, request1, response1) -> returnValue.toString().getBytes());
        when(mock.hasCustomResponse()).thenReturn(true);
        when(mock.customResponse()).thenReturn(HttpResponseStatus.BAD_REQUEST);
        execution.handleReturnValue("foo", request, response);
        assertTrue(response.isCommitted());
        assertEquals(HttpResponseStatus.BAD_REQUEST.code(), response.status());
        assertArrayEquals("foo".getBytes(), ByteBufUtil.getBytes(response.getSentData()));
    }

    @Test
    void testHandleReturnValueButResponseCommitted() {
        final HandlerAdapter mock = mock(HandlerAdapter.class);
        final AbstractHandlerExecution<HandlerAdapter> execution = new AbstractHandlerExecution<HandlerAdapter>(mock) {
            @Override
            protected CompletableFuture<Object> transferToFuture(Object returnValue) {
                return Futures.completedFuture(returnValue);
            }
        };
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        response.sendResult();
        execution.handleReturnValue("foo", request, response);
        assertTrue(response.isCommitted());
        verify(mock, never()).returnValueResolver();
    }

    @Test
    void testHandle() {
        final HandlerAdapter mock = mock(HandlerAdapter.class);
        final AbstractHandlerExecution<HandlerAdapter> execution = new AbstractHandlerExecution<HandlerAdapter>(mock) {
            @Override
            protected CompletableFuture<Object> transferToFuture(Object returnValue) {
                return Futures.completedFuture(returnValue);
            }
        };
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        when(mock.params()).thenReturn(new HandlerAdapter.ResolvableParam[0]);
        when(mock.returnValueResolver())
                .thenReturn((returnValue, request1, response1) -> String.valueOf(returnValue).getBytes());
        execution.handle(request, response).join();
        assertTrue(response.isCommitted());
    }

    @Test
    void testHandleWithError() {
        final HandlerAdapter mock = mock(HandlerAdapter.class);
        final AbstractHandlerExecution<HandlerAdapter> execution =
                new AbstractHandlerExecution<HandlerAdapter>(mock) {
                    @Override
                    protected CompletableFuture<Object> transferToFuture(Object returnValue) {
                        return Futures.completedFuture(returnValue);
                    }
                };
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        when(mock.params()).thenThrow(new IllegalStateException());
        assertTrue(execution.handle(request, response).isCompletedExceptionally());
        assertFalse(response.isCommitted());
    }
}
