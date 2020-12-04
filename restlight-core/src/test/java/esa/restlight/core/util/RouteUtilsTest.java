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
package esa.restlight.core.util;

import com.google.common.util.concurrent.ListenableFuture;
import esa.restlight.core.DeployContext;
import esa.restlight.core.annotation.Scheduled;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.handler.RouteHandler;
import esa.restlight.core.handler.locate.RouteHandlerLocator;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.resolver.ExceptionResolver;
import esa.restlight.core.resolver.HandlerResolverFactory;
import esa.restlight.core.resolver.exception.ExceptionResolverFactory;
import esa.restlight.server.route.Mapping;
import esa.restlight.server.route.Route;
import esa.restlight.server.schedule.Schedulers;
import esa.restlight.server.util.Futures;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RouteUtilsTest {

    private static final Subject SUBJECT = new Subject();

    @Test
    void testIsConcurrent() throws NoSuchMethodException {
        final HandlerMethod method2
                = HandlerMethod.of(Subject.class.getDeclaredMethod("method2"), SUBJECT);
        assertFalse(RouteUtils.isConcurrent(method2));

        final HandlerMethod method3
                = HandlerMethod.of(Subject.class.getDeclaredMethod("method3"), SUBJECT);
        assertTrue(RouteUtils.isConcurrent(method3));

        final HandlerMethod method4
                = HandlerMethod.of(Subject.class.getDeclaredMethod("method4"), SUBJECT);
        assertTrue(RouteUtils.isConcurrent(method4));

        final HandlerMethod method5
                = HandlerMethod.of(Subject.class.getDeclaredMethod("method5"), SUBJECT);
        assertTrue(RouteUtils.isConcurrent(method5));

        final HandlerMethod method6
                = HandlerMethod.of(Subject.class.getDeclaredMethod("method6"), SUBJECT);
        assertTrue(RouteUtils.isConcurrent(method6));

        final HandlerMethod method7
                = HandlerMethod.of(Subject.class.getDeclaredMethod("method7"), SUBJECT);
        assertFalse(RouteUtils.isConcurrent(method7));
    }

    @Test
    void testDispatchingStrategy() throws NoSuchMethodException {
        final HandlerMethod method2
                = HandlerMethod.of(Subject.class.getDeclaredMethod("method2"), SUBJECT);
        assertEquals(Schedulers.BIZ, RouteUtils.scheduling(method2));
        assertEquals(Schedulers.IO, RouteUtils.scheduling(method2, Schedulers.IO));

        final HandlerMethod method8
                = HandlerMethod.of(Subject.class.getDeclaredMethod("method8"), SUBJECT);
        assertEquals(Schedulers.IO, RouteUtils.scheduling(method8));
        assertEquals(Schedulers.IO,
                RouteUtils.scheduling(method8, Schedulers.BIZ));
    }

    @Test
    void testExtractRouteByMappingAndRouteHandler() throws NoSuchMethodException {
        final DeployContext<RestlightOptions> ctx = mock(DeployContext.class);
        final Mapping mapping = Mapping.get();

        assertFalse(RouteUtils.extractRoute(ctx, null, mock(RouteHandler.class)).isPresent());

        final HandlerResolverFactory resolverFactory = mock(HandlerResolverFactory.class);
        final ExceptionResolverFactory exceptionResolverFactory = mock(ExceptionResolverFactory.class);
        final ExceptionResolver<Throwable> exceptionResolver =
                (request, response, throwable) -> Futures.completedFuture();
        when(exceptionResolverFactory.createResolver(any()))
                .thenReturn(exceptionResolver);

        when(ctx.exceptionResolverFactory()).thenReturn(Optional.of(exceptionResolverFactory));

        when(ctx.resolverFactory()).thenReturn(Optional.empty());
        assertFalse(RouteUtils.extractRoute(ctx, mapping, mock(RouteHandler.class)).isPresent());

        when(ctx.resolverFactory()).thenReturn(Optional.of(resolverFactory));
        when(ctx.exceptionResolverFactory()).thenReturn(Optional.empty());
        assertFalse(RouteUtils.extractRoute(ctx, mapping, mock(RouteHandler.class)).isPresent());

        when(ctx.exceptionResolverFactory()).thenReturn(Optional.of(exceptionResolverFactory));

        final RouteHandler routeHandler = mockRouteHandler();

        assertThrows(NullPointerException.class, () -> RouteUtils.extractRoute(ctx, mapping, routeHandler));

        when(ctx.schedulers()).thenReturn(Collections.singletonMap("foo", Schedulers.biz()));
        Optional<Route> ret = RouteUtils.extractRoute(ctx, mapping, routeHandler);
        assertTrue(ret.isPresent());
        assertTrue(ret.get().handler().isPresent());
        assertEquals(routeHandler.handler(), ret.get().handler().get());
        assertSame(Schedulers.biz(), ret.get().scheduler());
        assertSame(mapping, ret.get().mapping());
    }

    @Test
    void testExtractRouteByMappingAndMethod() throws NoSuchMethodException {
        final DeployContext<RestlightOptions> ctx = mock(DeployContext.class);
        final Mapping mapping = Mapping.get();
        assertFalse(RouteUtils.extractRoute(ctx,
                RouteUtilsTest.class,
                RouteUtilsTest.class.getDeclaredMethod("normal", String.class),
                new RouteUtilsTest(),
                null).isPresent());

        final HandlerResolverFactory resolverFactory = mock(HandlerResolverFactory.class);
        final ExceptionResolverFactory exceptionResolverFactory = mock(ExceptionResolverFactory.class);
        final ExceptionResolver<Throwable> exceptionResolver =
                (request, response, throwable) -> Futures.completedFuture();
        when(exceptionResolverFactory.createResolver(any()))
                .thenReturn(exceptionResolver);

        when(ctx.resolverFactory()).thenReturn(Optional.of(resolverFactory));
        when(ctx.exceptionResolverFactory()).thenReturn(Optional.of(exceptionResolverFactory));

        when(ctx.routeHandlerLocator()).thenReturn(Optional.empty());
        assertFalse(RouteUtils.extractRoute(ctx,
                RouteUtilsTest.class,
                RouteUtilsTest.class.getDeclaredMethod("normal", String.class),
                new RouteUtilsTest(),
                mapping).isPresent());

        final RouteHandler handler = mockRouteHandler();
        final RouteHandlerLocator locator = (userType, method, bean) -> Optional.of(handler);
        when(ctx.routeHandlerLocator()).thenReturn(Optional.of(locator));
        when(ctx.schedulers()).thenReturn(Collections.singletonMap("foo", Schedulers.biz()));

        final Optional<Route> ret = RouteUtils.extractRoute(ctx,
                RouteUtilsTest.class,
                RouteUtilsTest.class.getDeclaredMethod("normal", String.class),
                new RouteUtilsTest(),
                mapping);


        assertTrue(ret.isPresent());
        assertTrue(ret.get().handler().isPresent());
        assertEquals(handler.handler(), ret.get().handler().get());
        assertSame(Schedulers.biz(), ret.get().scheduler());
        assertSame(mapping, ret.get().mapping());
    }

    @Test
    void testExtractRouteByMethod() throws NoSuchMethodException {
        final DeployContext<RestlightOptions> ctx = mock(DeployContext.class);
        assertFalse(RouteUtils.extractRoute(ctx,
                RouteUtilsTest.class,
                RouteUtilsTest.class.getDeclaredMethod("normal", String.class),
                new RouteUtilsTest()).isPresent());


        when(ctx.mappingLocator()).thenReturn(Optional.of(((userType, method) -> Optional.empty())));
        assertFalse(RouteUtils.extractRoute(ctx,
                RouteUtilsTest.class,
                RouteUtilsTest.class.getDeclaredMethod("normal", String.class),
                new RouteUtilsTest()).isPresent());

        final Mapping mapping = Mapping.get();
        when(ctx.mappingLocator()).thenReturn(Optional.of(((userType, method) -> Optional.of(mapping))));


        final HandlerResolverFactory resolverFactory = mock(HandlerResolverFactory.class);
        final ExceptionResolverFactory exceptionResolverFactory = mock(ExceptionResolverFactory.class);
        final ExceptionResolver<Throwable> exceptionResolver =
                (request, response, throwable) -> Futures.completedFuture();
        when(exceptionResolverFactory.createResolver(any()))
                .thenReturn(exceptionResolver);

        when(ctx.resolverFactory()).thenReturn(Optional.of(resolverFactory));
        when(ctx.exceptionResolverFactory()).thenReturn(Optional.of(exceptionResolverFactory));
        final RouteHandler handler = mockRouteHandler();
        final RouteHandlerLocator locator = (userType, method, bean) -> Optional.of(handler);
        when(ctx.routeHandlerLocator()).thenReturn(Optional.of(locator));
        when(ctx.schedulers()).thenReturn(Collections.singletonMap("foo", Schedulers.biz()));

        final Optional<Route> ret = RouteUtils.extractRoute(ctx,
                RouteUtilsTest.class,
                RouteUtilsTest.class.getDeclaredMethod("normal", String.class),
                new RouteUtilsTest());


        assertTrue(ret.isPresent());
        assertTrue(ret.get().handler().isPresent());
        assertEquals(handler.handler(), ret.get().handler().get());
        assertSame(Schedulers.biz(), ret.get().scheduler());
        assertSame(mapping, ret.get().mapping());
    }

    private RouteHandler mockRouteHandler() throws NoSuchMethodException {
        final RouteHandler routeHandler = mock(RouteHandler.class);
        when(routeHandler.intercepted()).thenReturn(true);
        when(routeHandler.scheduler()).thenReturn("foo");
        when(routeHandler.handler())
                .thenReturn(HandlerMethod.of(RouteUtilsTest.class.getDeclaredMethod("normal", String.class),
                        new RouteUtilsTest()));
        return routeHandler;
    }

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
