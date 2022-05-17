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
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.MethodParam;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.core.mock.MockHttpResponse;
import io.esastack.restlight.core.resolver.param.ParamResolver;
import io.esastack.restlight.core.resolver.param.ParamResolverContext;
import io.esastack.restlight.core.resolver.param.ParamResolverContextImpl;
import io.esastack.restlight.core.route.predicate.PatternsPredicate;
import io.esastack.restlight.jaxrs.resolver.ResolverUtils;
import io.esastack.restlight.jaxrs.resolver.param.subject.ConstructorSubject;
import io.esastack.restlight.jaxrs.resolver.param.subject.FromStringSubject;
import io.esastack.restlight.jaxrs.resolver.param.subject.ValueOfSubject;
import io.esastack.restlight.jaxrs.util.JaxrsMappingUtils;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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

class PathParamResolverTest {

    private static final PathParamResolver resolverFactory = new PathParamResolver();

    private static final Subject SUBJECT = new Subject();

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void init() {
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testPathVar() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/foo")
                .build();

        final Object resolved = createResolverAndResolve(request, "pathVar");
        assertEquals("foo", resolved);
    }

    @Test
    void testCleanMatrixVariables() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/foo;a=b;c=d")
                .build();
        final Object resolved = createResolverAndResolve(request, "pathVar");
        assertEquals("foo", resolved);
    }

    @Test
    void testNone() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/")
                .build();

        final Object resolved = createResolverAndResolve(request, "none");

        assertNull(resolved);
    }


    @Test
    void testDefaultValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/")
                .build();

        final Object resolved = createResolverAndResolve(request, "defaultValue");
        assertEquals("default", resolved);
    }

    @Test
    void testDefaultCollectionValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/")
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultCollectionValue");
        assertNotNull(resolved);
        assertTrue(((Collection) resolved).isEmpty());
    }

    @Test
    void testDefaultArrayValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/")
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
                .withUri("/")
                .build();
        final Optional<String> resolved =
                (Optional<String>) createResolverAndResolve(request, "defaultOptionalValue");
        assertFalse(resolved.isPresent());
    }

    @Test
    void testConstructor() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/foo")
                .build();

        final Object resolved = createResolverAndResolve(request, "constructor");
        assertTrue(resolved instanceof ConstructorSubject);
        assertEquals("foo", ((ConstructorSubject) resolved).getValue());
    }

    @Test
    void testValueOf() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/foo")
                .build();

        final Object resolved = createResolverAndResolve(request, "valueOf");
        assertTrue(resolved instanceof ValueOfSubject);
        assertEquals("foo", ((ValueOfSubject) resolved).getValue());
    }

    @Test
    void testFromString() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/foo")
                .build();

        final Object resolved = createResolverAndResolve(request, "fromString");
        assertTrue(resolved instanceof FromStringSubject);
        assertEquals("foo", ((FromStringSubject) resolved).getValue());
    }

    private static Object createResolverAndResolve(HttpRequest request, String method) throws Exception {
        final MethodParam param = handlerMethods.get(method).parameters()[0];
        assertTrue(resolverFactory.supports(param));
        // match first
        final RequestContext context = new RequestContextImpl(request, MockHttpResponse.aMockResponse().build());
        new PatternsPredicate(JaxrsMappingUtils.extractMapping(SUBJECT.getClass(),
                param.method(), StringUtils.empty()).get().path()).test(context);
        final ParamResolver resolver = resolverFactory.createResolver(param,
                ResolverUtils.defaultConverters(param), null);
        final ParamResolverContext resolverContext = new ParamResolverContextImpl(context, param);
        return resolver.resolve(resolverContext);
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
        public void defaultCollectionValue(@PathParam("foo") @DefaultValue("") Collection<String> foo) {
        }

        @GET
        @Path("/{foo}")
        public void defaultArrayValue(@PathParam("foo") @DefaultValue("") String[] foo) {
        }

        @GET
        @Path("/{foo}")
        public void defaultOptionalValue(@PathParam("foo") Optional<String> foo) {
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

