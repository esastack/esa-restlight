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
package io.esastack.restlight.core.method;

import esa.commons.collection.MultiValueMap;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.handler.impl.HandlerContext;
import io.esastack.restlight.core.handler.impl.MockHandlerData;
import io.esastack.restlight.core.handler.impl.RouteHandlerMethodAdapterTest;
import io.esastack.restlight.core.handler.impl.SingletonRouteMethod;
import io.esastack.restlight.core.interceptor.Interceptor;
import io.esastack.restlight.core.interceptor.InterceptorPredicate;
import io.esastack.restlight.core.resolver.ExceptionResolver;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.route.RouteExecution;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SingletonRouteMethodTest extends RouteHandlerMethodAdapterTest {

    private static final Bean BEAN = new Bean();

    @Override
    protected SingletonRouteMethod build(HandlerMapping mapping,
                                         HandlerContext context,
                                         HandlerValueResolver handlerResolver,
                                         MultiValueMap<InterceptorPredicate, Interceptor> interceptors,
                                         ExceptionResolver<Throwable> exceptionResolver) {
        when(mapping.bean()).thenReturn(Optional.of(BEAN));
        return new SingletonRouteMethod(mapping, context, handlerResolver, interceptors, exceptionResolver);
    }

    @Test
    void testExecution() throws Exception {
        final MockHandlerData mockData = new MockHandlerData();

        final SingletonRouteMethod routeMethod = build(mockData.mapping(),
                mockData.context(),
                mockData.handlerValueResolver(),
                mockData.interceptors(),
                mockData.exceptionResolver()
        );
        final RequestContext context = mock(RequestContext.class);

        assertEquals(1, Bean.getBeanNum());

        RouteExecution execution = routeMethod.toExecution(context);
        execution.handle(context);
        assertEquals(1, Bean.getBeanNum());

        execution = routeMethod.toExecution(context);
        execution.handle(context);
        assertEquals(1, Bean.getBeanNum());
    }

    private static class Bean {

        private static AtomicInteger beanNum = new AtomicInteger(0);

        Bean() {
            beanNum.incrementAndGet();
        }

        static int getBeanNum() {
            return beanNum.get();
        }
    }
}
