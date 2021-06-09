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
package esa.restlight.springmvc.resolver.exception;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.handler.HandlerAdvice;
import esa.restlight.core.handler.impl.HandlerImpl;
import esa.restlight.core.handler.locate.AbstractRouteHandlerLocator;
import esa.restlight.core.handler.locate.HandlerLocator;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.resolver.ExceptionResolver;
import esa.restlight.core.resolver.HandlerResolverFactory;
import esa.restlight.core.resolver.HandlerResolverFactoryImpl;
import esa.restlight.core.resolver.ReturnValueResolverAdapter;
import esa.restlight.core.resolver.exception.ExceptionMapper;
import esa.restlight.core.serialize.FastJsonHttpBodySerializer;
import esa.restlight.server.schedule.Schedulers;
import esa.restlight.springmvc.MockUtils;
import esa.restlight.springmvc.annotation.shaded.ExceptionHandler0;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SpringMvcExceptionResolverFactoryTest {

    private static final String NAME = "name";

    private HandlerLocator handlerLocator;

    @BeforeEach
    void setUp() {
        assumeTrue(ExceptionHandler0.shadedClass().getName().startsWith("org.springframework"));
        handlerLocator = MockUtils.mockRouteHandlerLocator();
    }

    @Test
    void testExceptionResolverInController() throws NoSuchMethodException {
        final ControllerExceptionMapping bean = new ControllerExceptionMapping();

        final SpringMvcExceptionResolverFactory factory = new SpringMvcExceptionResolverFactory(null,
                Collections.singleton(bean), null, handlerLocator,
                MockUtils.mockResolverFactory());

        final ExceptionResolver<Throwable> resolver1 = factory.createResolver(new HandlerImpl(HandlerMethod.of(
                ControllerExceptionMapping.class.getDeclaredMethod("method1",
                        AsyncResponse.class, IllegalArgumentException.class),
                bean)));
        assertNotNull(resolver1);
        final AsyncResponse response1 = MockAsyncResponse.aMockResponse().build();
        resolver1.handleException(MockAsyncRequest.aMockRequest().build(), response1, new IllegalArgumentException());
        assertEquals("IllegalArgumentException", response1.getHeader(NAME));

        ExceptionResolver<Throwable> resolver2 = factory.createResolver(new HandlerImpl(HandlerMethod.of(
                ControllerExceptionMapping.class.getDeclaredMethod("method2", AsyncResponse.class),
                bean)));
        assertNotNull(resolver2);
        final AsyncResponse response2 = MockAsyncResponse.aMockResponse().build();
        resolver2.handleException(null, response2, new NullPointerException());
        assertEquals("NullPointerException", response2.getHeader(NAME));

        final AsyncResponse response3 = MockAsyncResponse.aMockResponse().build();
        resolver2.handleException(null, response3, new RuntimeException());
        assertNull(response3.getHeader(NAME));
    }

    @Test
    void testExceptionResolverInControllerAdvice() throws NoSuchMethodException {
        final ControllerAdviceExceptionMapping bean = new ControllerAdviceExceptionMapping();

        final SpringMvcExceptionResolverFactory factory = new SpringMvcExceptionResolverFactory(null,
                Collections.singleton(bean), null, handlerLocator,
                MockUtils.mockResolverFactory());

        final ExceptionResolver<Throwable> resolver1 = factory.createResolver(new HandlerImpl(HandlerMethod.of(
                ControllerAdviceExceptionMapping.class.getDeclaredMethod("method1",
                        AsyncResponse.class, IllegalArgumentException.class),
                bean)));
        assertNotNull(resolver1);
        final AsyncResponse response1 = MockAsyncResponse.aMockResponse().build();
        resolver1.handleException(MockAsyncRequest.aMockRequest().build(), response1, new IllegalArgumentException());
        assertEquals("IllegalArgumentException", response1.getHeader(NAME));

        ExceptionResolver<Throwable> resolver2 = factory.createResolver(new HandlerImpl(HandlerMethod.of(
                ControllerExceptionMapping.class.getDeclaredMethod("method2", AsyncResponse.class),
                bean)));
        assertNotNull(resolver2);
        final AsyncResponse response2 = MockAsyncResponse.aMockResponse().build();
        resolver2.handleException(null, response2, new NullPointerException());
        assertEquals("NullPointerException", response2.getHeader(NAME));

        final AsyncResponse response3 = MockAsyncResponse.aMockResponse().build();
        resolver2.handleException(null, response3, new RuntimeException());
        assertNull(response3.getHeader(NAME));
    }

    @Test
    void testExceptionResolverInRestControllerAdvice() throws NoSuchMethodException {
        final RestControllerAdviceExceptionMapping bean = new RestControllerAdviceExceptionMapping();

        final SpringMvcExceptionResolverFactory factory = new SpringMvcExceptionResolverFactory(null,
                Collections.singleton(bean), null, handlerLocator,
                MockUtils.mockResolverFactory());

        final ExceptionResolver<Throwable> resolver1 = factory.createResolver(new HandlerImpl(HandlerMethod.of(
                RestControllerAdviceExceptionMapping.class.getDeclaredMethod("method1",
                        AsyncResponse.class, IllegalArgumentException.class),
                bean)));
        assertNotNull(resolver1);
        final AsyncResponse response1 = MockAsyncResponse.aMockResponse().build();
        resolver1.handleException(MockAsyncRequest.aMockRequest().build(), response1, new IllegalArgumentException());
        assertEquals("IllegalArgumentException", response1.getHeader(NAME));

        ExceptionResolver<Throwable> resolver2 = factory.createResolver(new HandlerImpl(HandlerMethod.of(
                ControllerExceptionMapping.class.getDeclaredMethod("method2", AsyncResponse.class),
                bean)));
        assertNotNull(resolver2);
        final AsyncResponse response2 = MockAsyncResponse.aMockResponse().build();
        resolver2.handleException(null, response2, new NullPointerException());
        assertEquals("NullPointerException", response2.getHeader(NAME));

        final AsyncResponse response3 = MockAsyncResponse.aMockResponse().build();
        resolver2.handleException(null, response3, new RuntimeException());
        assertNull(response3.getHeader(NAME));
    }

    @Test
    void testDuplicatedResolversInController() {
        DuplicatedException0 controllerBean = new DuplicatedException0();
        assertThrows(IllegalStateException.class, () -> new SpringMvcExceptionResolverFactory(null,
                Collections.singletonList(controllerBean),
                null,
                MockUtils.mockRouteHandlerLocator(),
                MockUtils.mockResolverFactory()));
    }

    @Test
    void testDuplicatedResolversInControllerAdvice() {
        DuplicatedException1 advice = new DuplicatedException1();
        assertThrows(IllegalStateException.class, () -> new SpringMvcExceptionResolverFactory(null,
                null,
                Collections.singletonList(advice),
                MockUtils.mockRouteHandlerLocator(),
                MockUtils.mockResolverFactory()));
    }

    @Test
    void testNoResolverToExceptionInController() {
        NoExceptionMapping0 controllerBean = new NoExceptionMapping0();
        assertThrows(IllegalStateException.class, () -> new SpringMvcExceptionResolverFactory(null,
                Collections.singletonList(controllerBean),
                null,
                MockUtils.mockRouteHandlerLocator(),
                MockUtils.mockResolverFactory()));
    }

    @Test
    void testNoResolverToExceptionInControllerAdvice() {
        NoExceptionMapping1 advice = new NoExceptionMapping1();
        assertThrows(IllegalStateException.class, () -> new SpringMvcExceptionResolverFactory(null,
                null,
                Collections.singletonList(advice),
                MockUtils.mockRouteHandlerLocator(),
                MockUtils.mockResolverFactory()));
    }

    @Test
    void testCreateMappersFromController() {
        final HandlerResolverFactoryImpl handlerResolverFactory = newHandlerResolverFactory();
        final HandlerLocator locator = newHandlerLocator();
        final SpringMvcExceptionResolverFactory factory = newExceptionResolverFactory(
                locator, handlerResolverFactory);

        final ControllerExceptionMapping controller = new ControllerExceptionMapping();
        List<ExceptionMapper> mappers = factory.createMappersFromController(controller,
                locator, handlerResolverFactory);
        assertEquals(1, mappers.size());
        assertNotNull(mappers.get(0).mapTo(NullPointerException.class));
        assertNotNull(mappers.get(0).mapTo(IllegalArgumentException.class));
        assertNull(mappers.get(0).mapTo(RuntimeException.class));
    }

    @Test
    void testCreateMappersFromControllerAdvice() {
        final HandlerResolverFactoryImpl handlerResolverFactory = newHandlerResolverFactory();
        final HandlerLocator locator = newHandlerLocator();
        final SpringMvcExceptionResolverFactory factory = newExceptionResolverFactory(
                locator, handlerResolverFactory);

        final ControllerAdviceExceptionMapping controller = new ControllerAdviceExceptionMapping();
        List<ExceptionMapper> mappers = factory.createMappersFromControllerAdvice(controller,
                false, locator, handlerResolverFactory);
        assertEquals(1, mappers.size());
        assertNotNull(mappers.get(0).mapTo(NullPointerException.class));
        assertNotNull(mappers.get(0).mapTo(IllegalArgumentException.class));
        assertNull(mappers.get(0).mapTo(RuntimeException.class));
    }

    private static HandlerResolverFactoryImpl newHandlerResolverFactory() {
        return new HandlerResolverFactoryImpl(Collections
                .singleton(new FastJsonHttpBodySerializer()),
                Collections.singleton(new FastJsonHttpBodySerializer()),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.singleton(new ReturnValueResolverAdapter() {
                    @Override
                    public byte[] resolve(Object returnValue, AsyncRequest request, AsyncResponse response) {
                        return new byte[0];
                    }

                    @Override
                    public boolean supports(InvocableMethod invocableMethod) {
                        return true;
                    }
                }),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList());
    }

    private static HandlerLocator newHandlerLocator() {
        return new AbstractRouteHandlerLocator(Schedulers.BIZ,
                (handler) -> new HandlerAdvice[0]) {
            @Override
            protected HttpResponseStatus getCustomResponse(InvocableMethod handlerMethod) {
                return null;
            }
        };
    }

    private static SpringMvcExceptionResolverFactory newExceptionResolverFactory(HandlerLocator locator,
                                                                                 HandlerResolverFactory factory) {
        return new SpringMvcExceptionResolverFactory(Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                locator, factory);
    }

    // *****        exception handlers defined in controller    *****

    @RestController
    private static class ControllerExceptionMapping {

        @ExceptionHandler
        public void method1(AsyncResponse response, IllegalArgumentException ex) {
            response.setHeader(NAME, "IllegalArgumentException");
        }

        @ExceptionHandler(NullPointerException.class)
        public void method2(AsyncResponse response) {
            response.setHeader(NAME, "NullPointerException");
        }
    }

    // *****        exception handlers defined in controller advice    *****

    @ControllerAdvice
    private static class ControllerAdviceExceptionMapping {

        @ExceptionHandler
        public void method1(AsyncResponse response, IllegalArgumentException ex) {
            response.setHeader(NAME, "IllegalArgumentException");
        }

        @ExceptionHandler(NullPointerException.class)
        public void method2(AsyncResponse response) {
            response.setHeader(NAME, "NullPointerException");
        }
    }

    // *****        exception handlers defined in rest controller advice    *****

    @RestControllerAdvice
    private static class RestControllerAdviceExceptionMapping {

        @ExceptionHandler
        public void method1(AsyncResponse response, IllegalArgumentException ex) {
            response.setHeader(NAME, "IllegalArgumentException");
        }

        @ExceptionHandler(NullPointerException.class)
        public void method2(AsyncResponse response) {
            response.setHeader(NAME, "NullPointerException");
        }
    }

    // *****        duplicate handlers(defined in controller) mapping to same exception    *****

    @RestController
    private static class DuplicatedException0 {
        @ExceptionHandler
        public void method1(NullPointerException ex) {
        }

        @ExceptionHandler(NullPointerException.class)
        public void method2(NullPointerException ex) {
        }
    }

    // *****        duplicate handlers(defined in controller advice) mapping to same exception    *****

    @RestControllerAdvice
    private static class DuplicatedException1 {
        @ExceptionHandler
        public void method1(NullPointerException ex) {
        }

        @ExceptionHandler(NullPointerException.class)
        public void method2(NullPointerException ex) {
        }
    }

    // *****        no handlers(defined in controller) mapping to any exception    *****

    @RestController
    private static class NoExceptionMapping0 {
        @ExceptionHandler
        public void method() {

        }
    }

    // *****        no handlers(defined in controller advice) mapping to any exception    *****

    @RestControllerAdvice
    private static class NoExceptionMapping1 {

        @ExceptionHandler
        public void method() {

        }
    }

}
