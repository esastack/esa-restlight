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
package esa.restlight.core.resolver.arg;

import esa.commons.ClassUtils;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.annotation.RequestBean;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.MethodParam;
import esa.restlight.core.mock.MockContext;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.core.resolver.HandlerResolverFactoryImpl;
import esa.restlight.core.serialize.JacksonHttpBodySerializer;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RequestBeanArgumentResolverTest {

    private static RequestBeanArgumentResolver resolverFactory =
            new RequestBeanArgumentResolver(MockContext.withHandlerResolverFactory(
                    new HandlerResolverFactoryImpl(Collections.singletonList(new JacksonHttpBodySerializer()),
                            Collections.singletonList(new JacksonHttpBodySerializer()),
                            null,
                            Arrays.asList(new AsyncRequestArgumentResolverFactory(),
                                    new AsyncResponseArgumentResolverFactory()),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null)));

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        handlerMethods = ClassUtils.userDeclaredMethods(Subject.class)
                .stream()
                .map(method -> HandlerMethod.of(method, new Subject()))
                .collect(Collectors.toMap(h -> h.method().getName(), hm -> hm));
    }

    @Test
    void testSupportIfAnnotationPresent() {
        assertTrue(resolverFactory.supports(handlerMethods.get("method").parameters()[0]));
    }

    @Test
    void testFieldValueSetByReflection() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withParameter("foo", "1")
                .build();
        final Bean bean = (Bean) createResolverAndResolve(request, "method");
        assertEquals(0, bean.foo);
        assertEquals(request, bean.request);
        assertNotNull(bean.response);
    }

    private static Object createResolverAndResolve(AsyncRequest request, String method) throws Exception {
        final MethodParam parameter = handlerMethods.get(method).parameters()[0];
        assertTrue(resolverFactory.supports(parameter));
        final ArgumentResolver resolver = resolverFactory.createResolver(parameter, null);
        return resolver.resolve(request, MockAsyncResponse.aMockResponse().build());
    }

    private static class Subject {

        void method(@RequestBean Bean pojo) {

        }
    }

    private static class Bean {
        private int foo;
        private AsyncRequest request;
        private AsyncResponse response;
    }
}
