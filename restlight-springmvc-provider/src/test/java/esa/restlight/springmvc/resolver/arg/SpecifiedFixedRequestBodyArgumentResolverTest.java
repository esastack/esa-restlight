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
package esa.restlight.springmvc.resolver.arg;

import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.annotation.RequestSerializer;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.MethodParam;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.core.serialize.HttpRequestSerializer;
import esa.restlight.core.serialize.JacksonHttpBodySerializer;
import esa.restlight.core.serialize.JacksonSerializer;
import esa.restlight.core.util.MediaType;
import esa.restlight.springmvc.ResolverUtils;
import esa.restlight.springmvc.annotation.shaded.RequestBody0;
import esa.restlight.springmvc.resolver.Pojo;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SpecifiedFixedRequestBodyArgumentResolverTest {

    private static SpecifiedFixedRequestBodyArgumentResolver resolverFactory;

    private static final Subject SUBJECT = new Subject();

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        assumeTrue(RequestBody0.shadedClass().getName().startsWith("org.springframework"));
        resolverFactory = new SpecifiedFixedRequestBodyArgumentResolver();
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testSupport() {
        final MethodParam absent = handlerMethods.get("requestBody").parameters()[0];
        assertFalse(resolverFactory.supports(absent));

        final MethodParam illegal = handlerMethods.get("illegalSpecifiedRequestBody").parameters()[0];
        assertFalse(resolverFactory.supports(illegal));

        final MethodParam support = handlerMethods.get("jacksonSpecifiedRequestBody").parameters()[0];
        assertTrue(resolverFactory.supports(support));

        final MethodParam supportOnMethod =
                handlerMethods.get("jacksonSpecifiedOnMethodRequestBody").parameters()[0];
        assertTrue(resolverFactory.supports(supportOnMethod));

        final MethodParam supportWithSerializerAnnotation =
                handlerMethods.get("jacksonSerializerSpecified").parameters()[0];
        assertTrue(resolverFactory.supports(supportWithSerializerAnnotation));
    }

    @Test
    void testSpecified() throws Exception {
        final Pojo origin = new Pojo(1024, "hell restlight");
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .withBody(JacksonSerializer.getDefaultMapper().writeValueAsBytes(origin))
                .build();
        final Object resolved = createResolverAndResolve(request, "jacksonSpecifiedRequestBody");
        assertEquals(origin, resolved);
    }

    private static Object createResolverAndResolve(AsyncRequest request, String method) throws Exception {
        final MethodParam parameter = handlerMethods.get(method).parameters()[0];
        assertTrue(resolverFactory.supports(parameter));
        final ArgumentResolver resolver = resolverFactory.createResolver(parameter,
                Collections.singletonList(new JacksonHttpBodySerializer()));
        return resolver.resolve(request, MockAsyncResponse.aMockResponse().build());
    }

    private static class Subject {
        public void requestBody(@RequestBody Pojo pojo) {
        }

        public void illegalSpecifiedRequestBody(@RequestBody
                                                @RequestSerializer(HttpRequestSerializer.class) Pojo pojo) {
        }

        public void jacksonSpecifiedRequestBody(@RequestBody
                                                @RequestSerializer(JacksonHttpBodySerializer.class) Pojo pojo) {
        }

        @RequestSerializer(JacksonHttpBodySerializer.class)
        @ResponseBody
        public void jacksonSpecifiedOnMethodRequestBody(@RequestBody Pojo pojo) {
        }

        @esa.restlight.core.annotation.Serializer(JacksonHttpBodySerializer.class)
        @ResponseBody
        public Pojo jacksonSerializerSpecified(@RequestBody Pojo pojo) {
            return new Pojo(11, "foo");
        }
    }

}
