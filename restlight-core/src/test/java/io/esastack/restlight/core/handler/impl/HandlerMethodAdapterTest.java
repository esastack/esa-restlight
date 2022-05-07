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

import io.esastack.restlight.core.annotation.Intercepted;
import io.esastack.restlight.core.handler.method.DefaultResolvableParamPredicate;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.HandlerMethodImpl;
import io.esastack.restlight.core.resolver.context.ContextResolver;
import io.esastack.restlight.core.resolver.factory.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.param.ParamResolver;
import io.esastack.restlight.core.resolver.reqentity.RequestEntityResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HandlerMethodAdapterTest {

    @Test
    void testConstruct() throws NoSuchMethodException {
        final Method method = Subject.class
                .getDeclaredMethod("params", String.class, int.class, List.class);
        final HandlerMethod handlerMethod = HandlerMethodImpl.of(Subject.class, method);
        final HandlerContext context = mock(HandlerContext.class);
        when(context.paramPredicate()).thenReturn(Optional.empty());
        when(context.resolverFactory()).thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalStateException.class,
                () -> new HandlerMethodAdapter<>(context, handlerMethod));

        when(context.paramPredicate()).thenReturn(Optional.of(param -> false));
        final HandlerResolverFactory resolverFactory = mock(HandlerResolverFactory.class);
        when(context.resolverFactory()).thenReturn(Optional.of(resolverFactory));

        HandlerMethodAdapter<HandlerMethod> handlerMethodAdapter
                = new HandlerMethodAdapter<>(context, handlerMethod);

        assertEquals(handlerMethod.parameters(), handlerMethodAdapter.parameters());
        assertEquals(handlerMethod.hasMethodAnnotation(Intercepted.class, false),
                handlerMethodAdapter.hasMethodAnnotation(Intercepted.class, false));
        assertEquals(handlerMethod.hasClassAnnotation(Intercepted.class, false),
                handlerMethodAdapter.hasClassAnnotation(Intercepted.class, false));
        assertEquals(handlerMethod.beanType(), handlerMethodAdapter.beanType());
        assertEquals(handlerMethod.method(), handlerMethodAdapter.method());
        assertTrue(handlerMethodAdapter.isConcurrent());
        assertEquals(handlerMethod, handlerMethodAdapter.handlerMethod());
        assertEquals(context, handlerMethodAdapter.context());

        //TODO 这块是不是应该在构造的时候发现param被test时就抛出异常。
        assertEquals(0, handlerMethodAdapter.paramResolvers().length);
    }

    @Test
    void testParamResolvers() throws NoSuchMethodException {
        final Method method = Subject.class
                .getDeclaredMethod("params", String.class, int.class, List.class);
        final HandlerMethod handlerMethod = HandlerMethodImpl.of(Subject.class, method);
        final HandlerContext context = mock(HandlerContext.class);
        when(context.paramPredicate()).thenReturn(Optional.of(new DefaultResolvableParamPredicate()));
        final HandlerResolverFactory resolverFactory = mock(HandlerResolverFactory.class);
        when(context.resolverFactory()).thenReturn(Optional.of(resolverFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new HandlerMethodAdapter<>(context, handlerMethod));

        ContextResolver p1Resolver = mock(ContextResolver.class);
        when(resolverFactory.getContextResolver(handlerMethod.parameters()[0]))
                .thenReturn(p1Resolver);

        ParamResolver p2Resolver = mock(ParamResolver.class);
        when(resolverFactory.getParamResolver(handlerMethod.parameters()[1]))
                .thenReturn(p2Resolver);

        List<RequestEntityResolver> p3Resolver = mock(List.class);
        when(resolverFactory.getRequestEntityResolvers(handlerMethod.parameters()[2]))
                .thenReturn(p3Resolver);

        HandlerMethodAdapter<HandlerMethod> handlerMethodAdapter
                = new HandlerMethodAdapter<>(context, handlerMethod);

        assertEquals(3, handlerMethodAdapter.paramResolvers().length);
    }


    @Intercepted(false)
    private static class Subject {

        @Intercepted(false)
        CompletableFuture<Object> params(String p0, int p1, List<String> p2) {
            return CompletableFuture.completedFuture(null);
        }

    }
}
