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
package io.esastack.restlight.jaxrs.adapter;

import esa.commons.collection.AttributeKey;
import esa.commons.collection.AttributeMap;
import esa.commons.collection.Attributes;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.ResponseContent;
import io.esastack.restlight.core.context.ResponseEntity;
import io.esastack.restlight.core.context.ResponseEntityChannel;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.core.mock.MockHttpResponse;
import io.esastack.restlight.core.resolver.ResolverExecutor;
import io.esastack.restlight.core.resolver.ret.entity.ResponseEntityResolverContext;
import io.netty.buffer.ByteBufAllocator;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.WriterInterceptor;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WriterInterceptorsAdapterTest {

    @Test
    void testBasic() {
        assertThrows(NullPointerException.class,
                () -> new WriterInterceptorsAdapter(null, ProvidersPredicate.BINDING_GLOBAL));
        assertThrows(NullPointerException.class,
                () -> new WriterInterceptorsAdapter(new WriterInterceptor[0], null));
        assertDoesNotThrow(() -> new WriterInterceptorsAdapter(new WriterInterceptor[0],
                ProvidersPredicate.BINDING_GLOBAL));
        assertTrue(new WriterInterceptorsAdapter(new WriterInterceptor[0],
                ProvidersPredicate.BINDING_GLOBAL).supports(null));
    }

    @Test
    void testAroundWrite() throws Throwable {
        final AtomicInteger count = new AtomicInteger();
        final WriterInterceptor[] interceptors = new WriterInterceptor[1];

        final MultivaluedMap<String, Object> map = new MultivaluedHashMap<>();
        interceptors[0] = context -> {
            map.putAll(context.getHeaders());
            context.getHeaders().add("name", "value");
            count.incrementAndGet();
            context.proceed();
        };
        final WriterInterceptorsAdapter adapter = new WriterInterceptorsAdapter(interceptors,
                ProvidersPredicate.BINDING_GLOBAL);

        final AtomicBoolean proceeded = new AtomicBoolean();
        final Attributes attrs = new AttributeMap();
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext context = new RequestContextImpl(attrs, request, response);

        final ResponseEntityResolverContext rspCtx = new ResponseEntityResolverContext() {
            @Override
            public ResponseEntity httpEntity() {
                return mock(ResponseEntity.class);
            }

            @Override
            public ResponseEntityChannel channel() {
                return null;
            }

            @Override
            public RequestContext requestContext() {
                return context;
            }
        };

        ResolverExecutor<ResponseEntityResolverContext> executor =
                new ResolverExecutor<ResponseEntityResolverContext>() {
                    @Override
                    public ResponseEntityResolverContext context() {
                        return rspCtx;
                    }

                    @Override
                    public Object proceed() throws Exception {
                        proceeded.set(true);
                        return null;
                    }
                };

        response.headers().add("name0", "value0");

        // mismatched
        context.attrs().attr(AttributeKey.valueOf("$internal.handled.method")).set(mock(HandlerMethod.class));
        adapter.aroundResolve0(executor);
        assertEquals(0, count.intValue());
        assertTrue(proceeded.get());
        assertEquals(1, response.headers().size());
        assertEquals("value0", response.headers().get("name0"));
        assertEquals(0, map.size());

        // matched
        final ResponseContent content = mock(ResponseContent.class);
        when(content.alloc()).thenReturn(ByteBufAllocator.DEFAULT);
        context.attrs().attr(RequestContextImpl.RESPONSE_CONTENT).set(content);
        context.attrs().attr(AttributeKey.valueOf("$internal.handled.method")).remove();
        adapter.aroundResolve0(executor);
        assertEquals(1, count.intValue());
        assertTrue(proceeded.get());
        assertEquals(1, map.size());
        assertEquals("value0", map.getFirst("name0"));
        assertEquals(2, response.headers().size());
        assertEquals("value0", response.headers().get("name0"));
        assertEquals("value", response.headers().get("name"));

        // matched ====> exception occurred.
        interceptors[0] = ctx -> {
            map.putAll(ctx.getHeaders());
            ctx.getHeaders().add("name", "value");
            count.incrementAndGet();
            throw new RuntimeException();
        };
        count.set(0);
        proceeded.set(false);
        map.clear();
        response.headers().clear();
        response.headers().add("name0", "value0");

        assertThrows(RuntimeException.class, () -> adapter.aroundResolve0(executor));
        assertFalse(proceeded.get());
        assertEquals(1, count.intValue());
        assertEquals(1, map.size());
        assertEquals("value0", map.getFirst("name0"));
        assertEquals(2, response.headers().size());
        assertEquals("value0", response.headers().get("name0"));
        assertEquals("value", response.headers().get("name"));
    }

}

