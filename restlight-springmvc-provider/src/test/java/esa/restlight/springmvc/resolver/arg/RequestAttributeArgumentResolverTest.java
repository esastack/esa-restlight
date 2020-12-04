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
import esa.restlight.springmvc.annotation.shaded.PathVariable0;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestAttribute;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class RequestAttributeArgumentResolverTest {

    private static final Subject SUBJECT = new Subject();
    private static RequestAttributeArgumentResolver resolverFactory = new RequestAttributeArgumentResolver();
    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        assumeTrue(PathVariable0.shadedClass().getName().startsWith("org.springframework"));
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testNormal() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withAttribute("foo", "bar")
                .build();
        final Object resolved = createResolverAndResolve(request, "requestAttribute");
        assertEquals("bar", resolved);
    }

    @Test
    void testNamedAttribute() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withAttribute("baz", "qux")
                .build();
        final Object resolved = createResolverAndResolve(request, "requestAttributeName");
        assertEquals("qux", resolved);
    }

    @Test
    void testRequiredAttribute() {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        assertThrows(WebServerException.class, () -> createResolverAndResolve(request, "requestAttribute"));
    }

    @Test
    void testNoneRequiredAttribute() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final Object resolved = createResolverAndResolve(request, "noneRequiredAttribute");
        assertNull(resolved);
    }

    @Test
    void testCollection() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withAttribute("collection", "1,  2, 3,4,  5 ")
                .build();
        final Collection resolved =
                (Collection) createResolverAndResolve(request, "requestAttributeOfCollection");
        assertEquals(5, resolved.size());
        assertTrue(resolved.contains("1"));
        assertFalse(resolved.contains(1));
    }

    @Test
    void testGenericCollection() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withAttribute("collection", "1,  2, 3,4,  5 ")
                .build();
        final Collection<Integer> resolved =
                (Collection<Integer>) createResolverAndResolve(request, "requestAttributeOfGenericCollection");
        assertEquals(5, resolved.size());
        assertTrue(resolved.contains(1));
        assertFalse(resolved.contains("1"));
    }

    @Test
    void testArray() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withAttribute("objects", "1,  2, 3,4,  5 ")
                .build();
        final Object[] resolved = (Object[]) createResolverAndResolve(request, "requestAttributeOfArray");
        assertEquals(5, resolved.length);
        assertEquals("1", resolved[0]);
    }

    @Test
    void testPrimitiveArray() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withAttribute("ints", "1,  2, 3,4,  5 ")
                .build();
        final int[] resolved = (int[]) createResolverAndResolve(request, "requestAttributeOfPrimitiveArray");

        assertEquals(5, resolved.length);
        assertEquals(1, resolved[0]);
    }

    @Test
    void testGenericArray() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withAttribute("ints", "1.01,  2.01, 3,4.111,  5 ")
                .build();
        final BigDecimal[] resolved =
                (BigDecimal[]) createResolverAndResolve(request, "requestAttributeOfGenericArray");
        assertEquals(5, resolved.length);
        assertEquals(new BigDecimal("1.01"), resolved[0]);
    }

    @Test
    void testAttributeOfCharSequence() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withAttribute("foo", "bar")
                .build();
        final Object resolved = createResolverAndResolve(request, "requestAttributeOfCharSequence");
        assertTrue(CharSequence.class.isAssignableFrom(resolved.getClass()));
        assertEquals("bar", resolved);
    }

    private static Object createResolverAndResolve(AsyncRequest request, String method) throws Exception {
        final MethodParam parameter = handlerMethods.get(method).parameters()[0];
        assertTrue(resolverFactory.supports(parameter));
        final ArgumentResolver resolver = resolverFactory.createResolver(parameter, null);
        return resolver.resolve(request, MockAsyncResponse.aMockResponse().build());
    }

    private static class Subject {
        public void requestAttribute(@RequestAttribute String foo) {
        }

        public void requestAttributeName(@RequestAttribute(name = "baz") String foo) {
        }

        public void noneRequiredAttribute(@RequestAttribute(required = false) String foo) {
        }

        public void requestAttributeOfCharSequence(@RequestAttribute CharSequence foo) {
        }

        public void requestAttributeOfCollection(@RequestAttribute Collection collection) {
        }

        public void requestAttributeOfGenericCollection(@RequestAttribute
                                                                Collection<Integer> collection) {
        }

        public void requestAttributeOfArray(@RequestAttribute Object[] objects) {
        }

        public void requestAttributeOfPrimitiveArray(@RequestAttribute int[] ints) {
        }


        public void requestAttributeOfGenericArray(@RequestAttribute BigDecimal[] ints) {
        }
    }

}
