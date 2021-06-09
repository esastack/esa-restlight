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
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.serialize.HttpResponseSerializer;
import esa.restlight.core.serialize.ProtoBufHttpBodySerializer;
import esa.restlight.core.util.MediaType;
import esa.restlight.server.bootstrap.WebServerException;
import esa.restlight.server.util.Futures;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractResponseBodyReturnValueResolverTest {

    @Test
    void testCreateResolver() throws Throwable {
        final InvocableMethod method = HandlerMethod.of(AbstractResponseBodyReturnValueResolverTest.class
                .getDeclaredMethod("m1"), this);

        final AbstractResponseBodyReturnValueResolver resolver0 = new
                ResponseBodyReturnValueResolverImpl(true, "");
        assertTrue(resolver0.createResolver(method, Collections.emptyList())
                instanceof AbstractResponseBodyReturnValueResolver.NegotiationResolver);

        final AbstractResponseBodyReturnValueResolver resolver1 = new
                ResponseBodyReturnValueResolverImpl(false, "");
        assertTrue(resolver0.createResolver(method, Collections.emptyList())
                instanceof AbstractResponseBodyReturnValueResolver.DefaultResolver);

        assertEquals(10, resolver0.getOrder());
        assertEquals(10, resolver1.getOrder());
    }

    @Test
    void testResolve0OfDefaultResolver() throws Throwable {
        final AbstractResponseBodyReturnValueResolver.DefaultResolver resolver0 = new
                AbstractResponseBodyReturnValueResolver.DefaultResolver(Collections.emptyList(), true);
        assertThrows(WebServerException.class, () -> resolver0.resolve0("", Collections.emptyList(),
                MockAsyncRequest.aMockRequest().build(), MockAsyncResponse.aMockResponse().build()));

        final AbstractResponseBodyReturnValueResolver.DefaultResolver resolver1 = new
                AbstractResponseBodyReturnValueResolver.DefaultResolver(Collections.singletonList(
                        new HttpResponseSerializer2()), true);
        // media type is empty, use serializers.get(0) as default without matching
        assertArrayEquals("Hello World2!".getBytes(StandardCharsets.UTF_8),
                resolver1.resolve0("", Collections.emptyList(),
                MockAsyncRequest.aMockRequest().build(), MockAsyncResponse.aMockResponse().build()));

        // matching by #supports()
        final List<HttpResponseSerializer> serializers = new LinkedList<>();
        serializers.add(new HttpResponseSerializer2());
        serializers.add(new HttpResponseSerializer1());
        final AbstractResponseBodyReturnValueResolver.DefaultResolver resolver2 = new
                AbstractResponseBodyReturnValueResolver.DefaultResolver(serializers, true);
        assertArrayEquals("Hello World1!".getBytes(StandardCharsets.UTF_8),
                resolver2.resolve0("", Collections.singletonList(MediaType.ALL),
                        MockAsyncRequest.aMockRequest().build(), MockAsyncResponse.aMockResponse().build()));

        // have not matched serializer but to use serializers.get(0) as default.
        final AbstractResponseBodyReturnValueResolver.DefaultResolver resolver3 = new
                AbstractResponseBodyReturnValueResolver.DefaultResolver(
                        Collections.singletonList(new HttpResponseSerializer2()), true);
        assertArrayEquals("Hello World2!".getBytes(StandardCharsets.UTF_8),
                resolver3.resolve0("", Collections.singletonList(MediaType.ALL),
                        MockAsyncRequest.aMockRequest().build(), MockAsyncResponse.aMockResponse().build()));
    }

    @Test
    void testGetMediaTypesOfNegotiationResolver() {
        final String format = "format0";
        AbstractResponseBodyReturnValueResolver.NegotiationResolver resolver =
                new AbstractResponseBodyReturnValueResolver.NegotiationResolver(Collections.emptyList(),
                        format, true);

        final MockAsyncRequest request0 = MockAsyncRequest.aMockRequest().withParameter(format, "json").build();
        List<MediaType> types0 = resolver.getMediaTypes(request0);
        assertEquals(1, types0.size());
        assertEquals(MediaType.APPLICATION_JSON, types0.get(0));

        final MockAsyncRequest request1 = MockAsyncRequest.aMockRequest().withParameter(format, "pb").build();
        List<MediaType> types1 = resolver.getMediaTypes(request1);
        assertEquals(1, types1.size());
        assertEquals(ProtoBufHttpBodySerializer.PROTOBUF, types1.get(0));
    }

    private CompletionStage<Object> m1() {
        return Futures.completedFuture();
    }

    private static class HttpResponseSerializer1 implements HttpResponseSerializer {
        @Override
        public boolean supportsWrite(MediaType mediaType, Type type) {
            return true;
        }

        @Override
        public Object customResponse(AsyncRequest request, AsyncResponse response, Object returnValue) {
            return null;
        }

        @Override
        public byte[] serialize(Object target) throws Exception {
            return "Hello World1!".getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public void serialize(Object target, HttpOutputStream outputStream) throws Exception {
            // do nothing
        }

        @Override
        public boolean preferStream() {
            return false;
        }
    }

    private static class HttpResponseSerializer2 implements HttpResponseSerializer {
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
            return "Hello World2!".getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public void serialize(Object target, HttpOutputStream outputStream) throws Exception {
            // do nothing
        }

        @Override
        public boolean preferStream() {
            return false;
        }
    }

    private static class ResponseBodyReturnValueResolverImpl extends AbstractResponseBodyReturnValueResolver {

        private ResponseBodyReturnValueResolverImpl(boolean negotiation, String parameterName) {
            super(negotiation, parameterName);
        }

        @Override
        public boolean supports(InvocableMethod invocableMethod) {
            return true;
        }
    }
}

