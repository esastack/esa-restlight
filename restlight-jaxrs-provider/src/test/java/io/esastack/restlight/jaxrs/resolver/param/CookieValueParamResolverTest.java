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

import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.MethodParam;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.jaxrs.resolver.ResolverUtils;
import io.esastack.restlight.jaxrs.resolver.param.subject.ConstructorSubject;
import io.esastack.restlight.jaxrs.resolver.param.subject.FromStringSubject;
import io.esastack.restlight.jaxrs.resolver.param.subject.ValueOfSubject;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.mock.MockHttpResponse;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DefaultValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookieValueParamResolverTest {

    private static final CookieValueParamResolver resolverFactory = new CookieValueParamResolver();

    private static final Subject SUBJECT = new Subject();

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testCookieValueResolve() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withCookie("foo", "bar")
                .build();
        final Object resolved = createResolverAndResolve(request, "cookieValue");
        assertEquals("bar", resolved);
    }

    @Test
    void testEmptyDefaultValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .build();
        final Object resolved = createResolverAndResolve(request, "emptyDefaultCookieValue");
        assertEquals("", resolved);
    }

    @Test
    void testDefaultValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultCookieValue");
        assertEquals("bar", resolved);
    }

    @Test
    void testDefaultCollectionValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultCollectionValue");
        assertNotNull(resolved);
        assertTrue(((Collection) resolved).isEmpty());
    }

    @Test
    void testDefaultArrayValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultArrayValue");
        assertNotNull(resolved);
        assertEquals(0, ((String[]) resolved).length);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDefaultOptionalValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .build();
        final Optional<String> resolved =
                (Optional<String>) createResolverAndResolve(request, "defaultOptionalValue");
        assertFalse(resolved.isPresent());
    }

    @Test
    void testDefaultNull() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .build();
        final Object resolved = createResolverAndResolve(request, "cookieValue");
        assertNull(resolved);
    }

    @Test
    void testInitializeArgumentByConstructor() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withCookie("foo", "bar")
                .build();
        final Object resolved = createResolverAndResolve(request, "constructor");
        assertTrue(resolved instanceof ConstructorSubject);
        assertEquals("bar", ((ConstructorSubject) resolved).getValue());
    }

    @Test
    void testInitializeArgumentByValueOfMethod() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withCookie("foo", "bar")
                .build();
        final Object resolved = createResolverAndResolve(request, "valueOf");
        assertTrue(resolved instanceof ValueOfSubject);
        assertEquals("bar", ((ValueOfSubject) resolved).getValue());
    }

    @Test
    void testInitializeArgumentByFromStringMethod() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withCookie("foo", "bar")
                .build();
        final Object resolved = createResolverAndResolve(request, "fromString");
        assertTrue(resolved instanceof FromStringSubject);
        assertEquals("bar", ((FromStringSubject) resolved).getValue());
    }

    private static Object createResolverAndResolve(HttpRequest request, String method) throws Exception {
        final MethodParam param = handlerMethods.get(method).parameters()[0];
        assertTrue(resolverFactory.supports(param));
        final ParamResolver resolver = resolverFactory.createResolver(param,
                ResolverUtils.defaultConverterFunc(), null);
        return resolver.resolve(param, new RequestContextImpl(request, MockHttpResponse.aMockResponse().build()));
    }

    private static class Subject {
        public void cookieValue(@CookieParam("foo") String foo) {
        }

        public void emptyDefaultCookieValue(@DefaultValue("")
                                            @CookieParam("foo") String foo) {
        }

        public void defaultCookieValue(@DefaultValue("bar")
                                       @CookieParam("foo") String foo) {
        }

        public void primitive(@CookieParam("foo") int foo) {
        }

        public void constructor(@CookieParam("foo") ConstructorSubject foo) {
        }

        public void valueOf(@CookieParam("foo") ValueOfSubject foo) {
        }

        public void fromString(@CookieParam("foo") FromStringSubject foo) {
        }

        public void defaultCollectionValue(@CookieParam("foo") @DefaultValue("") Collection<String> foo) {
        }

        public void defaultArrayValue(@CookieParam("foo") @DefaultValue("") String[] foo) {
        }

        public void defaultOptionalValue(@CookieParam("foo") Optional<String> foo) {
        }
    }

}

