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
package io.esastack.restlight.springmvc.resolver.rspentity;

import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.ResponseEntityResolver;
import io.esastack.restlight.core.serialize.FastJsonHttpBodySerializer;
import io.esastack.restlight.core.serialize.JacksonHttpBodySerializer;
import io.esastack.restlight.core.serialize.JacksonSerializer;
import io.esastack.restlight.core.serialize.ProtoBufHttpBodySerializer;
import io.esastack.restlight.core.serialize.Serializers;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.mock.MockHttpResponse;
import io.esastack.restlight.springmvc.annotation.shaded.ResponseBody0;
import io.esastack.restlight.springmvc.resolver.Pojo;
import io.esastack.restlight.springmvc.resolver.ResolverUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class NegotiationResponseResolverFactoryTest {

    private static final NegotiationResponseResolverFactory resolverFactory =
            new NegotiationResponseResolverFactory(null);
    private static final Subject SUBJECT = new Subject();
    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        assumeTrue(ResponseBody0.shadedClass().getName().startsWith("org.springframework"));
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testResolve() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();

        final Pojo pojo = new Pojo(1024, "foo");
        final byte[] resolved = createResolverAndResolve(pojo, request, response, "responseBodyPojo");
        assertArrayEquals(JacksonSerializer.getDefaultMapper().writeValueAsBytes(pojo), resolved);
    }

    @Test
    void testJsonFormatParameterInUrl() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withParameter("format", "json")
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();

        final Pojo pojo = new Pojo(1024, "foo");
        final byte[] resolved = createMultiResolverAndResolve(pojo, request, response, "responseBodyPojo");
        assertArrayEquals(JacksonSerializer.getDefaultMapper().writeValueAsBytes(pojo), resolved);
    }

    @Test
    void testPbParameterInUrl() {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withParameter("format", "pb")
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();

        final Pojo pojo = new Pojo(1024, "foo");
        assertThrows(UnsupportedOperationException.class,
                () -> createMultiResolverAndResolve(pojo, request, response, "responseBodyPojo"));
    }

    @Test
    void testResolveDetectableStringType() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();

        final String foo = "foo";
        final byte[] resolved = createResolverAndResolve(foo, request, response, "str");
        assertArrayEquals(foo.getBytes(StandardCharsets.UTF_8), resolved);
    }

    @Test
    void testResolveDetectableByteArrayType() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();

        final byte[] foo = "foo".getBytes(StandardCharsets.UTF_8);
        final byte[] resolved = createResolverAndResolve(foo, request, response, "byteArray");
        assertArrayEquals(foo, resolved);
    }

    @Test
    void testResolveDetectableByteBufType() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();

        final ByteBuf foo = Unpooled.copiedBuffer("foo".getBytes(StandardCharsets.UTF_8));
        final byte[] resolved = createResolverAndResolve(foo, request, response, "byteBuf");
        assertArrayEquals(Serializers.ALREADY_WRITE, resolved);
    }

    @Test
    void testResolveDetectablePrimitiveType() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();

        final int foo = 1;
        final byte[] resolved = createResolverAndResolve(foo, request, response, "byteBuf");
        assertArrayEquals(String.valueOf(foo).getBytes(StandardCharsets.UTF_8), resolved);
    }

    private static byte[] createResolverAndResolve(Object returnValue,
                                                   HttpRequest request,
                                                   HttpResponse response,
                                                   String method) throws Exception {
        final HandlerMethod handlerMethod = handlerMethods.get(method);
        final ResponseEntityResolver resolver = resolverFactory.createResolver(
                Collections.singletonList(new FastJsonHttpBodySerializer()));
        return ResolverUtils.writtenContent(request, response, returnValue, handlerMethod, resolver);
    }

    private static byte[] createMultiResolverAndResolve(Object returnValue,
                                                        HttpRequest request,
                                                        HttpResponse response,
                                                        String method) throws Exception {
        final HandlerMethod handlerMethod = handlerMethods.get(method);
        final ResponseEntityResolver resolver = resolverFactory.createResolver(
                Arrays.asList(new JacksonHttpBodySerializer() {
                    @Override
                    public int getOrder() {
                        return LOWEST_PRECEDENCE;
                    }
                }, new ProtoBufHttpBodySerializer() {
                    @Override
                    public int getOrder() {
                        return HIGHEST_PRECEDENCE;
                    }
                }));
        return ResolverUtils.writtenContent(request, response, returnValue, handlerMethod, resolver);
    }

    private static class Subject {

        public void none() {
        }

        @ResponseBody
        public void responseBody() {
        }

        @ResponseBody
        public Pojo responseBodyPojo() {
            return null;
        }

        @ResponseBody
        public Object str() {
            return null;
        }

        @ResponseBody
        public Object byteArray() {
            return null;
        }

        @ResponseBody
        public Object byteBuf() {
            return null;
        }

        @ResponseBody
        public Object primitive() {
            return null;
        }
    }

}

