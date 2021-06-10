/*
 * Copyright 2021 OPPO ESA Stack Project
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
package esa.restlight.core.resolver.exception;

import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.handler.locate.HandlerLocator;
import esa.restlight.core.resolver.HandlerResolverFactory;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AbstractExceptionResolverFactoryTest {

    @Test
    void testConstructor() {
        final HandlerResolverFactory factory = mock(HandlerResolverFactory.class);
        final HandlerLocator locator = mock(HandlerLocator.class);

        final AbstractExceptionResolverFactory resolver = new ExceptionResolverFactoryImpl(Collections.emptyList(),
                Collections.singletonList(new Controller()),
                Collections.singletonList(new ControllerAdvice()),
                locator,
                factory);
        assertEquals(2, resolver.mappers.size());
    }

    private static class Controller {

        public void method1(AsyncResponse response, IllegalArgumentException ex) {
            response.sendResult(200);
        }
    }

    private static class ControllerAdvice {

        public void method1(AsyncResponse response, IllegalArgumentException ex) {
            response.sendResult(200);
        }

    }

    private static class ExceptionResolverFactoryImpl extends AbstractExceptionResolverFactory {

        private ExceptionResolverFactoryImpl(List<ExceptionMapper> mappers,
                                            Collection<?> controllerBeans,
                                            Collection<?> adviceBeans,
                                            HandlerLocator locator,
                                            HandlerResolverFactory factory) {
            super(mappers, controllerBeans, adviceBeans, locator, factory);
        }

        @Override
        protected List<ExceptionMapper> createMappersFromController(Object bean,
                                                                    HandlerLocator locator,
                                                                    HandlerResolverFactory factory) {
            return Collections.singletonList(new DefaultExceptionMapper(Collections.singletonMap(
                    Throwable.class, (req, rsp, th) -> null)));
        }

        @Override
        protected List<ExceptionMapper> createMappersFromControllerAdvice(Object adviceBean,
                                                                          boolean isController,
                                                                          HandlerLocator locator,
                                                                          HandlerResolverFactory factory) {
            return Collections.singletonList(new DefaultExceptionMapper(Collections.singletonMap(
                    Throwable.class, (req, rsp, th) -> null)));
        }
    }
}

