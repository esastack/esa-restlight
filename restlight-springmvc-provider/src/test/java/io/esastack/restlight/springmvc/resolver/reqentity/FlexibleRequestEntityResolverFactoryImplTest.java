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
package io.esastack.restlight.springmvc.resolver.reqentity;

import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.MethodParam;
import io.esastack.restlight.core.resolver.RequestEntity;
import io.esastack.restlight.core.resolver.RequestEntityImpl;
import io.esastack.restlight.core.resolver.RequestEntityResolver;
import io.esastack.restlight.core.resolver.reqentity.FlexibleRequestEntityResolverFactory;
import io.esastack.restlight.core.serialize.GsonHttpBodySerializer;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.serialize.JacksonHttpBodySerializer;
import io.esastack.restlight.core.serialize.JacksonSerializer;
import io.esastack.restlight.server.bootstrap.WebServerException;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.mock.MockHttpResponse;
import io.esastack.restlight.springmvc.annotation.shaded.RequestBody0;
import io.esastack.restlight.springmvc.resolver.Pojo;
import io.esastack.restlight.springmvc.resolver.ResolverUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class FlexibleRequestEntityResolverFactoryImplTest {

    private static final FlexibleRequestEntityResolverFactory resolverFactory
            = new FlexibleRequestEntityResolverFactoryImpl(false, null);
    private static final Subject SUBJECT = new Subject();
    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        assumeTrue(RequestBody0.shadedClass().getName().startsWith("org.springframework"));
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testSupportIfAnnotationAbsent() throws Exception {
        final Pojo origin = new Pojo(1024, "hello restlight");
        final HttpRequest request = MockHttpRequest
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
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .withBody(JacksonSerializer.getDefaultMapper().writeValueAsBytes(origin))
                .build();
        final Object resolved = createResolverAndResolve(request, "requestBody");
        assertEquals(origin, resolved);
    }

    @Test
    void testRequired() {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .build();
        assertThrows(WebServerException.class, () -> createResolverAndResolve(request, "requestBody"));
    }

    @Test
    void testRequestBodyText() throws Exception {
        final Pojo origin = new Pojo(1024, "hello restlight");
        final HttpRequest request = MockHttpRequest
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
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withBody(JacksonSerializer.getDefaultMapper().writeValueAsBytes(origin))
                .build();
        final Object resolved = createResolverAndResolve(request, "requestBodyText");
        assertEquals(JacksonSerializer.getDefaultMapper().writeValueAsString(origin), resolved);
    }

    @Test
    void testNoneRequiredRequestBody() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .build();
        final Object resolved = createResolverAndResolve(request, "noneRequiredRequestBody");
        assertNull(resolved);
    }

    @Test
    void testUnsupported() {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.MULTIPART_FORM_DATA.value())
                .build();
        assertThrows(WebServerException.class, () -> createResolverAndResolve(request, "requestBodyText"));
    }

    @Test
    void testMultiSerializer() throws Exception {
        final MethodParam param = handlerMethods.get("requestBody").parameters()[0];
        List<HttpRequestSerializer> serializers = Arrays.asList(new GsonHttpBodySerializer() {
            @Override
            protected boolean supportsRead(RequestEntity entity) {
                return entity.mediaType().isCompatibleWith(MediaType.APPLICATION_XML);
            }
        }, new JacksonHttpBodySerializer());
        RequestEntityResolver resolver =
                new FlexibleRequestEntityResolverFactoryImpl(false, null)
                .createResolver(param, ResolverUtils.defaultConverterFunc(), serializers);

        final Pojo origin = new Pojo(1024, "hello restlight");
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .withBody(JacksonSerializer.getDefaultMapper().writeValueAsBytes(origin))
                .build();
        final RequestContext context = new RequestContextImpl(request,
                MockHttpResponse.aMockResponse().build());
        final Object resolvedWithJson = resolver.readFrom(new RequestEntityImpl(param, context), context).value();
        assertEquals(origin, resolvedWithJson);

        final HttpRequest request2 = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_XML.value())
                .withBody(JacksonSerializer.getDefaultMapper().writeValueAsBytes(origin))
                .build();
        final RequestContext context2 = new RequestContextImpl(request2,
                MockHttpResponse.aMockResponse().build());
        final Object resolvedWithXml = resolver.readFrom(new RequestEntityImpl(param, context2), context2).value();
        assertEquals(origin, resolvedWithXml);
    }

    private static Object createResolverAndResolve(HttpRequest request, String method) throws Exception {
        final MethodParam param = handlerMethods.get(method).parameters()[0];
        assertTrue(resolverFactory.supports(param));
        final RequestEntityResolver resolver = resolverFactory.createResolver(param,
                ResolverUtils.defaultConverterFunc(),
                Collections.singletonList(new JacksonHttpBodySerializer()));

        final RequestContext context = new RequestContextImpl(request,
                MockHttpResponse.aMockResponse().build());
        return resolver.readFrom(new RequestEntityImpl(param, context), context).value();
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

