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
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.MethodParam;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.core.serialize.GsonHttpBodySerializer;
import esa.restlight.core.serialize.HttpRequestSerializer;
import esa.restlight.core.serialize.JacksonHttpBodySerializer;
import esa.restlight.core.serialize.JacksonSerializer;
import esa.restlight.core.util.MediaType;
import esa.restlight.server.bootstrap.WebServerException;
import esa.restlight.springmvc.ResolverUtils;
import esa.restlight.springmvc.annotation.shaded.RequestBody0;
import esa.restlight.springmvc.resolver.Pojo;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class RequestBodyArgumentResolverTest {

    private static RequestBodyArgumentResolver resolverFactory;

    private static final Subject SUBJECT = new Subject();

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        assumeTrue(RequestBody0.shadedClass().getName().startsWith("org.springframework"));
        resolverFactory = new RequestBodyArgumentResolver();
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testSupportIfAnnotationAbsent() throws Exception {
        final Pojo origin = new Pojo(1024, "hello restlight");
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .withBody(JacksonSerializer.getDefaultMapper().writeValueAsBytes(origin))
                .build();
        final Object resolved = createResolverAndResolve(request, "none");
        assertEquals(origin, resolved);
    }

    @Test
    void testRequestBody() throws Exception {
        final Pojo origin = new Pojo(1024, "hello restlight");
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .withBody(JacksonSerializer.getDefaultMapper().writeValueAsBytes(origin))
                .build();
        final Object resolved = createResolverAndResolve(request, "requestBody");
        assertEquals(origin, resolved);
    }

    @Test
    void testRequired() {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .build();
        assertThrows(WebServerException.class, () -> createResolverAndResolve(request, "requestBody"));
    }

    @Test
    void testRequestBodyText() throws Exception {
        final Pojo origin = new Pojo(1024, "hello restlight");
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.TEXT_PLAIN.value())
                .withBody(JacksonSerializer.getDefaultMapper().writeValueAsBytes(origin))
                .build();
        final Object resolved = createResolverAndResolve(request, "requestBodyText");
        assertEquals(JacksonSerializer.getDefaultMapper().writeValueAsString(origin), resolved);
    }

    @Test
    void testNoneContentType() throws Exception {
        final Pojo origin = new Pojo(1024, "hello restlight");
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withBody(JacksonSerializer.getDefaultMapper().writeValueAsBytes(origin))
                .build();
        final Object resolved = createResolverAndResolve(request, "requestBodyText");
        assertEquals(JacksonSerializer.getDefaultMapper().writeValueAsString(origin), resolved);
    }

    @Test
    void testNoneRequiredRequestBody() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .build();
        final Object resolved = createResolverAndResolve(request, "noneRequiredRequestBody");
        assertNull(resolved);
    }

    @Test
    void testUnsupported() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.MULTIPART_FORM_DATA.value())
                .build();
        assertThrows(WebServerException.class, () -> createResolverAndResolve(request, "requestBodyText"));
    }

    @Test
    void testMultiSerializer() throws Exception {

        final MethodParam parameter = handlerMethods.get("requestBody").parameters()[0];

        List<HttpRequestSerializer> serializers = Arrays.asList(new GsonHttpBodySerializer() {
            @Override
            public boolean supportsRead(MediaType mediaType, Type type) {
                return mediaType.isCompatibleWith(MediaType.APPLICATION_XML);
            }
        }, new JacksonHttpBodySerializer());
        ArgumentResolver resolver = new RequestBodyArgumentResolver()
                .createResolver(parameter, serializers);

        final Pojo origin = new Pojo(1024, "hello restlight");
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .withBody(JacksonSerializer.getDefaultMapper().writeValueAsBytes(origin))
                .build();
        final Object resolvedWithJson = resolver.resolve(request, MockAsyncResponse.aMockResponse().build());
        assertEquals(origin, resolvedWithJson);
        final AsyncRequest request2 = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_XML.value())
                .withBody(JacksonSerializer.getDefaultMapper().writeValueAsBytes(origin))
                .build();
        final Object resolvedWithXml = resolver.resolve(request2, MockAsyncResponse.aMockResponse().build());

        assertEquals(origin, resolvedWithXml);
    }

    private static Object createResolverAndResolve(AsyncRequest request, String method) throws Exception {
        final MethodParam parameter = handlerMethods.get(method).parameters()[0];
        assertTrue(resolverFactory.supports(parameter));
        final ArgumentResolver resolver = resolverFactory.createResolver(parameter,
                Collections.singletonList(new JacksonHttpBodySerializer()));
        return resolver.resolve(request, MockAsyncResponse.aMockResponse().build());
    }

    private static class Subject {
        public void none(@RequestBody Pojo pojo) {
        }

        public void requestBody(@RequestBody Pojo pojo) {
        }

        public void requestBodyText(@RequestBody String pojoStr) {
        }

        public void noneRequiredRequestBody(@RequestBody(required = false) Pojo pojo) {
        }
    }

}
