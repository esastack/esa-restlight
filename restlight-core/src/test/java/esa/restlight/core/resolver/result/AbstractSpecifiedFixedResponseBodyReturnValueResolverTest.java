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
package esa.restlight.core.resolver.result;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.httpserver.core.HttpOutputStream;
import esa.restlight.core.annotation.ResponseSerializer;
import esa.restlight.core.annotation.Serializer;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.serialize.FastJsonHttpBodySerializer;
import esa.restlight.core.serialize.HttpBodySerializer;
import esa.restlight.core.serialize.HttpResponseSerializer;
import esa.restlight.core.serialize.ProtoBufHttpBodySerializer;
import esa.restlight.core.util.MediaType;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractSpecifiedFixedResponseBodyReturnValueResolverTest {

    @Test
    void testSupports() throws Throwable {
        final AbstractSpecifiedFixedResponseBodyReturnValueResolver resolver =
                new SpecifiedFixedResponseBodyReturnValueResolver();

        final Object obj1 = new M1();
        assertTrue(resolver.supports(HandlerMethod.of(M1.class.getDeclaredMethod("k1"), obj1)));
        assertTrue(resolver.supports(HandlerMethod.of(M1.class.getDeclaredMethod("k2"), obj1)));
        assertThrows(IllegalArgumentException.class,
                () -> resolver.supports(HandlerMethod.of(M1.class.getDeclaredMethod("k3"), obj1)));


        final Object obj2 = new N1();
        assertTrue(resolver.supports(HandlerMethod.of(N1.class.getDeclaredMethod("k1"), obj2)));
        assertTrue(resolver.supports(HandlerMethod.of(N1.class.getDeclaredMethod("k2"), obj2)));
        assertThrows(IllegalArgumentException.class,
                () -> resolver.supports(HandlerMethod.of(N1.class.getDeclaredMethod("k3"), obj2)));
    }

    @Test
    void testCreateResolver() throws Throwable {
        final AbstractSpecifiedFixedResponseBodyReturnValueResolver resolver =
                new SpecifiedFixedResponseBodyReturnValueResolver();

        final Object obj1 = new M1();
        assertTrue(resolver.createResolver(HandlerMethod.of(M1.class.getDeclaredMethod("k1"), obj1),
                Collections.singletonList(new ProtoBufHttpBodySerializer()))
                instanceof AbstractDetectableReturnValueResolver);

        assertThrows(IllegalArgumentException.class, () -> resolver.createResolver(
                HandlerMethod.of(M1.class.getDeclaredMethod("k1"), obj1),
                Collections.singletonList(new FastJsonHttpBodySerializer())));

        assertThrows(IllegalArgumentException.class, () -> resolver.createResolver(
                HandlerMethod.of(M1.class.getDeclaredMethod("k1"), obj1),
                Collections.emptyList()));
    }

    @Test
    void testGetOrder() {
        assertEquals(20, new SpecifiedFixedResponseBodyReturnValueResolver().getOrder());
    }

    @Test
    void testResolve0() throws Throwable {
        final AbstractSpecifiedFixedResponseBodyReturnValueResolver resolver =
                new SpecifiedFixedResponseBodyReturnValueResolver();

        final Object obj1 = new M1();

        final AbstractDetectableReturnValueResolver resolver0 = (AbstractDetectableReturnValueResolver)
                resolver.createResolver(HandlerMethod.of(M1.class.getDeclaredMethod("k4"), obj1),
                Collections.singletonList(new HttpBodySerializerImpl()));
        assertTrue(resolver0.getMediaTypes(MockAsyncRequest.aMockRequest().build()).isEmpty());

        assertArrayEquals("Hello!".getBytes(StandardCharsets.UTF_8),
                resolver0.resolve0("xx", Collections.emptyList(), MockAsyncRequest.aMockRequest().build(),
                MockAsyncResponse.aMockResponse().build()));
    }

    @Test
    void testResolve0InResolver() {

    }

    private static class HttpBodySerializerImpl implements HttpResponseSerializer {
        @Override
        public boolean supportsWrite(MediaType mediaType, Type type) {
            return false;
        }

        @Override
        public Object customResponse(AsyncRequest request, AsyncResponse response, Object returnValue) {
            return null;
        }

        @Override
        public byte[] serialize(Object target) throws Exception {
            return "Hello!".getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public void serialize(Object target, HttpOutputStream outputStream) throws Exception {

        }

        @Override
        public boolean preferStream() {
            return false;
        }
    }

    @ResponseSerializer(value = ProtoBufHttpBodySerializer.class)
    private static class M1 {

        private Object k1() {
            return null;
        }

        @ResponseSerializer(value = FastJsonHttpBodySerializer.class)
        private Object k2() {
            return null;
        }

        @ResponseSerializer(value = HttpBodySerializer.class)
        private Object k3() {
            return null;
        }

        @ResponseSerializer(value = HttpBodySerializerImpl.class)
        private Object k4() {
            return null;
        }

    }

    @Serializer(value = ProtoBufHttpBodySerializer.class)
    private static class N1 {

        private Object k1() {
            return null;
        }

        @Serializer(value = FastJsonHttpBodySerializer.class)
        private Object k2() {
            return null;
        }

        @ResponseSerializer(value = HttpBodySerializer.class)
        private Object k3() {
            return null;
        }
    }

    private static class SpecifiedFixedResponseBodyReturnValueResolver
            extends AbstractSpecifiedFixedResponseBodyReturnValueResolver {

        private SpecifiedFixedResponseBodyReturnValueResolver() {
        }

        @Override
        protected boolean supports0(InvocableMethod invocableMethod) {
            return true;
        }
    }
}

