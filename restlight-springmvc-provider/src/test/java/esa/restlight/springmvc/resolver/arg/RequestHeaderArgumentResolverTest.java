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
import esa.restlight.core.serialize.FastJsonHttpBodySerializer;
import esa.restlight.server.bootstrap.WebServerException;
import esa.restlight.springmvc.ResolverUtils;
import esa.restlight.springmvc.annotation.shaded.RequestHeader0;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.handler.codec.http.HttpHeaders;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class RequestHeaderArgumentResolverTest {

    private static RequestHeaderArgumentResolver resolverFactory = new RequestHeaderArgumentResolver();

    private static final Subject SUBJECT = new Subject();

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        assumeTrue(RequestHeader0.shadedClass().getName().startsWith("org.springframework"));
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testNormal() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader("foo", "bar")
                .build();
        final Object resolved = createResolverAndResolve(request, "requestHeader");
        assertEquals("bar", resolved);
    }

    @Test
    void testHttpHeaders() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader("foo", "bar")
                .build();
        final Object resolved = createResolverAndResolve(request, "requestHeaders");
        assertTrue(HttpHeaders.class.isAssignableFrom(resolved.getClass()));
        assertEquals("bar", ((HttpHeaders) resolved).get("foo"));
    }


    @Test
    void testNamedHeader() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader("baz", "qux")
                .build();
        final Object resolved = createResolverAndResolve(request, "requestHeaderName");
        assertEquals("qux", resolved);
    }

    @Test
    void testRequiredHeader() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        assertThrows(WebServerException.class, () -> createResolverAndResolve(request, "requestHeader"));
    }

    @Test
    void testNoneRequiredHeader() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final Object resolved = createResolverAndResolve(request, "noneRequiredHeader");
        assertNull(resolved);
    }

    @Test
    void testDefaultHeader() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultHeader");

        assertEquals("foo", resolved);
    }

    @Test
    void testDefaultAndRequiredHeader() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultAndRequiredHeader");
        assertEquals("foo", resolved);
    }

    @Test
    void testDefaultAndRequiredHeader1() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader("foo", "bar")
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultAndRequiredHeader");
        assertEquals("bar", resolved);
    }

    private static Object createResolverAndResolve(AsyncRequest request, String method) throws Exception {
        final MethodParam parameter = handlerMethods.get(method).parameters()[0];
        assertTrue(resolverFactory.supports(parameter));
        final ArgumentResolver resolver = resolverFactory.createResolver(parameter,
                Collections.singletonList(new FastJsonHttpBodySerializer()));
        return resolver.resolve(request, MockAsyncResponse.aMockResponse().build());
    }

    private static class Subject {
        public void requestHeader(@RequestHeader String foo) {
        }

        public void requestHeaders(@RequestHeader HttpHeaders foo) {
        }

        public void requestHeaderName(@RequestHeader(name = "baz") String foo) {
        }

        public void noneRequiredHeader(@RequestHeader(required = false) String foo) {
        }

        public void defaultHeader(@RequestHeader(required = false, defaultValue = "foo") String foo) {
        }

        public void defaultAndRequiredHeader(@RequestHeader(defaultValue = "foo") String foo) {
        }

    }
}
