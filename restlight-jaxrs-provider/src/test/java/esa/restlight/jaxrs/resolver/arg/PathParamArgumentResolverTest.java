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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PathParamArgumentResolverTest {

    private static PathParamArgumentResolver resolverFactory = new PathParamArgumentResolver();

    private static final Subject SUBJECT = new Subject();

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void init() {
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testPathVar() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri("/foo")
                .build();

        final Object resolved = createResolverAndResolve(request, "pathVar");
        assertEquals("foo", resolved);
    }

    @Test
    void testCleanMatrixVariables() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri("/foo;a=b;c=d")
                .build();
        final Object resolved = createResolverAndResolve(request, "pathVar");
        assertEquals("foo", resolved);
    }

    @Test
    void testNone() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri("/")
                .build();

        final Object resolved = createResolverAndResolve(request, "none");

        assertNull(resolved);
    }


    @Test
    void testDefaultValue() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri("/")
                .build();

        final Object resolved = createResolverAndResolve(request, "defaultValue");
        assertEquals("default", resolved);
    }

    @Test
    void testConstructor() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri("/foo")
                .build();

        final Object resolved = createResolverAndResolve(request, "constructor");
        assertTrue(resolved instanceof ConstructorSubject);
        assertEquals("foo", ((ConstructorSubject) resolved).getValue());
    }

    @Test
    void testValueOf() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri("/foo")
                .build();

        final Object resolved = createResolverAndResolve(request, "valueOf");
        assertTrue(resolved instanceof ValueOfSubject);
        assertEquals("foo", ((ValueOfSubject) resolved).getValue());
    }

    @Test
    void testFromString() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri("/foo")
                .build();

        final Object resolved = createResolverAndResolve(request, "fromString");
        assertTrue(resolved instanceof FromStringSubject);
        assertEquals("foo", ((FromStringSubject) resolved).getValue());
    }

    private static Object createResolverAndResolve(AsyncRequest request, String method) throws Exception {
        final MethodParam parameter = handlerMethods.get(method).parameters()[0];
        assertTrue(resolverFactory.supports(parameter));
        // match first
        new PatternsPredicate(JaxrsMappingUtils.extractMapping(SUBJECT.getClass(),
                parameter.method()).get().path()).test(request);
        final ArgumentResolver resolver = resolverFactory.createResolver(parameter, null);
        return resolver.resolve(request, MockAsyncResponse.aMockResponse().build());
    }

    private static class Subject {
        @GET
        @Path("/{foo}")
        public void pathVar(@PathParam("foo") String foo) {
        }

        @GET
        @Path("/{foo}")
        public void none(@PathParam("foo") String foo) {
        }

        @GET
        @Path("/{foo}")
        public void defaultValue(@DefaultValue("default")
                                 @PathParam("foo") String foo) {
        }

        @GET
        @Path("/{foo}")
        public void constructor(@PathParam("foo") ConstructorSubject foo) {
        }

        @GET
        @Path("/{foo}")
        public void valueOf(@PathParam("foo") ValueOfSubject foo) {
        }

        @GET
        @Path("/{foo}")
        public void fromString(@PathParam("foo") FromStringSubject foo) {
        }
    }

}
