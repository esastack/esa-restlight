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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class PathVariableArgumentResolverTest {

    private static PathVariableArgumentResolver resolverFactory = new PathVariableArgumentResolver();

    private static final Subject SUBJECT = new Subject();

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void init() {
        assumeTrue(RequestMapping0.shadedClass().getName().startsWith("org.springframework"));
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testNormal() throws Exception {
        final String realPath = "/foo/bar";
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri(realPath)
                .build();

        final Object resolved = createResolverAndResolve(request, "requestPathVariable");
        assertEquals("bar", resolved);
    }

    @Test
    void testCleanMatrixVariables() throws Exception {
        final String realPath = "/foo/bar;a=b;c=d";
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri(realPath)
                .build();
        final Object resolved = createResolverAndResolve(request, "requestPathVariable");
        assertEquals("bar", resolved);
    }

    @Test
    void testNamedPathVariable() throws Exception {
        final String realPath = "/foo1/qux";
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri(realPath)
                .build();

        final Object resolved = createResolverAndResolve(request, "requestPathVariableName");

        assertEquals("qux", resolved);
    }

    @Test
    void testRequiredPathVariable() {
        final String realPath = "/foo1";
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri(realPath)
                .build();
        assertThrows(WebServerException.class, () -> createResolverAndResolve(request, "requestPathVariable"));
    }

    @Test
    void testNoneRequiredPathVariable() throws Exception {
        final String realPath = "/foo2";
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri(realPath)
                .build();

        final Object resolved = createResolverAndResolve(request, "noneRequiredPathVariable");
        assertEquals(resolved, "");
    }

    private static Object createResolverAndResolve(AsyncRequest request, String method) throws Exception {
        final MethodParam parameter = handlerMethods.get(method).parameters()[0];
        assertTrue(resolverFactory.supports(parameter));
        // match first
        new PatternsPredicate(RequestMappingUtils.extractMapping(SUBJECT.getClass(),
                parameter.method()).get().path()).test(request);
        final ArgumentResolver resolver = resolverFactory.createResolver(parameter, null);
        return resolver.resolve(request, MockAsyncResponse.aMockResponse().build());
    }

    private static class Subject {
        @RequestMapping("/foo/{foo}")
        public void requestPathVariable(@PathVariable String foo) {
        }

        @RequestMapping("/foo1/{baz}")
        public void requestPathVariableName(@PathVariable(name = "baz") String foo) {
        }

        @RequestMapping("/foo2{foo}")
        public void noneRequiredPathVariable(@PathVariable(required = false) String foo) {
        }
    }

}
