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
import esa.restlight.springmvc.annotation.shaded.RequestParam0;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SuppressWarnings("unchecked")
class RequestRequestParamArgumentResolverTest {

    private static RequestParamArgumentResolver resolverFactory = new RequestParamArgumentResolver();

    private static final Subject SUBJECT = new Subject();

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        assumeTrue(RequestParam0.shadedClass().getName().startsWith("org.springframework"));
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testNormal() throws Exception {

        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withParameter("foo", "bar")
                .build();
        final Object resolved = createResolverAndResolve(request, "requestParam");
        assertEquals("bar", resolved);
    }

    @Test
    void testNamedParam() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withParameter("baz", "qux")
                .build();
        final Object resolved = createResolverAndResolve(request, "requestParamName");
        assertEquals("qux", resolved);
    }

    @Test
    void testRequiredParam() {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        assertThrows(WebServerException.class, () -> createResolverAndResolve(request, "requestParam"));
    }

    @Test
    void testNoneRequiredParam() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final Object resolved = createResolverAndResolve(request, "noneRequiredParam");
        assertNull(resolved);
    }

    @Test
    void testDefaultParam() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultParam");
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

    @SuppressWarnings("unchecked")
    @Test
    void testDefaultOptionalValue() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final Optional<String> resolved =
                (Optional<String>) createResolverAndResolve(request, "defaultOptionalValue");
        assertFalse(resolved.isPresent());
    }

    @Test
    void testDefaultAndRequiredParam() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultAndRequiredParam");
        assertEquals("foo", resolved);
    }

    @Test
    void testDefaultAndRequiredParamTest1() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withParameter("foo", "bar")
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultAndRequiredParam");
        assertEquals("bar", resolved);
    }

    @Test
    void testCollection() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withParameter("collection", "1,  2, 3,4,  5 ")
                .build();
        final Collection resolved = (Collection) createResolverAndResolve(request, "requestParamOfCollection");
        assertEquals(5, resolved.size());
        assertTrue(resolved.contains("1"));
        assertFalse(resolved.contains(1));
    }

    @Test
    void testGenericCollection() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withParameter("collection", "1,  2, 3,4,  5 ")
                .build();
        final Collection<Integer> resolved =
                (Collection<Integer>) createResolverAndResolve(request, "requestParamOfGenericCollection");
        assertEquals(5, resolved.size());
        assertTrue(resolved.contains(1));
        assertFalse(resolved.contains("1"));
    }

    @Test
    void testArray() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withParameter("objects", "1,  2, 3,4,  5 ")
                .build();
        final Object[] resolved = (Object[]) createResolverAndResolve(request, "requestParamOfArray");
        assertEquals(5, resolved.length);
        assertEquals("1", resolved[0]);
    }

    @Test
    void testGenericArray() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withParameter("ints", "1,  2, 3,4,  5 ")
                .build();
        final int[] resolved = (int[]) createResolverAndResolve(request, "requestParamOfGenericArray");
        assertEquals(5, resolved.length);
        assertEquals(1, resolved[0]);
    }

    @Test
    void testSingleValueMap() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withParameter("a", "1")
                .withParameter("a", "2")
                .withParameter("b", "3")
                .build();
        final Map<String, String> resolved = (Map<String, String>) createResolverAndResolve(request,
                "requestParamOfSingleValueMap");
        assertEquals(2, resolved.size());
        assertEquals("1", resolved.get("a"));
        assertEquals("3", resolved.get("b"));
    }

    @Test
    void testEmptySingleValueMap() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        final Map<String, String> resolved = (Map<String, String>) createResolverAndResolve(request,
                "requestParamOfSingleValueMap");
        assertTrue(resolved.isEmpty());
        assertSame(Collections.emptyMap(), resolved);
    }

    @Test
    void testMultiMap() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withParameter("a", "1")
                .withParameter("a", "2")
                .withParameter("b", "3")
                .build();
        final Map<String, List<String>> resolved = (Map<String, List<String>>) createResolverAndResolve(request,
                "requestParamOfMultiMap");
        assertEquals(2, resolved.size());
        assertEquals("1", resolved.get("a").get(0));
        assertEquals("2", resolved.get("a").get(1));
        assertEquals("3", resolved.get("b").get(0));
    }

    private static Object createResolverAndResolve(AsyncRequest request, String method) throws Exception {
        final MethodParam parameter = handlerMethods.get(method).parameters()[0];
        assertTrue(resolverFactory.supports(parameter));
        final ArgumentResolver resolver = resolverFactory.createResolver(parameter,
                Collections.singletonList(new FastJsonHttpBodySerializer()));
        return resolver.resolve(request, MockAsyncResponse.aMockResponse().build());
    }

    private static class Subject {
        void requestParam(@RequestParam String foo) {
        }

        void requestParamName(@RequestParam(name = "baz") String foo) {
        }

        void noneRequiredParam(@RequestParam(required = false) String foo) {
        }

        void defaultParam(@RequestParam(required = false, defaultValue = "foo") String foo) {
        }

        public void defaultCollectionValue(@RequestParam(value = "foo", defaultValue = "") Collection<String> foo) {
        }

        public void defaultArrayValue(@RequestParam(value = "foo", defaultValue = "") String[] foo) {
        }

        public void defaultOptionalValue(@RequestParam(value = "foo") Optional<String> foo) {
        }

        void defaultAndRequiredParam(@RequestParam(defaultValue = "foo") String foo) {
        }

        void requestParamOfCollection(@RequestParam Collection collection) {
        }

        void requestParamOfGenericCollection(@RequestParam
                                                            Collection<Integer> collection) {
        }

        void requestParamOfArray(@RequestParam Object[] objects) {
        }

        void requestParamOfGenericArray(@RequestParam int[] ints) {
        }

        void requestParamOfSingleValueMap(@RequestParam Map<String, String> params) {
        }

        void requestParamOfMultiMap(@RequestParam Map<String, List<String>> params) {
        }
    }
}
