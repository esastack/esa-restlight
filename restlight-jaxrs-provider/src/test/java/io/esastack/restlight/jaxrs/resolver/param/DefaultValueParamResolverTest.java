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
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultValueParamResolverTest {

    private static final DefaultValueParamResolver resolverFactory = new DefaultValueParamResolver();

    private static final Subject SUBJECT = new Subject();

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testSupport() {
        final MethodParam parameter = handlerMethods.get("none").parameters()[0];
        assertFalse(resolverFactory.supports(parameter));

        final MethodParam parameter1 = handlerMethods.get("defaultValue").parameters()[0];
        assertTrue(resolverFactory.supports(parameter1));
    }

    @Test
    void testDefaultValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultValue");
        assertEquals("default", resolved);
    }

    @Test
    void testConstructor() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .build();
        final Object resolved = createResolverAndResolve(request, "constructor");
        assertTrue(resolved instanceof ConstructorSubject);
        assertEquals("default", ((ConstructorSubject) resolved).getValue());
    }

    @Test
    void testValueOf() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withParameter("foo", "bar")
                .build();
        final Object resolved = createResolverAndResolve(request, "valueOf");
        assertTrue(resolved instanceof ValueOfSubject);
        assertEquals("default", ((ValueOfSubject) resolved).getValue());
    }

    @Test
    void testFromString() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withParameter("foo", "bar")
                .build();
        final Object resolved = createResolverAndResolve(request, "fromString");
        assertTrue(resolved instanceof FromStringSubject);
        assertEquals("default", ((FromStringSubject) resolved).getValue());
    }

    @Test
    void testDefaultCollectionValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withParameter("foo", "")
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultCollectionValue");
        assertTrue(resolved instanceof Collection);
        assertTrue(((Collection) resolved).isEmpty());
    }

    @Test
    void testDefaultArrayValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withParameter("foo", "")
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultArrayValue");
        assertTrue(resolved instanceof String[]);
        assertEquals(0, ((String[]) resolved).length);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDefaultOptionalValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withParameter("foo", "")
                .build();
        final Optional<Integer> resolved = (Optional<Integer>)
                createResolverAndResolve(request, "defaultOptionalValue");
        assertFalse(resolved.isPresent());
    }

    private static Object createResolverAndResolve(HttpRequest request, String method) throws Exception {
        final MethodParam param = handlerMethods.get(method).parameters()[0];
        assertTrue(resolverFactory.supports(param));
        final ParamResolver resolver = resolverFactory.createResolver(param,
                ResolverUtils.defaultConverterFunc(), null);
        return resolver.resolve(param, new RequestContextImpl(request,
                MockHttpResponse.aMockResponse().build()));
    }

    private static class Subject {

        public void defaultValue(@DefaultValue("default") String foo) {
        }

        public void none(@QueryParam("foo") String foo) {
        }


        public void constructor(@DefaultValue("default") ConstructorSubject foo) {
        }


        public void valueOf(@DefaultValue("default") ValueOfSubject foo) {
        }

        public void fromString(@DefaultValue("default") FromStringSubject foo) {
        }

        public void defaultCollectionValue(@DefaultValue("") Collection<String> foo) {
        }

        public void defaultArrayValue(@DefaultValue("") String[] foo) {
        }

        public void defaultOptionalValue(@DefaultValue("") Optional<Integer> foo) {
        }
    }

}

