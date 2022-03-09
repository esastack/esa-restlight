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
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.RouteMethodInfo;
import io.esastack.restlight.core.handler.impl.HandlerContext;
import io.esastack.restlight.core.handler.locate.MappingLocator;
import io.esastack.restlight.core.handler.locate.RouteMethodLocator;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.HandlerMethodImpl;
import io.esastack.restlight.server.route.Mapping;
import io.esastack.restlight.server.schedule.Schedulers;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
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

//    @Test
//    void testExtractRouteByMappingAndHandlerContext() throws NoSuchMethodException {
//        final HandlerContext ctx = mock(HandlerContext.class);
//        final Mapping mapping = Mapping.get();
//
//        when(ctx.handlerResolverLocator()).thenReturn(Optional.empty());
//        assertFalse(RouteUtils.extractRoute(ctx, null).isPresent());
//
//        final HandlerResolverFactory resolverFactory = mock(HandlerResolverFactory.class);
//        final ExceptionResolverFactory exceptionResolverFactory = mock(ExceptionResolverFactory.class);
//        final ExceptionResolver<Throwable> exceptionResolver =
//                (ctx0, throwable) -> Futures.completedFuture();
//        when(exceptionResolverFactory.createResolver(any()))
//                .thenReturn(exceptionResolver);
//
//        when(ctx.exceptionResolverFactory()).thenReturn(Optional.of(exceptionResolverFactory));
//
//        when(ctx.resolverFactory()).thenReturn(Optional.empty());
//        assertFalse(RouteUtils.extractRoute(ctx, mapping).isPresent());
//
//        when(ctx.resolverFactory()).thenReturn(Optional.of(resolverFactory));
//        when(ctx.exceptionResolverFactory()).thenReturn(Optional.empty());
//        assertFalse(RouteUtils.extractRoute(ctx, mapping).isPresent());
//
//        when(ctx.exceptionResolverFactory()).thenReturn(Optional.of(exceptionResolverFactory));
//
//        final RouteHandler routeHandler = mockRouteHandler();
//
//        assertThrows(NullPointerException.class, () -> RouteUtils.extractRoute(ctx, mapping, routeHandler));
//
//        when(ctx.schedulers()).thenReturn(Collections.singletonMap("foo", Schedulers.biz()));
//        Optional<Route> ret = RouteUtils.extractRoute(ctx, mapping, routeHandler);
//        assertTrue(ret.isPresent());
//        assertTrue(ret.get().handler().isPresent());
//        assertEquals(routeHandler.handler(), ret.get().handler().get());
//        assertSame(Schedulers.biz(), ret.get().scheduler());
//        assertSame(mapping, ret.get().mapping());
//    }
//
//    @Test
//    void testExtractRouteByMappingAndMethod() throws NoSuchMethodException {
//        final DeployContext<RestlightOptions> ctx = mock(DeployContext.class);
//        final Mapping mapping = Mapping.get();
//        assertFalse(RouteUtils.extractRoute(ctx,
//                RouteUtilsTest.class,
//                RouteUtilsTest.class.getDeclaredMethod("normal", String.class),
//                new RouteUtilsTest(),
//                null).isPresent());
//
//        final HandlerResolverFactory resolverFactory = mock(HandlerResolverFactory.class);
//        final ExceptionResolverFactory exceptionResolverFactory = mock(ExceptionResolverFactory.class);
//        final ExceptionResolver<Throwable> exceptionResolver =
//                (request, response, throwable) -> Futures.completedFuture();
//        when(exceptionResolverFactory.createResolver(any()))
//                .thenReturn(exceptionResolver);
//
//        when(ctx.resolverFactory()).thenReturn(Optional.of(resolverFactory));
//        when(ctx.exceptionResolverFactory()).thenReturn(Optional.of(exceptionResolverFactory));
//
//        when(ctx.routeHandlerLocator()).thenReturn(Optional.empty());
//        assertFalse(RouteUtils.extractRoute(ctx,
//                RouteUtilsTest.class,
//                RouteUtilsTest.class.getDeclaredMethod("normal", String.class),
//                new RouteUtilsTest(),
//                mapping).isPresent());
//
//        final RouteHandler handler = mockRouteHandler();
//        final RouteHandlerLocator locator = (userType, method, bean) -> Optional.of(handler);
//        when(ctx.routeHandlerLocator()).thenReturn(Optional.of(locator));
//        when(ctx.schedulers()).thenReturn(Collections.singletonMap("foo", Schedulers.biz()));
//
//        final Optional<Route> ret = RouteUtils.extractRoute(ctx,
//                RouteUtilsTest.class,
//                RouteUtilsTest.class.getDeclaredMethod("normal", String.class),
//                new RouteUtilsTest(),
//                mapping);
//
//
//        assertTrue(ret.isPresent());
//        assertTrue(ret.get().handler().isPresent());
//        assertEquals(handler.handler(), ret.get().handler().get());
//        assertSame(Schedulers.biz(), ret.get().scheduler());
//        assertSame(mapping, ret.get().mapping());
//    }
//
//    @Test
//    void testExtractRouteByMethod() throws NoSuchMethodException {
//        final DeployContext<RestlightOptions> ctx = mock(DeployContext.class);
//        assertFalse(RouteUtils.extractRoute(ctx,
//                RouteUtilsTest.class,
//                RouteUtilsTest.class.getDeclaredMethod("normal", String.class),
//                new RouteUtilsTest()).isPresent());
//
//
//        when(ctx.mappingLocator()).thenReturn(Optional.of(((userType, method) -> Optional.empty())));
//        assertFalse(RouteUtils.extractRoute(ctx,
//                RouteUtilsTest.class,
//                RouteUtilsTest.class.getDeclaredMethod("normal", String.class),
//                new RouteUtilsTest()).isPresent());
//
//        final Mapping mapping = Mapping.get();
//        when(ctx.mappingLocator()).thenReturn(Optional.of(((userType, method) -> Optional.of(mapping))));
//
//
//        final HandlerResolverFactory resolverFactory = mock(HandlerResolverFactory.class);
//        final ExceptionResolverFactory exceptionResolverFactory = mock(ExceptionResolverFactory.class);
//        final ExceptionResolver<Throwable> exceptionResolver =
//                (request, response, throwable) -> Futures.completedFuture();
//        when(exceptionResolverFactory.createResolver(any()))
//                .thenReturn(exceptionResolver);
//
//        when(ctx.resolverFactory()).thenReturn(Optional.of(resolverFactory));
//        when(ctx.exceptionResolverFactory()).thenReturn(Optional.of(exceptionResolverFactory));
//        final RouteHandler handler = mockRouteHandler();
//        final RouteHandlerLocator locator = (userType, method, bean) -> Optional.of(handler);
//        when(ctx.routeHandlerLocator()).thenReturn(Optional.of(locator));
//        when(ctx.schedulers()).thenReturn(Collections.singletonMap("foo", Schedulers.biz()));
//
//        final Optional<Route> ret = RouteUtils.extractRoute(ctx,
//                RouteUtilsTest.class,
//                RouteUtilsTest.class.getDeclaredMethod("normal", String.class),
//                new RouteUtilsTest());
//
//
//        assertTrue(ret.isPresent());
//        assertTrue(ret.get().handler().isPresent());
//        assertEquals(handler.handler(), ret.get().handler().get());
//        assertSame(Schedulers.biz(), ret.get().scheduler());
//        assertSame(mapping, ret.get().mapping());
//    }
//
//    private RouteHandler mockRouteHandler() throws NoSuchMethodException {
//        final RouteHandler routeHandler = mock(RouteHandler.class);
//        when(routeHandler.intercepted()).thenReturn(true);
//        when(routeHandler.scheduler()).thenReturn("foo");
//        when(routeHandler.handler())
//                .thenReturn(HandlerMethodImpl.of(RouteUtilsTest.class.getDeclaredMethod("normal", String.class),
//                        new RouteUtilsTest()));
//        return routeHandler;
//    }

    private String normal(String foo) {
        return foo;
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
