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
import esa.restlight.server.route.predicate.PatternsPredicate;
import esa.restlight.springmvc.ResolverUtils;
import esa.restlight.springmvc.annotation.shaded.RequestMapping0;
import esa.restlight.springmvc.util.RequestMappingUtils;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SuppressWarnings("unchecked")
class MatrixVariableArgumentResolverTest {

    private static final Subject SUBJECT = new Subject();
    private static MatrixVariableArgumentResolver resolverFactory = new MatrixVariableArgumentResolver();
    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void init() {
        assumeTrue(RequestMapping0.shadedClass().getName().startsWith("org.springframework"));
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testNormal() throws Exception {
        final String realPath = "/foo/bar;a=bar";
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri(realPath)
                .build();

        final Object resolved = createResolverAndResolve(request, "requestMatrixVariable", 0);
        assertEquals("bar", resolved);
    }

    @Test
    void testNormal1() throws Exception {
        final String realPath = "/foo1/qux;a=qux,qux1,qux2";
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri(realPath)
                .build();

        final List<String> resolved = (List<String>) createResolverAndResolve(request, "requestMatrixVariable1", 0);

        assertEquals(3, resolved.size());
        assertTrue(resolved.contains("qux"));
        assertTrue(resolved.contains("qux1"));
        assertTrue(resolved.contains("qux2"));
    }

    @Test
    void testRequiredMatrixVariable() {
        final String realPath = "/foo1/qux";
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri(realPath)
                .build();

        assertThrows(WebServerException.class,
                () -> createResolverAndResolve(request, "requestMatrixVariable1", 0));
    }

    @Test
    void testNoneRequiredMatrixVariable() throws Exception {
        final String realPath = "/foo2/qux";
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri(realPath)
                .build();

        final Object resolved = createResolverAndResolve(request, "noneRequiredMatrixVariable", 0);

        assertNull(resolved);
    }

    @Test
    void testMultipartMatrixVariables() throws Exception {
        final String realPath = "/foo3/qux;a=qux1,qux2,qux3/abc/qux;a=qux1,qux2,qux3";
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri(realPath)
                .build();

        final List<String> resolved1
                = (List<String>) createResolverAndResolve(request, "multipartMatrixVariables", 0);
        assertEquals(3, resolved1.size());
        assertTrue(resolved1.contains("qux3"));
        assertTrue(resolved1.contains("qux1"));
        assertTrue(resolved1.contains("qux2"));

        final List<String> resolved2
                = (List<String>) createResolverAndResolve(request, "multipartMatrixVariables", 1);

        assertEquals(3, resolved1.size());
        assertTrue(resolved2.contains("qux3"));
        assertTrue(resolved2.contains("qux1"));
        assertTrue(resolved2.contains("qux2"));
    }

    @Test
    void testNoneNameMultipartVariable() throws Exception {
        final String realPath = "/foo4/qux;name=qux";
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri(realPath)
                .build();

        final Object resolved = createResolverAndResolve(request, "noneNameMatrixVariable", 0);
        assertEquals("qux", resolved);
    }

    @Test
    void testMatrixVariableMap() throws Exception {
        final String realPath = "/foo5/a=11,12/foo2/21;a=22;s=23";
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri(realPath)
                .build();

        final Map resolved = (Map) createResolverAndResolve(request, "matrixVariableMap", 0);
        assertEquals(2, resolved.size());
        assertEquals("11", resolved.get("a"));
        assertEquals("23", resolved.get("s"));
    }


    @Test
    void testMultiMatrixVariableMap() throws Exception {
        final String realPath = "/foo6/a=11,12/foo2/21;a=22;s=23";
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri(realPath)
                .build();

        final Map<String, List<String>> resolved =
                (Map<String, List<String>>) createResolverAndResolve(request, "matrixVariableListMap", 0);
        assertEquals(2, resolved.size());
        assertEquals(3, resolved.get("a").size());
        assertTrue(resolved.get("a").contains("11"));
        assertTrue(resolved.get("a").contains("12"));
        assertTrue(resolved.get("a").contains("22"));
        assertEquals(1, resolved.get("s").size());
        assertTrue(resolved.get("s").contains("23"));
    }

    private static Object createResolverAndResolve(AsyncRequest request, String method, int index) throws Exception {
        final MethodParam parameter = handlerMethods.get(method).parameters()[index];
        assertTrue(resolverFactory.supports(parameter));
        // match first
        new PatternsPredicate(RequestMappingUtils.extractMapping(SUBJECT.getClass(),
                parameter.method()).get().path()).test(request);
        final ArgumentResolver resolver = resolverFactory.createResolver(parameter, null);
        return resolver.resolve(request, MockAsyncResponse.aMockResponse().build());
    }

    private static class Subject {
        @RequestMapping("/foo/{foo}")
        public void requestMatrixVariable(@MatrixVariable(name = "a", pathVar = "foo") String name) {
        }

        @RequestMapping("/foo1/{baz}")
        public void requestMatrixVariable1(@MatrixVariable(name = "a", pathVar = "baz") List<String> names) {
        }

        @RequestMapping("/foo2/{foo}")
        public void noneRequiredMatrixVariable(@MatrixVariable(name = "a", pathVar = "foo", required = false)
                                                       String name) {
        }

        @RequestMapping("/foo3/{foo}/abc/{baz}")
        public void multipartMatrixVariables(@MatrixVariable(name = "a", pathVar = "foo") List<String> names1,
                                             @MatrixVariable(name = "a", pathVar = "baz") List<String> names2) {
        }

        @RequestMapping("/foo4/{foo}")
        public void noneNameMatrixVariable(@MatrixVariable(pathVar = "foo") String name) {
        }

        @RequestMapping("/foo5/{foo1}/foo2/{foo3}")
        public void matrixVariableMap(@MatrixVariable Map<String, String> matrixVariables) {
        }

        @RequestMapping("/foo6/{foo1}/foo2/{foo3}")
        public void matrixVariableListMap(@MatrixVariable Map<String, List<String>> multiValueVariables) {
        }
    }

}
