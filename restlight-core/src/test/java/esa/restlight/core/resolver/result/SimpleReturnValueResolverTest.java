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
package esa.restlight.core.resolver.result;

import esa.commons.ClassUtils;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.resolver.ReturnValueResolver;
import esa.restlight.core.serialize.FastJsonHttpBodySerializer;
import esa.restlight.core.serialize.Serializers;
import esa.restlight.core.util.MediaType;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DefaultReturnValueResolverTest
 */
class SimpleReturnValueResolverTest {

    private static SimpleReturnValueResolver factory = new SimpleReturnValueResolver();

    private static final Subject SUBJECT = new Subject();

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        handlerMethods = ClassUtils.userDeclaredMethods(SUBJECT.getClass())
                .stream()
                .map(method -> HandlerMethod.of(method, SUBJECT))
                .collect(Collectors.toMap(h -> h.method().getName(), hm -> hm));
    }

    @Test
    void testString() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final AsyncResponse response = MockAsyncResponse
                .aMockResponse()
                .build();
        final String ret = "foo";
        byte[] serialized = createResolverAndResolve(ret, request, response, "str");
        assertArrayEquals(ret.getBytes(), serialized);
        assertEquals(MediaType.APPLICATION_OCTET_STREAM.value(), response.getHeader(HttpHeaderNames.CONTENT_TYPE));
    }

    @Test
    void testObject() {
        final InvocableMethod invocableMethod = handlerMethods.get("obj");
        assertFalse(factory.supports(invocableMethod));
    }

    @Test
    void testByte() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final AsyncResponse response = MockAsyncResponse
                .aMockResponse()
                .build();
        final byte[] ret = new byte[]{1, 2, 3};
        final byte[] serialized = createResolverAndResolve(ret, request, response, "byteArray");
        assertArrayEquals(ret, serialized);
        assertEquals(MediaType.APPLICATION_OCTET_STREAM.value(), response.getHeader(HttpHeaderNames.CONTENT_TYPE));
    }

    @Test
    void testByteBuf() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final AsyncResponse response = MockAsyncResponse
                .aMockResponse()
                .build();
        final ByteBuf ret = Unpooled.EMPTY_BUFFER;
        final byte[] serialized = createResolverAndResolve(ret, request, response, "byteBuf");
        assertArrayEquals(Serializers.alreadyWrite(), serialized);
        assertEquals(MediaType.APPLICATION_OCTET_STREAM.value(), response.getHeader(HttpHeaderNames.CONTENT_TYPE));
    }

    @Test
    void testPrimitive() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final AsyncResponse response = MockAsyncResponse
                .aMockResponse()
                .build();
        final int ret = 1;
        final byte[] serialized = createResolverAndResolve(ret, request, response, "primitive");
        assertArrayEquals("1".getBytes(StandardCharsets.UTF_8), serialized);
        assertEquals(MediaType.APPLICATION_OCTET_STREAM.value(), response.getHeader(HttpHeaderNames.CONTENT_TYPE));
    }

    private static byte[] createResolverAndResolve(Object returnValue, AsyncRequest request,
                                                   AsyncResponse response,
                                                   String method) throws Exception {
        final InvocableMethod invocableMethod = handlerMethods.get(method);
        assertTrue(factory.supports(invocableMethod));
        final ReturnValueResolver resolver = factory.createResolver(invocableMethod,
                Collections.singletonList(new FastJsonHttpBodySerializer()));
        return resolver.resolve(returnValue, request, response);
    }

    private static class Subject {

        public Object obj() {
            return null;
        }

        public String str() {
            return null;
        }

        public byte[] byteArray() {
            return null;
        }

        public ByteBuf byteBuf() {
            return null;
        }

        public int primitive() {
            return 1;
        }

    }
}
