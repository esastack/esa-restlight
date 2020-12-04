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
package esa.restlight.jaxrs.resolver.arg;

import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.MethodParam;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.jaxrs.ResolverUtils;
import esa.restlight.jaxrs.resolver.arg.subject.ConstructorSubject;
import esa.restlight.jaxrs.resolver.arg.subject.FromStringSubject;
import esa.restlight.jaxrs.resolver.arg.subject.ValueOfSubject;
import esa.restlight.jaxrs.util.JaxrsMappingUtils;
import esa.restlight.server.route.predicate.PatternsPredicate;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatrixParamArgumentResolverTest {

    private static final Subject SUBJECT = new Subject();
    private static MatrixParamArgumentResolver resolverFactory = new MatrixParamArgumentResolver();
    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void init() {
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testSingleValue() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri("/foo;a=1")
                .build();

        final Object resolved = createResolverAndResolve(request, "single", 0);
        assertEquals("1", resolved);
    }

    @Test
    void testMultiValue() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri("/foo;a=1;b=2")
                .build();

        final Object resolved1 = createResolverAndResolve(request, "multiValue", 0);
        final Object resolved2 = createResolverAndResolve(request, "multiValue", 1);
        assertEquals("1", resolved1);
        assertEquals("2", resolved2);
    }

    @Test
    void testListValue() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri("/foo;a=1,2,3")
                .build();

        final List<String> resolved
                = (List<String>) createResolverAndResolve(request, "listValue", 0);

        assertEquals(3, resolved.size());
        assertTrue(resolved.contains("1"));
        assertTrue(resolved.contains("2"));
        assertTrue(resolved.contains("3"));
    }

    @Test
    void testNoneMatrixParam() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri("/foo/bar")
                .build();

        final Object resolved = createResolverAndResolve(request, "none", 0);

        assertNull(resolved);
    }

    @Test
    void testDefaultValue() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri("/foo")
                .build();

        final Object resolved = createResolverAndResolve(request, "defaultValue", 0);
        assertEquals("default", resolved);
    }

    @Test
    void testConstructorValue() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri("/foo;a=1")
                .build();

        final Object resolved = createResolverAndResolve(request, "constructor", 0);
        assertTrue(resolved instanceof ConstructorSubject);
        assertEquals("1", ((ConstructorSubject) resolved).getValue());
    }

    @Test
    void testValueOf() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri("/foo;a=1")
                .build();

        final Object resolved = createResolverAndResolve(request, "valueOf", 0);
        assertTrue(resolved instanceof ValueOfSubject);
        assertEquals("1", ((ValueOfSubject) resolved).getValue());
    }

    @Test
    void testFromString() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri("/foo;a=1")
                .build();

        final Object resolved = createResolverAndResolve(request, "fromString", 0);
        assertTrue(resolved instanceof FromStringSubject);
        assertEquals("1", ((FromStringSubject) resolved).getValue());
    }

    private static Object createResolverAndResolve(AsyncRequest request, String method, int index) throws Exception {
        final MethodParam parameter = handlerMethods.get(method).parameters()[index];
        assertTrue(resolverFactory.supports(parameter));
        // match first
        new PatternsPredicate(JaxrsMappingUtils.extractMapping(SUBJECT.getClass(),
                parameter.method()).get().path()).test(request);
        final ArgumentResolver resolver = resolverFactory.createResolver(parameter, null);
        return resolver.resolve(request, MockAsyncResponse.aMockResponse().build());
    }

    private static class Subject {

        @Path("/{foo}")
        @GET
        public void single(@MatrixParam("a") String a) {
        }

        @Path("/{foo}")
        @GET
        public void multiValue(@MatrixParam("a") String a,
                               @MatrixParam("b") String b) {
        }

        @Path("/{foo}")
        @GET
        public void listValue(@MatrixParam("a") List<String> a) {
        }

        @Path("/{foo}")
        @GET
        public void none(@MatrixParam("a") String foo) {
        }

        @Path("/{foo}")
        @GET
        public void defaultValue(@DefaultValue("default")
                                 @MatrixParam("a") String foo) {
        }

        @Path("/{foo}")
        @GET
        public void constructor(@MatrixParam("a") ConstructorSubject foo) {
        }

        @Path("/{foo}")
        @GET
        public void valueOf(@MatrixParam("a") ValueOfSubject foo) {
        }

        @Path("/{foo}")
        @GET
        public void fromString(@MatrixParam("a") FromStringSubject foo) {
        }
    }

}
