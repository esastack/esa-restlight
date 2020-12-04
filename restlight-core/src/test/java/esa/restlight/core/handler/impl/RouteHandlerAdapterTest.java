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

import esa.commons.collection.LinkedMultiValueMap;
import esa.commons.collection.MultiValueMap;
import esa.restlight.core.handler.RouteHandler;
import esa.restlight.core.interceptor.Interceptor;
import esa.restlight.core.interceptor.InterceptorPredicate;
import esa.restlight.core.interceptor.InternalInterceptor;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.resolver.ExceptionResolver;
import esa.restlight.core.resolver.HandlerResolverFactory;
import esa.restlight.test.mock.MockAsyncRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RouteHandlerAdapterTest {

    @Test
    void testNormal() throws NoSuchMethodException {
        final RouteHandler handler = mock(RouteHandler.class);
        when(handler.intercepted()).thenReturn(true);
        when(handler.scheduler()).thenReturn("foo");
        when(handler.toString()).thenReturn("str");
        when(handler.handler())
                .thenReturn(HandlerMethod.of(HandlerAdapterTest.class.getDeclaredMethod("normal", String.class),
                        new HandlerAdapterTest()));
        final ExceptionResolver<Throwable> exceptionResolver = (request, response, throwable) -> null;
        final RouteHandlerAdapter adapter =
                new RouteHandlerAdapter(handler, mock(HandlerResolverFactory.class), null, exceptionResolver);


        assertSame(exceptionResolver, adapter.exceptionResolver());
        assertEquals(handler.scheduler(), adapter.scheduler());
        assertTrue(handler.intercepted() && adapter.intercepted());
        assertEquals(handler.toString(), adapter.toString());
        assertNull(adapter.getMatchingInterceptors(MockAsyncRequest.aMockRequest().build()));
    }

    @Test
    void testMatchSimpleInterceptor() throws NoSuchMethodException {
        final RouteHandler handler = mock(RouteHandler.class);
        when(handler.intercepted()).thenReturn(true);
        when(handler.handler())
                .thenReturn(HandlerMethod.of(HandlerAdapterTest.class.getDeclaredMethod("normal", String.class),
                        new HandlerAdapterTest()));
        final MultiValueMap<InterceptorPredicate, Interceptor> interceptors = new LinkedMultiValueMap<>();
        final Interceptor interceptor0 = new Interceptor() {
            @Override
            public InterceptorPredicate predicate() {
                return request -> true;
            }

            @Override
            public int getOrder() {
                return 0;
            }
        };
        final Interceptor interceptor1 = new Interceptor() {
            @Override
            public InterceptorPredicate predicate() {
                return request -> true;
            }

            @Override
            public int getOrder() {
                return -1;
            }
        };
        final Interceptor interceptor2 = new Interceptor() {
            @Override
            public InterceptorPredicate predicate() {
                return request -> false;
            }

            @Override
            public int getOrder() {
                return 1;
            }
        };
        interceptors.add(interceptor0.predicate(), interceptor0);
        interceptors.add(interceptor1.predicate(), interceptor1);
        interceptors.add(interceptor2.predicate(), interceptor2);

        final RouteHandlerAdapter adapter =
                new RouteHandlerAdapter(handler, mock(HandlerResolverFactory.class), interceptors, null);

        final List<InternalInterceptor> matched =
                adapter.getMatchingInterceptors(MockAsyncRequest.aMockRequest().build());
        assertNotNull(matched);
        assertEquals(2, matched.size());
        assertSame(interceptor1, matched.get(0));
        assertSame(interceptor0, matched.get(1));
    }

    @Test
    void testMatchComplexInterceptor() throws NoSuchMethodException {
        final RouteHandler handler = mock(RouteHandler.class);
        when(handler.intercepted()).thenReturn(true);
        when(handler.handler())
                .thenReturn(HandlerMethod.of(HandlerAdapterTest.class.getDeclaredMethod("normal", String.class),
                        new HandlerAdapterTest()));
        final MultiValueMap<InterceptorPredicate, Interceptor> interceptors = new LinkedMultiValueMap<>();
        final InterceptorPredicate p0 = mock(InterceptorPredicate.class);
        when(p0.test(any())).thenReturn(true);
        final InterceptorPredicate p1 = mock(InterceptorPredicate.class);
        when(p1.test(any())).thenReturn(true);
        final InterceptorPredicate p2 = mock(InterceptorPredicate.class);
        when(p2.test(any())).thenReturn(false);
        final Interceptor interceptor0 = new Interceptor() {
            @Override
            public InterceptorPredicate predicate() {
                return p0;
            }

            @Override
            public int affinity() {
                return 30;
            }

            @Override
            public int getOrder() {
                return 0;
            }
        };
        final Interceptor interceptor1 = new Interceptor() {
            @Override
            public InterceptorPredicate predicate() {
                return p1;
            }

            @Override
            public int affinity() {
                return 30;
            }

            @Override
            public int getOrder() {
                return -1;
            }
        };
        final Interceptor interceptor2 = new Interceptor() {
            @Override
            public InterceptorPredicate predicate() {
                return p2;
            }

            @Override
            public int affinity() {
                return 30;
            }

            @Override
            public int getOrder() {
                return 1;
            }
        };
        interceptors.add(p0, interceptor0);
        interceptors.add(p0, interceptor0);
        interceptors.add(p1, interceptor1);
        interceptors.add(p1, interceptor1);
        interceptors.add(p2, interceptor2);
        interceptors.add(p2, interceptor2);

        final RouteHandlerAdapter adapter =
                new RouteHandlerAdapter(handler, mock(HandlerResolverFactory.class), interceptors, null);

        List<InternalInterceptor> matched = adapter.getMatchingInterceptors(MockAsyncRequest.aMockRequest().build());
        assertNotNull(matched);
        assertEquals(4, matched.size());
        assertSame(interceptor1, matched.get(0));
        assertSame(interceptor1, matched.get(1));
        assertSame(interceptor0, matched.get(2));
        assertSame(interceptor0, matched.get(3));
        verify(p0, times(1)).test(any());
        verify(p1, times(1)).test(any());

        // match 2 times for testing cleared thread local status
        matched = adapter.getMatchingInterceptors(MockAsyncRequest.aMockRequest().build());
        assertNotNull(matched);
        assertEquals(4, matched.size());
        assertSame(interceptor1, matched.get(0));
        assertSame(interceptor1, matched.get(1));
        assertSame(interceptor0, matched.get(2));
        assertSame(interceptor0, matched.get(3));
    }


    private String normal(String foo) {
        return foo;
    }

}
