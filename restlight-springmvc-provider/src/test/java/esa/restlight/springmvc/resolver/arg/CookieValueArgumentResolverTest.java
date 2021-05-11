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
import esa.restlight.server.bootstrap.WebServerException;
import esa.restlight.springmvc.ResolverUtils;
import esa.restlight.springmvc.annotation.shaded.CookieValue0;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.CookieValue;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class CookieValueArgumentResolverTest {

    private static CookieValueArgumentResolver resolverFactory = new CookieValueArgumentResolver();

    private static final Subject SUBJECT = new Subject();

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        assumeTrue(CookieValue0.shadedClass().getName().startsWith("org.springframework"));
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testCookieValueResolve() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withCookie("foo", "bar")
                .build();
        final Object resolved = createResolverAndResolve(request, "cookieValue");
        assertEquals("bar", resolved);
    }

    @Test
    void testCookieObjectValueResolve() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withCookie("foo", "bar")
                .build();
        final Object resolved = createResolverAndResolve(request, "cookieObjectValue");
        assertEquals(new DefaultCookie("foo", "bar"), resolved);
    }

    @Test
    void testCookieObjectsValueResolve() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withCookie("foo", "bar")
                .build();
        final Object resolved = createResolverAndResolve(request, "cookieObjectsValue");
        assertTrue(Set.class.isAssignableFrom(resolved.getClass()));
        assertTrue(((Set<Cookie>) resolved).contains(new DefaultCookie("foo", "bar")));
    }

    @Test
    void testResolveNamedHeader() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withCookie("baz", "qux")
                .build();
        final Object resolved = createResolverAndResolve(request, "cookieValueName");
        assertEquals("qux", resolved);
    }

    @Test
    void testRequiredHeader() {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        assertThrows(WebServerException.class, () -> createResolverAndResolve(request, "cookieValue"));
    }

    @Test
    void testNoneRequiredHeader() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final Object resolved = createResolverAndResolve(request, "noneRequiredCookieValue");
        assertNull(resolved);
    }

    @Test
    void testDefaultHeader() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultCookieValue");
        assertEquals("foo", resolved);
    }

    @Test
    void testDefaultCollectionValue() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultCollectionValue");
        assertNotNull(resolved);
        assertTrue(((Collection) resolved).isEmpty());
    }

    @Test
    void testDefaultArrayValue() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultArrayValue");
        assertNotNull(resolved);
        assertEquals(0, ((String[]) resolved).length);
    }

    @Test
    void testDefaultAndRequiredHeader() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final Object resolved =
                createResolverAndResolve(request, "defaultAndRequiredCookieValue");
        assertEquals("foo", resolved);
    }

    @Test
    void testDefaultAndRequiredHeader1() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withCookie("foo", "bar")
                .build();
        final Object resolved =
                createResolverAndResolve(request, "defaultAndRequiredCookieValue");
        assertEquals("bar", resolved);
    }

    private static Object createResolverAndResolve(AsyncRequest request, String method) throws Exception {
        final MethodParam parameter = handlerMethods.get(method).parameters()[0];
        assertTrue(resolverFactory.supports(parameter));
        final ArgumentResolver resolver = resolverFactory.createResolver(parameter, null);
        return resolver.resolve(request, MockAsyncResponse.aMockResponse().build());
    }

    private static class Subject {
        public void cookieValue(@CookieValue String foo) {
        }

        public void cookieValueName(@CookieValue(name = "baz") String foo) {
        }

        public void noneRequiredCookieValue(@CookieValue(required = false) String foo) {
        }

        public void defaultCookieValue(@CookieValue(required = false, defaultValue = "foo") String foo) {
        }

        public void defaultAndRequiredCookieValue(@CookieValue(defaultValue = "foo") String foo) {
        }

        public void cookieObjectValue(@CookieValue Cookie foo) {
        }

        public void cookieObjectsValue(@CookieValue Set<Cookie> foo) {
        }

        public void defaultCollectionValue(@CookieValue(value = "foo", defaultValue = "") Collection<String> foo) {
        }

        public void defaultArrayValue(@CookieValue(value = "foo", defaultValue = "") String[] foo) {
        }

    }

}
