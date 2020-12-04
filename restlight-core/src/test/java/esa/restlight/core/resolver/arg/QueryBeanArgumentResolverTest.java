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
package esa.restlight.core.resolver.arg;

import esa.commons.ClassUtils;
import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.annotation.QueryBean;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.MethodParam;
import esa.restlight.core.mock.MockContext;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.core.resolver.HandlerResolverFactoryImpl;
import esa.restlight.core.serialize.JacksonHttpBodySerializer;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class QueryBeanArgumentResolverTest {

    private static final QueryBeanArgumentResolver resolverFactory =
            new QueryBeanArgumentResolver(MockContext.withHandlerResolverFactory(
                    new HandlerResolverFactoryImpl(Collections.singletonList(new JacksonHttpBodySerializer()),
                            Collections.singletonList(new JacksonHttpBodySerializer()),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null)));

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        handlerMethods = ClassUtils.userDeclaredMethods(Subject.class)
                .stream()
                .map(method -> HandlerMethod.of(method, new Subject()))
                .collect(Collectors.toMap(h -> h.method().getName(), hm -> hm));
    }

    @Test
    void testSupportIfAnnotationPresent() {
        assertTrue(resolverFactory.supports(handlerMethods.get("funcWithQueryBean").parameters()[0]));
        assertFalse(resolverFactory.supports(handlerMethods.get("funcWithOutQueryBean").parameters()[0]));
    }

    @Test
    void testFieldValueSetByReflection() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withParameter("foo", "1")
                .withParameter("bar", "bar")
                .withParameter("baz", "true")
                .build();
        final Pojo pojo = (Pojo) createResolverAndResolve(request, "funcWithQueryBean");
        assertEquals(1, pojo.foo);
        assertEquals("barA", pojo.bar);
        assertTrue(pojo.baz);
    }

    @Test
    void testAlia() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withParameter("foo", "1")
                .withParameter("bar", "bar")
                .withParameter("baz", "true")
                .withParameter("other", "other")
                .build();
        final Pojo pojo = (Pojo) createResolverAndResolve(request, "funcWithQueryBean");
        assertEquals(1, pojo.foo);
        assertEquals("barA", pojo.bar);
        assertTrue(pojo.baz);
        assertEquals("other", pojo.qux);
    }

    @Test
    void testCollections() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withParameter("colles0", "1,2,3,4,5")
                .withParameter("colles1", "1,2,3,4,5")
                .build();
        final Pojo pojo = (Pojo) createResolverAndResolve(request, "funcWithQueryBean");
        assertEquals(5, pojo.colles0.size());
        assertTrue(pojo.colles0.contains(1));
        assertFalse(pojo.colles0.contains("1"));

        assertEquals(5, pojo.colles1.size());
        assertTrue(pojo.colles1.contains("1"));
        assertFalse(pojo.colles1.contains(1));
    }

    @Test
    void testArrays() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withParameter("objects", "1,2,3,4,5")
                .withParameter("ints", "1,2,3,4,5")
                .build();

        final Pojo pojo = (Pojo) createResolverAndResolve(request, "funcWithQueryBean");
        assertEquals(5, pojo.objects.length);
        assertEquals("1", pojo.objects[0]);

        assertEquals(5, pojo.ints.length);
        assertEquals(1, pojo.ints[0]);
    }

    @Test
    void testInitializedValueIfEmptyConstructorPresent() throws Exception {
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withParameter("foo", "2")
                .build();


        final PojoWithEmptyConstructor pojo =
                (PojoWithEmptyConstructor) createResolverAndResolve(request, "funcWithQueryBean1");
        assertEquals(2, pojo.foo);
        assertEquals("a", pojo.bar);
        assertFalse(pojo.baz);
        assertEquals(1, pojo.qux);
    }

    private static Object createResolverAndResolve(AsyncRequest request, String method) throws Exception {
        final MethodParam parameter = handlerMethods.get(method).parameters()[0];
        assertTrue(resolverFactory.supports(parameter));
        final ArgumentResolver resolver = resolverFactory.createResolver(parameter, null);
        return resolver.resolve(request, MockAsyncResponse.aMockResponse().build());
    }

    private static class Subject {
        void funcWithQueryBean(@QueryBean Pojo pojo) {

        }

        void funcWithQueryBean1(@QueryBean PojoWithEmptyConstructor pojo) {

        }

        void funcWithOutQueryBean(Pojo pojo) {

        }
    }

    private static class Pojo {
        private int foo;
        private String bar;
        private boolean baz;
        @QueryBean.Name("other")
        private String qux;

        private Collection<Integer> colles0;
        private Collection colles1;
        private Object[] objects;
        private int[] ints;

        void setBar(String bar) {
            this.bar = bar + "A";
        }
    }

    private static class PojoWithEmptyConstructor {
        private int foo;
        private final String bar = "a";
        private final boolean baz = false;
        private final int qux = 1;

        public PojoWithEmptyConstructor() {
        }
    }

}
