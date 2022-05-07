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
package io.esastack.restlight.core.util;

import com.google.common.util.concurrent.ListenableFuture;
import io.esastack.restlight.core.annotation.Scheduled;
import io.esastack.restlight.core.handler.HandlerFactory;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.handler.RouteMethodInfo;
import io.esastack.restlight.core.handler.impl.HandlerContext;
import io.esastack.restlight.core.handler.impl.MockHandlerData;
import io.esastack.restlight.core.locator.HandlerValueResolverLocator;
import io.esastack.restlight.core.locator.MappingLocator;
import io.esastack.restlight.core.locator.RouteMethodLocator;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.HandlerMethodImpl;
import io.esastack.restlight.core.resolver.exception.ExceptionResolver;
import io.esastack.restlight.core.resolver.factory.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.exception.ExceptionResolverFactory;
import io.esastack.restlight.core.route.Mapping;
import io.esastack.restlight.core.route.Route;
import io.esastack.restlight.core.server.processor.schedule.Schedulers;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RouteUtilsTest {

    private static final Subject SUBJECT = new Subject();

    @Test
    void testExtractHandlerMapping() throws NoSuchMethodException {
        final HandlerContext handlerContext = mock(HandlerContext.class);
        final HandlerMapping parent = mock(HandlerMapping.class);
        final Class<?> userType = Subject.class;
        final Method method = Subject.class.getDeclaredMethod("method8");
        final MappingLocator mappingLocator = mock(MappingLocator.class);
        final RouteMethodLocator methodLocator = mock(RouteMethodLocator.class);

        //mappingLocator or methodLocator is null
        when(handlerContext.mappingLocator()).thenReturn(Optional.empty());
        when(handlerContext.methodLocator()).thenReturn(Optional.empty());
        assertFalse(RouteUtils.extractHandlerMapping(handlerContext, parent, SUBJECT, userType, method).isPresent());

        when(handlerContext.mappingLocator()).thenReturn(Optional.of(mappingLocator));
        assertFalse(RouteUtils.extractHandlerMapping(handlerContext, parent, SUBJECT, userType, method).isPresent());

        when(handlerContext.mappingLocator()).thenReturn(Optional.empty());
        when(handlerContext.methodLocator()).thenReturn(Optional.of(methodLocator));
        assertFalse(RouteUtils.extractHandlerMapping(handlerContext, parent, SUBJECT, userType, method).isPresent());

        //test mapping or handlerMethod is null
        when(handlerContext.mappingLocator()).thenReturn(Optional.of(mappingLocator));
        when(mappingLocator.getMapping(any(), any(), any())).thenReturn(Optional.empty());
        RouteMethodInfo methodInfo = mock(RouteMethodInfo.class);
        when(methodLocator.getRouteMethodInfo(any(), any(), any())).thenReturn(Optional.of(methodInfo));
        assertFalse(RouteUtils.extractHandlerMapping(handlerContext, parent, SUBJECT, userType, method).isPresent());

        when(methodLocator.getRouteMethodInfo(any(), any(), any())).thenReturn(Optional.empty());
        Mapping mapping = mock(Mapping.class);
        when(mappingLocator.getMapping(any(), any(), any())).thenReturn(Optional.of(mapping));
        assertFalse(RouteUtils.extractHandlerMapping(handlerContext, parent, SUBJECT, userType, method).isPresent());

        when(methodLocator.getRouteMethodInfo(parent, userType, method)).thenReturn(Optional.of(methodInfo));
        when(mappingLocator.getMapping(parent, userType, method)).thenReturn(Optional.of(mapping));
        HandlerMapping handlerMapping =
                RouteUtils.extractHandlerMapping(handlerContext, parent, SUBJECT, userType, method).get();

        assertEquals(parent, handlerMapping.parent().get());
        assertEquals(mapping, handlerMapping.mapping());
        assertEquals(SUBJECT, handlerMapping.bean().get());
        assertEquals(methodInfo, handlerMapping.methodInfo());
    }

    @Test
    void testIsConcurrent() throws NoSuchMethodException {
        final HandlerMethod method2
                = HandlerMethodImpl.of(Subject.class, Subject.class.getDeclaredMethod("method2"));
        assertFalse(RouteUtils.isConcurrent(method2));

        final HandlerMethod method3
                = HandlerMethodImpl.of(Subject.class, Subject.class.getDeclaredMethod("method3"));
        assertTrue(RouteUtils.isConcurrent(method3));

        final HandlerMethod method4
                = HandlerMethodImpl.of(Subject.class, Subject.class.getDeclaredMethod("method4"));
        assertTrue(RouteUtils.isConcurrent(method4));

        final HandlerMethod method5
                = HandlerMethodImpl.of(Subject.class, Subject.class.getDeclaredMethod("method5"));
        assertTrue(RouteUtils.isConcurrent(method5));

        final HandlerMethod method6
                = HandlerMethodImpl.of(Subject.class, Subject.class.getDeclaredMethod("method6"));
        assertTrue(RouteUtils.isConcurrent(method6));

        final HandlerMethod method7
                = HandlerMethodImpl.of(Subject.class, Subject.class.getDeclaredMethod("method7"));
        assertFalse(RouteUtils.isConcurrent(method7));
    }

    @Test
    void testDispatchingStrategy() throws NoSuchMethodException {
        final HandlerMethod method2
                = HandlerMethodImpl.of(Subject.class, Subject.class.getDeclaredMethod("method2"));
        assertEquals(Schedulers.BIZ, RouteUtils.scheduling(method2, null));
        assertEquals(Schedulers.IO, RouteUtils.scheduling(method2, Schedulers.IO));

        final HandlerMethod method8
                = HandlerMethodImpl.of(Subject.class, Subject.class.getDeclaredMethod("method8"));
        assertEquals(Schedulers.IO, RouteUtils.scheduling(method8, null));
        assertEquals(Schedulers.IO,
                RouteUtils.scheduling(method8, Schedulers.BIZ));
    }

    @Test
    void testExtractRoute() throws Exception {
        final MockHandlerData mockData = new MockHandlerData();
        final HandlerContext ctx = mockData.context();
        final HandlerMapping mapping = mockData.mapping();
        when(mapping.mapping()).thenReturn(Mapping.mapping());

        when(ctx.handlerResolverLocator()).thenReturn(Optional.empty());
        assertFalse(RouteUtils.extractRoute(ctx, null).isPresent());

        final HandlerValueResolverLocator resolverLocator = mock(HandlerValueResolverLocator.class);
        final HandlerValueResolver valueResolver = mockData.handlerValueResolver();
        final HandlerFactory handlerFactory = mock(HandlerFactory.class);
        final HandlerResolverFactory resolverFactory = mockData.resolverFactory();
        final ExceptionResolverFactory exceptionResolverFactory = mock(ExceptionResolverFactory.class);
        final ExceptionResolver<Throwable> exceptionResolver =
                mockData.exceptionResolver();
        when(exceptionResolverFactory.createResolver(any()))
                .thenReturn(exceptionResolver);

        when(ctx.handlerResolverLocator()).thenReturn(Optional.of(resolverLocator));
        when(ctx.exceptionResolverFactory()).thenReturn(Optional.of(exceptionResolverFactory));
        when(resolverLocator.getHandlerValueResolver(any())).thenReturn(Optional.of(valueResolver));

        when(ctx.resolverFactory()).thenReturn(Optional.empty());
        assertFalse(RouteUtils.extractRoute(ctx, mapping).isPresent());

        when(ctx.resolverFactory()).thenReturn(Optional.of(resolverFactory));
        when(ctx.exceptionResolverFactory()).thenReturn(Optional.empty());
        assertFalse(RouteUtils.extractRoute(ctx, mapping).isPresent());

        when(ctx.exceptionResolverFactory()).thenReturn(Optional.of(exceptionResolverFactory));
        when(ctx.handlerFactory()).thenReturn(Optional.of(handlerFactory));

        when(ctx.schedulers()).thenReturn(Collections.singletonMap("BIZ", Schedulers.biz()));
        Optional<Route> ret = RouteUtils.extractRoute(ctx, mapping);
        assertTrue(ret.isPresent());
        assertTrue(ret.get().handler().isPresent());
        assertSame(Schedulers.biz(), ret.get().scheduler());
        assertSame(mapping.mapping(), ret.get().mapping());
    }

    private static class Subject {

        void method2() {
        }

        CompletableFuture<Void> method3() {
            return null;
        }

        ListenableFuture<Void> method4() {
            return null;
        }

        Future<Void> method5() {
            return null;
        }

        Promise<Void> method6() {
            return null;
        }

        Object method7() {
            return null;
        }

        @Scheduled(Schedulers.IO)
        void method8() {
        }
    }
}
