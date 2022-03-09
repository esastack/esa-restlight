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

import esa.commons.collection.MultiValueMap;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.interceptor.Interceptor;
import io.esastack.restlight.core.interceptor.InterceptorPredicate;
import io.esastack.restlight.core.resolver.ExceptionResolver;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.route.RouteExecution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

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
    void testConstruct() throws NoSuchMethodException {
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
    void testInterceptor(){
        //TODO
    }

}
