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
package io.esastack.restlight.core.handler.impl;

import esa.commons.collection.LinkedMultiValueMap;
import esa.commons.collection.MultiValueMap;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.interceptor.Interceptor;
import io.esastack.restlight.core.interceptor.InterceptorPredicate;
import io.esastack.restlight.core.interceptor.InternalInterceptor;
import io.esastack.restlight.core.resolver.ExceptionResolver;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.route.RouteExecution;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RouteHandlerMethodAdapterTest {

    protected RouteHandlerMethodAdapter build(HandlerMapping mapping,
                                              HandlerContext context,
                                              HandlerValueResolver handlerResolver,
                                              MultiValueMap<InterceptorPredicate, Interceptor> interceptors,
                                              ExceptionResolver<Throwable> exceptionResolver) {
        return new RouteHandlerMethodAdapter(mapping, context, handlerResolver, interceptors, exceptionResolver) {
            @Override
            public RouteExecution toExecution(RequestContext context) {
                return null;
            }
        };
    }

    @Test
    void testConstruct() throws Exception {
        MockHandlerData mockData = new MockHandlerData();

        RouteHandlerMethodAdapter routeHandlerMethodAdapter = build(mockData.mapping(),
                mockData.context(),
                mockData.handlerValueResolver(),
                mockData.interceptors(),
                mockData.exceptionResolver()
        );

        assertNull(routeHandlerMethodAdapter.getMatchingInterceptors(mock(RequestContext.class)));
        assertEquals(0, routeHandlerMethodAdapter.filters().length);
        assertEquals(mockData.handlerValueResolver(), routeHandlerMethodAdapter.handlerResolver());
        assertEquals(mockData.exceptionResolver(), routeHandlerMethodAdapter.exceptionResolver());
        assertEquals(mockData.mapping(), routeHandlerMethodAdapter.mapping());
    }

    @Test
    void testMatchSimpleInterceptor() throws Exception {
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

        final MockHandlerData mockData = new MockHandlerData();
        final RouteHandlerMethodAdapter routeHandlerMethodAdapter = build(mockData.mapping(),
                mockData.context(),
                mockData.handlerValueResolver(),
                interceptors,
                mockData.exceptionResolver()
        );

        final List<InternalInterceptor> matched =
                routeHandlerMethodAdapter.getMatchingInterceptors(mock(RequestContext.class));
        assertNotNull(matched);
        assertEquals(2, matched.size());
        assertSame(interceptor1, matched.get(0));
        assertSame(interceptor0, matched.get(1));
    }

    @Test
    void testMatchComplexInterceptor() throws Exception {
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

        final MockHandlerData mockData = new MockHandlerData();
        final RouteHandlerMethodAdapter routeHandlerMethodAdapter = build(mockData.mapping(),
                mockData.context(),
                mockData.handlerValueResolver(),
                interceptors,
                mockData.exceptionResolver()
        );

        List<InternalInterceptor> matched =
                routeHandlerMethodAdapter.getMatchingInterceptors(mock(RequestContext.class));

        assertNotNull(matched);
        assertEquals(4, matched.size());
        assertSame(interceptor1, matched.get(0));
        assertSame(interceptor1, matched.get(1));
        assertSame(interceptor0, matched.get(2));
        assertSame(interceptor0, matched.get(3));
        verify(p0, times(1)).test(any());
        verify(p1, times(1)).test(any());

        // match 2 times for testing cleared thread local status
        matched = routeHandlerMethodAdapter.getMatchingInterceptors(mock(RequestContext.class));
        assertNotNull(matched);
        assertEquals(4, matched.size());
        assertSame(interceptor1, matched.get(0));
        assertSame(interceptor1, matched.get(1));
        assertSame(interceptor0, matched.get(2));
        assertSame(interceptor0, matched.get(3));
        verify(p0, times(2)).test(any());
        verify(p1, times(2)).test(any());
    }

}
