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
package io.esastack.restlight.jaxrs.resolver.param;

import io.esastack.restlight.core.method.ConstructorParamImpl;
import io.esastack.restlight.core.method.FieldParamImpl;
import io.esastack.restlight.core.method.MethodParamImpl;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.jaxrs.resolver.ResolverUtils;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.mock.MockHttpResponse;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpHeadersParamResolverTest {

    @Test
    void testAll() throws Throwable {
        final HttpHeadersParamResolver factory = new HttpHeadersParamResolver();
        assertTrue(factory.supports(new FieldParamImpl(Subject.class.getDeclaredField("headers"))));
        assertTrue(factory.supports(new ConstructorParamImpl(Subject.class.getConstructor(HttpHeaders.class),
                0)));
        assertTrue(factory.supports(new MethodParamImpl(Subject.class.getDeclaredMethod("headersValue",
                HttpHeaders.class), 0)));
        assertTrue(factory.supports(new MethodParamImpl(Subject.class.getDeclaredMethod("setHeaders",
                HttpHeaders.class), 0)));
        assertFalse(factory.supports(new MethodParamImpl(Subject.class.getDeclaredMethod("headersValue0",
                HttpHeaders.class), 0)));

        final Param param = new FieldParamImpl(
                Subject.class.getDeclaredField("headers"));
        ParamResolver resolver = factory.createResolver(param, ResolverUtils.defaultConverterFunc(), null);
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext context = new RequestContextImpl(request, response);
        request.headers().add("name0", "value0");
        request.headers().add("name1", "value1");

        final HttpHeaders resolved = (HttpHeaders) resolver.resolve(context);
        assertEquals(2, resolved.getRequestHeaders().size());
        assertEquals("value0", resolved.getHeaderString("name0"));
        assertEquals("value1", resolved.getHeaderString("name1"));
    }

    private static class Subject {

        @Context
        private HttpHeaders headers;

        public Subject(@Context HttpHeaders headers) {
        }

        public void headersValue(@Context HttpHeaders headers) {
        }

        public void headersValue0(HttpHeaders headers) {
        }

        @Context
        public void setHeaders(HttpHeaders headers) {

        }
    }
}

