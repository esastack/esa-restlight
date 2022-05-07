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

import io.esastack.restlight.core.handler.HandlerInvoker;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.MethodParam;
import io.esastack.restlight.core.handler.method.ResolvableParam;
import io.esastack.restlight.core.exception.WebServerException;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.mock.MockHttpResponse;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractExecutionTest {

    @Test
    void testResolveEmptyArguments() throws Exception {
        final HandlerMethodAdapter mock = mock(HandlerMethodAdapter.class);
        MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.context()).thenReturn(mockHandlerData.context());
        final AbstractExecution<HandlerMethodAdapter> execution =
                new AbstractExecution<HandlerMethodAdapter>(mock(HandlerValueResolver.class), mock) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return null;
                    }
                };

        when(mock.paramResolvers()).thenReturn(new ResolvableParam[0]);
        final Object[] args = execution.resolveArgs(mock(RequestContext.class));
        assertNotNull(args);
        assertEquals(0, args.length);
    }

    @Test
    void testResolveFixArguments() throws Exception {
        final HandlerMethodAdapter mock = mock(HandlerMethodAdapter.class);
        MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.context()).thenReturn(mockHandlerData.context());
        final AbstractExecution<HandlerMethodAdapter> execution =
                new AbstractExecution<HandlerMethodAdapter>(mock(HandlerValueResolver.class), mock) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return null;
                    }

                    @Override
                    protected Object resolveFixedArg(MethodParam parameter,
                                                     RequestContext context) {
                        return "foo";
                    }
                };
        final MethodParam param = mock(MethodParam.class);
        //noinspection unchecked
        when(param.type()).thenReturn((Class) Object.class);
        final ResolvableParam[] params = new ResolvableParam[1];
        params[0] = new ResolvableParam(param, null);
        when(mock.paramResolvers()).thenReturn(params);

        final Object[] args = execution.resolveArgs(mock(RequestContext.class));
        assertNotNull(args);
        assertEquals(1, args.length);
        assertEquals("foo", args[0]);
    }

    @Test
    void testResolveArguments() throws Exception {
        final HandlerMethodAdapter mock = mock(HandlerMethodAdapter.class);
        MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.context()).thenReturn(mockHandlerData.context());
        final AbstractExecution<HandlerMethodAdapter> execution =
                new AbstractExecution<HandlerMethodAdapter>(mock(HandlerValueResolver.class), mock) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return null;
                    }
                };

        final MethodParam param = mock(MethodParam.class);
        final ResolverWrap argResolver = mock(ResolverWrap.class);
        when(argResolver.resolve(any(), any())).thenReturn("foo");
        final ResolvableParam[] params = new ResolvableParam[1];
        params[0] = new ResolvableParam(param, argResolver);
        when(mock.paramResolvers())
                .thenReturn(params);
        final Object[] args = execution.resolveArgs(mock(RequestContext.class));
        assertNotNull(args);
        assertEquals(1, args.length);
        assertEquals("foo", args[0]);
    }

    @Test
    void testResolveArgumentsWithNullArgumentResolver() throws Exception {
        final HandlerMethodAdapter mock = mock(HandlerMethodAdapter.class);
        MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.context()).thenReturn(mockHandlerData.context());
        final AbstractExecution<HandlerMethodAdapter> execution =
                new AbstractExecution<HandlerMethodAdapter>(mock(HandlerValueResolver.class), mock) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return null;
                    }
                };

        final MethodParam param = mock(MethodParam.class);
        //noinspection unchecked
        when(param.type()).thenReturn((Class) Object.class);
        final ResolvableParam[] params = new ResolvableParam[1];
        params[0] = new ResolvableParam(param, null);
        when(mock.paramResolvers())
                .thenReturn(params);
        assertThrows(WebServerException.class,
                () -> execution.resolveArgs(mock(RequestContext.class)));
    }

    @Test
    void testResolveArgumentsWithErrorArgumentResolver() throws Exception {
        final HandlerMethodAdapter mock = mock(HandlerMethodAdapter.class);
        MockHandlerData mockHandlerData = new MockHandlerData();
        when(mock.context()).thenReturn(mockHandlerData.context());
        final AbstractExecution<HandlerMethodAdapter> execution =
                new AbstractExecution<HandlerMethodAdapter>(mock(HandlerValueResolver.class), mock) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return null;
                    }
                };

        final MethodParam param = mock(MethodParam.class);
        final ResolverWrap argResolver = mock(ResolverWrap.class);
        when(argResolver.resolve(any(), any())).thenThrow(new IllegalStateException());
        //noinspection unchecked
        when(param.type()).thenReturn((Class) Object.class);
        final ResolvableParam[] params = new ResolvableParam[1];
        params[0] = new ResolvableParam(param, argResolver);
        when(mock.paramResolvers())
                .thenReturn(params);
        assertThrows(WebServerException.class,
                () -> execution.resolveArgs(mock(RequestContext.class)));
    }

    @Test
    void testNormalInvoke() throws Throwable {
        final HandlerMethodAdapter mock = mock(HandlerMethodAdapter.class);
        MockHandlerData mockHandlerData = new MockHandlerData();
        when(mockHandlerData.resolverFactory().getFutureTransfer(any()))
                .thenReturn((context, value) -> CompletableFuture.completedFuture(value));
        when(mock.context()).thenReturn(mockHandlerData.context());
        final AbstractExecution<HandlerMethodAdapter> execution =
                new AbstractExecution<HandlerMethodAdapter>(mock(HandlerValueResolver.class), mock) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return (context, args) -> "foo";
                    }
                };

        when(mock.isConcurrent()).thenReturn(false);
        final Object ret = execution.invoke(mock(RequestContext.class), null, null)
                .toCompletableFuture()
                .get();
        assertEquals("foo", ret);
    }

    @Test
    void testAsyncInvoke() throws Throwable {
        final HandlerMethodAdapter mock = mock(HandlerMethodAdapter.class);
        MockHandlerData mockHandlerData = new MockHandlerData();
        when(mockHandlerData.resolverFactory().getFutureTransfer(any()))
                .thenReturn((context, value) -> (CompletionStage<Object>) value);
        when(mock.context()).thenReturn(mockHandlerData.context());
        final AbstractExecution<HandlerMethodAdapter> execution =
                new AbstractExecution<HandlerMethodAdapter>(mock(HandlerValueResolver.class), mock) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return (context, args) -> CompletableFuture.completedFuture("foo");
                    }
                };

        when(mock.isConcurrent()).thenReturn(true);
        final Object ret = execution.invoke(mock(RequestContext.class), null, null)
                .toCompletableFuture()
                .get();
        assertEquals("foo", ret);
    }

    @Test
    void testAsyncInvokeButReturnNull() throws Throwable {
        final HandlerMethodAdapter mock = mock(HandlerMethodAdapter.class);
        MockHandlerData mockHandlerData = new MockHandlerData();
        when(mockHandlerData.resolverFactory().getFutureTransfer(any()))
                .thenReturn((context, value) -> (CompletionStage<Object>) value);
        when(mock.context()).thenReturn(mockHandlerData.context());
        final AbstractExecution<HandlerMethodAdapter> execution =
                new AbstractExecution<HandlerMethodAdapter>(mock(HandlerValueResolver.class), mock) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return (context, args) -> null;
                    }
                };
        when(mock.isConcurrent()).thenReturn(true);
        ExecutionException ex = null;
        try {
            execution.invoke(mock(RequestContext.class),
                    null, null)
                    .toCompletableFuture()
                    .get();
        } catch (ExecutionException e) {
            ex = e;
        }
        assertNotNull(ex);
        assertEquals(IllegalStateException.class, ex.getCause().getClass());
    }

    @Test
    void testInvokeError() throws Throwable {
        final HandlerMethodAdapter mock = mock(HandlerMethodAdapter.class);
        MockHandlerData mockHandlerData = new MockHandlerData();
        when(mockHandlerData.resolverFactory().getFutureTransfer(any()))
                .thenReturn((context, value) -> (CompletionStage<Object>) value);
        when(mock.context()).thenReturn(mockHandlerData.context());
        final AbstractExecution<HandlerMethodAdapter> execution =
                new AbstractExecution<HandlerMethodAdapter>(mock(HandlerValueResolver.class), mock) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return (context, args) -> {
                            throw new IllegalStateException();
                        };
                    }
                };

        final CompletableFuture<Object> ret = execution
                .invoke(mock(RequestContext.class), null, null)
                .toCompletableFuture();
        assertNotNull(ret);
        assertTrue(ret.isCompletedExceptionally());
    }

    @Test
    void testHandleReturnValue() throws Exception {
        final HandlerMethodAdapter mock = mock(HandlerMethodAdapter.class);
        MockHandlerData mockHandlerData = new MockHandlerData();
        when(mockHandlerData.resolverFactory().getFutureTransfer(any()))
                .thenReturn((context, value) -> (CompletionStage<Object>) value);
        when(mock.context()).thenReturn(mockHandlerData.context());
        final AbstractExecution<HandlerMethodAdapter> execution =
                new AbstractExecution<HandlerMethodAdapter>((returnValue, ctx) -> {
                    ctx.response().entity(returnValue);
                    return CompletableFuture.completedFuture(null);
                }, mock) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return (context, args) -> {
                            throw new IllegalStateException();
                        };
                    }
                };

        HttpResponse response = MockHttpResponse.aMockResponse().build();
        execution.resolveReturnValue("foo", new RequestContextImpl(mock(HttpRequest.class), response));
        assertEquals("foo", response.entity());
    }

    @Test
    void testHandle() throws Exception {
        final HandlerMethodAdapter mock = mock(HandlerMethodAdapter.class);
        MockHandlerData mockHandlerData = new MockHandlerData();
        when(mockHandlerData.resolverFactory().getFutureTransfer(any()))
                .thenReturn((context, value) -> CompletableFuture.completedFuture(value));
        when(mock.context()).thenReturn(mockHandlerData.context());
        final AbstractExecution<HandlerMethodAdapter> execution =
                new AbstractExecution<HandlerMethodAdapter>((returnValue, ctx) -> {
                    ctx.response().entity(returnValue + "oo");
                    return CompletableFuture.completedFuture(null);
                }, mock) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return (context, args) -> "foo";
                    }
                };

        HttpResponse response = MockHttpResponse.aMockResponse().build();
        execution.resolveReturnValue("foo", new RequestContextImpl(mock(HttpRequest.class), response));
        assertEquals("foooo", response.entity());
    }

    @Test
    void testHandleWithError() throws Exception {
        final HandlerMethodAdapter mock = mock(HandlerMethodAdapter.class);
        MockHandlerData mockHandlerData = new MockHandlerData();
        when(mockHandlerData.resolverFactory().getFutureTransfer(any()))
                .thenReturn((context, value) -> (CompletionStage<Object>) value);
        when(mock.context()).thenReturn(mockHandlerData.context());
        final AbstractExecution<HandlerMethodAdapter> execution =
                new AbstractExecution<HandlerMethodAdapter>(mock(HandlerValueResolver.class), mock) {
                    @Override
                    protected Object resolveBean(HandlerMethod handler, RequestContext context) {
                        return null;
                    }

                    @Override
                    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object instance) {
                        return (context, args) -> CompletableFuture.completedFuture("foo");
                    }
                };
        when(mock.paramResolvers()).thenThrow(new IllegalStateException());
        assertTrue(execution.handle(mock(RequestContext.class))
                .toCompletableFuture()
                .isCompletedExceptionally());
    }
}
