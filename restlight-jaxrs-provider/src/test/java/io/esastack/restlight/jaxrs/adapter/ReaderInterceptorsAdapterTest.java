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
import io.esastack.restlight.core.context.RequestEntity;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.resolver.ResolverExecutor;
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolverContext;
import jakarta.ws.rs.ext.ReaderInterceptor;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ReaderInterceptorsAdapterTest {

    @Test
    void testAll() throws Throwable {
        assertThrows(NullPointerException.class,
                () -> new ReaderInterceptorsAdapter(null, ProvidersPredicate.BINDING_GLOBAL));

        assertThrows(NullPointerException.class,
                () -> new ReaderInterceptorsAdapter(new ReaderInterceptor[0], null));

        final AtomicInteger count = new AtomicInteger();
        final ReaderInterceptor[] interceptors = new ReaderInterceptor[1];
        interceptors[0] = context -> {
            count.incrementAndGet();
            return context.proceed();
        };

        final ReaderInterceptorsAdapter adapter = new ReaderInterceptorsAdapter(interceptors,
                ProvidersPredicate.BINDING_GLOBAL);
        assertTrue(adapter.supports(null));

        final Attributes attrs = new AttributeMap();
        final RequestContext context =
                new RequestContextImpl(attrs, mock(HttpRequest.class), mock(HttpResponse.class));
        final RequestEntityResolverContext context1 = new RequestEntityResolverContext() {
            @Override
            public RequestEntity httpEntity() {
                return null;
            }

            @Override
            public RequestContext requestContext() {
                return context;
            }
        };
        final ResolverExecutor<RequestEntityResolverContext> executor =
                new ResolverExecutor<RequestEntityResolverContext>() {
                    @Override
                    public RequestEntityResolverContext context() {
                        return context1;
                    }

                    @Override
                    public Object proceed() throws Exception {
                        return "ABC";
                    }
        };

        assertEquals("ABC", adapter.aroundResolve(executor));
        assertEquals(1, count.intValue());

        count.set(0);
        attrs.attr(AttributeKey.valueOf("$internal.handled.method")).set(mock(HandlerMethod.class));
        assertEquals("ABC", adapter.aroundResolve(executor));
        assertEquals(0, count.intValue());
    }

}

