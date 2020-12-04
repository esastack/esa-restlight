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
import esa.restlight.core.handler.Handler;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.core.resolver.HandlerResolverFactory;
import esa.restlight.core.resolver.ReturnValueResolver;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HandlerAdapterTest {

    @Test
    void testNormal() throws Throwable {
        final Handler handler = mock(Handler.class);
        when(handler.handler())
                .thenReturn(HandlerMethod.of(HandlerAdapterTest.class.getDeclaredMethod("normal", String.class),
                        new HandlerAdapterTest()));
        when(handler.customResponse()).thenReturn(HttpResponseStatus.BAD_REQUEST);
        when(handler.toString()).thenReturn("str");
        when(handler.invoke(any(), any(), any())).thenReturn("foo");

        final HandlerResolverFactory factory = mock(HandlerResolverFactory.class);
        final ArgumentResolver argumentResolver = (request, response) -> request.uri();
        final ReturnValueResolver returnValueResolver =
                (returnValue, request, response) -> String.valueOf(returnValue).getBytes();
        when(factory.getArgumentResolver(any())).thenReturn(argumentResolver);
        when(factory.getReturnValueResolver(any())).thenReturn(returnValueResolver);
        final HandlerAdapter<Handler> adapter = new HandlerAdapter<>(handler, factory);

        assertSame(handler.handler(), adapter.handler());
        assertFalse(adapter.isConcurrent());
        assertSame(handler.customResponse(), adapter.customResponse());
        assertTrue(adapter.hasCustomResponse());

        // params
        final HandlerAdapter.ResolvableParam[] params = adapter.params();
        assertNotNull(params);
        assertEquals(1, params.length);

        // resolvers
        assertSame(argumentResolver, params[0].resolver);
        assertSame(returnValueResolver, adapter.returnValueResolver());
        assertEquals(handler.toString(), adapter.toString());

        // invoke
        final AsyncRequest request = MockAsyncRequest.aMockRequest().withUri("/foo").build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final Object ret = adapter.invoke(request,
                response,
                new Object[]{adapter.params()[0].resolver.resolve(request, response)});
        assertEquals("foo", ret);
    }

    @Test
    void testConcurrent() throws Throwable {
        final Handler handler = mock(Handler.class);
        when(handler.handler())
                .thenReturn(HandlerMethod.of(HandlerAdapterTest.class.getDeclaredMethod("concurrent", String.class),
                        new HandlerAdapterTest()));
        final HandlerAdapter<Handler> adapter = new HandlerAdapter<>(handler, mock(HandlerResolverFactory.class));
        assertTrue(adapter.isConcurrent());
    }

    private String normal(String foo) {
        return foo;
    }

    private CompletableFuture<String> concurrent(String foo) {
        return CompletableFuture.completedFuture(foo);
    }

}
