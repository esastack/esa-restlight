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

import esa.commons.StringUtils;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.MethodParam;
import io.esastack.restlight.core.resolver.param.ParamResolver;
import io.esastack.restlight.jaxrs.resolver.ResolverUtils;
import io.esastack.restlight.jaxrs.resolver.param.subject.ConstructorSubject;
import io.esastack.restlight.jaxrs.resolver.param.subject.FromStringSubject;
import io.esastack.restlight.jaxrs.resolver.param.subject.ValueOfSubject;
import io.esastack.restlight.jaxrs.util.JaxrsMappingUtils;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.core.mock.MockHttpResponse;
import io.esastack.restlight.core.route.predicate.PatternsPredicate;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatrixVariableParamResolverTest {

    private static final Subject SUBJECT = new Subject();
    private static final MatrixVariableParamResolver resolverFactory = new MatrixVariableParamResolver();
    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void init() {
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testSingleValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/foo;a=1")
                .build();

        final Object resolved = createResolverAndResolve(request, "single", 0);
        assertEquals("1", resolved);
    }

    @Test
    void testMultiValue() throws Exception {
        final HttpRequest request = MockHttpRequest
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
        final HttpRequest request = MockHttpRequest
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
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/foo/bar")
                .build();

        final Object resolved = createResolverAndResolve(request, "none", 0);

        assertNull(resolved);
    }

    @Test
    void testDefaultValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/foo")
                .build();

        final Object resolved = createResolverAndResolve(request, "defaultValue", 0);
        assertEquals("default", resolved);
    }

    @Test
    void testDefaultCollectionValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/foo")
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultCollectionValue", 0);
        assertNotNull(resolved);
        assertTrue(((Collection) resolved).isEmpty());
    }

    @Test
    void testDefaultArrayValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/foo")
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultArrayValue", 0);
        assertNotNull(resolved);
        assertEquals(0, ((String[]) resolved).length);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDefaultOptionalValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/foo")
                .build();
        final Optional<String> resolved =
                (Optional<String>) createResolverAndResolve(request, "defaultOptionalValue", 0);
        assertFalse(resolved.isPresent());
    }

    @Test
    void testConstructorValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/foo;a=1")
                .build();

        final Object resolved = createResolverAndResolve(request, "constructor", 0);
        assertTrue(resolved instanceof ConstructorSubject);
        assertEquals("1", ((ConstructorSubject) resolved).getValue());
    }

    @Test
    void testValueOf() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/foo;a=1")
                .build();

        final Object resolved = createResolverAndResolve(request, "valueOf", 0);
        assertTrue(resolved instanceof ValueOfSubject);
        assertEquals("1", ((ValueOfSubject) resolved).getValue());
    }

    @Test
    void testFromString() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/foo;a=1")
                .build();

        final Object resolved = createResolverAndResolve(request, "fromString", 0);
        assertTrue(resolved instanceof FromStringSubject);
        assertEquals("1", ((FromStringSubject) resolved).getValue());
    }

    private static Object createResolverAndResolve(HttpRequest request, String method, int index) throws Exception {
        final MethodParam param = handlerMethods.get(method).parameters()[index];
        assertTrue(resolverFactory.supports(param));
        // match first
        final RequestContext context = new RequestContextImpl(request, MockHttpResponse.aMockResponse().build());
        new PatternsPredicate(JaxrsMappingUtils.extractMapping(SUBJECT.getClass(),
                param.method(), StringUtils.empty()).get().path()).test(context);
        final ParamResolver resolver = resolverFactory.createResolver(param,
                ResolverUtils.defaultConverters(param), null);
        return resolver.resolve(context);
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
        public void defaultArrayValue(@DefaultValue("")
                                      @MatrixParam("a") String[] foo) {
        }

        @Path("/{foo}")
        @GET
        public void defaultCollectionValue(@DefaultValue("")
                                           @MatrixParam("a") Collection foo) {
        }

        @Path("/{foo}")
        @GET
        public void defaultOptionalValue(@MatrixParam("a") Optional<String> foo) {
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

