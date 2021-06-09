/*
 * Copyright 2021 OPPO ESA Stack Project
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

import esa.commons.collection.LinkedMultiValueMap;
import esa.commons.collection.MultiValueMap;
import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.method.MethodParamImpl;
import esa.restlight.core.method.Param;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.server.bootstrap.WebServerException;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractMatrixArgumentResolverTest {

    private static final String attrKey = "$matrix.vars";

    @Test
    void testSingleValue() throws Exception {
        final Map<String, MultiValueMap<String, String>> values = new HashMap<>();
        final String name = "single";

        final MultiValueMap<String, String> single = new LinkedMultiValueMap<>();
        single.putSingle(name, name);
        values.put(name, single);

        final AsyncRequest request0 = MockAsyncRequest
                .aMockRequest()
                .withAttribute(attrKey, values)
                .build();
        final NameAndValue nav0 = new NameAndValue(name, true, null);
        final ArgumentResolver resolver0 = asResolver(nav0, false).createResolver(new MethodParamImpl(
                this.getClass().getMethod("a", String.class), 0), null);
        final Object resolved0 = resolver0.resolve(request0, MockAsyncResponse.aMockResponse().build());
        assertNotNull(resolved0);
        assertEquals(name, resolved0);

        final NameAndValue nav1 = new NameAndValue(name, false, "default");
        final ArgumentResolver resolver1 = asResolver(nav1, false).createResolver(new MethodParamImpl(
                this.getClass().getMethod("a", String.class), 0), null);
        final Object resolved1 = resolver1.resolve(MockAsyncRequest.aMockRequest().build(),
                MockAsyncResponse.aMockResponse().build());
        assertNotNull(resolved1);
        assertEquals("default", resolved1);

        final NameAndValue nav2 = new NameAndValue(name, true, null);
        final ArgumentResolver resolver2 = asResolver(nav2, false).createResolver(new MethodParamImpl(
                this.getClass().getMethod("a", String.class), 0), null);

        assertThrows(WebServerException.class, () -> resolver2
                .resolve(MockAsyncRequest.aMockRequest().build(), MockAsyncResponse.aMockResponse().build()));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testResolveName1() throws Exception {
        // case1: matrixVariableMap == true, singleValueMap == true, pathVar == null
        AsyncRequest request = MockAsyncRequest.aMockRequest().withAttribute(attrKey,
                new LinkedMultiValueMap<>()).build();
        NameAndValue nav = new NameAndValue("", false, null);
        AbstractMatrixArgumentResolver.Resolver resolver = (AbstractMatrixArgumentResolver.Resolver)
                asResolver(nav, true).createResolver(new MethodParamImpl(
                                this.getClass().getMethod("m1", Map.class), 0),
                        null);
        Map<String, String> resolved = (Map<String, String>)
                resolver.resolveName("", request);
        assertTrue(resolved.isEmpty());

        final Map<String, MultiValueMap<String, String>> values1 = new LinkedHashMap<>();
        MultiValueMap<String, String> v1 = new LinkedMultiValueMap<>();
        v1.putSingle("x", "y");
        v1.add("x", "z");
        values1.put("m1", v1);

        resolved = (Map<String, String>) resolver.resolveName("x", MockAsyncRequest.aMockRequest()
                .withAttribute(attrKey, values1).build());
        assertEquals(1, resolved.size());
        assertEquals("y", resolved.get("x"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testResolveName2() throws Exception {
        // case2: matrixVariableMap == false, singleValueMap == false, pathVar == null
        AsyncRequest request = MockAsyncRequest.aMockRequest().withAttribute(attrKey,
                new LinkedMultiValueMap<>()).build();
        NameAndValue nav = new NameAndValue("m1", false, null);
        AbstractMatrixArgumentResolver.Resolver resolver = (AbstractMatrixArgumentResolver.Resolver)
                asResolver(nav, true).createResolver(new MethodParamImpl(
                                this.getClass().getMethod("m2", List.class), 0),
                        null);
        List<String> resolved = (List<String>) resolver.resolveName("", request);
        assertNull(resolved);

        final Map<String, MultiValueMap<String, String>> values1 = new LinkedHashMap<>();
        MultiValueMap<String, String> v1 = new LinkedMultiValueMap<>();
        v1.putSingle("x", "y");
        v1.add("x", "z");
        values1.put("m1", v1);

        resolved = (List<String>) resolver.resolveName("x", MockAsyncRequest.aMockRequest()
                .withAttribute(attrKey, values1).build());
        assertEquals(2, resolved.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testResolveName3() throws Exception {
        // case3: matrixVariableMap == true, singleValueMap == false, pathVar == null
        AsyncRequest request = MockAsyncRequest.aMockRequest().withAttribute(attrKey,
                new LinkedMultiValueMap<>()).build();
        NameAndValue nav = new NameAndValue("", false, null);
        AbstractMatrixArgumentResolver.Resolver resolver = (AbstractMatrixArgumentResolver.Resolver)
                asResolver(nav, true).createResolver(new MethodParamImpl(
                                this.getClass().getMethod("m3", Map.class), 0),
                        null);
        Map<String, List<String>> resolved = (Map<String, List<String>>)
                resolver.resolveName("", request);
        assertTrue(resolved.isEmpty());

        final Map<String, MultiValueMap<String, String>> values1 = new LinkedHashMap<>();
        MultiValueMap<String, String> v1 = new LinkedMultiValueMap<>();
        v1.putSingle("x", "y");
        v1.add("x", "z");
        values1.put("m1", v1);

        resolved = (Map<String, List<String>>) resolver.resolveName("x", MockAsyncRequest.aMockRequest()
                .withAttribute(attrKey, values1).build());
        assertEquals(1, resolved.size());
        assertEquals("y", resolved.get("x").get(0));
        assertEquals("z", resolved.get("x").get(1));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testResolveName4() throws Exception {
        // case4: matrixVariableMap == true, singleValueMap == true, pathVar != null
        AsyncRequest request = MockAsyncRequest.aMockRequest().withAttribute(attrKey,
                new LinkedMultiValueMap<>()).build();
        NameAndValue nav = new NameAndValue("", false, null);
        AbstractMatrixArgumentResolver.Resolver resolver = (AbstractMatrixArgumentResolver.Resolver)
                asResolver(nav, false).createResolver(new MethodParamImpl(
                                this.getClass().getMethod("m1", Map.class), 0),
                        null);
        Map<String, String> resolved = (Map<String, String>)
                resolver.resolveName("", request);
        assertTrue(resolved.isEmpty());

        final Map<String, MultiValueMap<String, String>> values1 = new LinkedHashMap<>();
        MultiValueMap<String, String> v1 = new LinkedMultiValueMap<>();
        v1.putSingle("x", "y");
        v1.add("x", "z");
        values1.put("", v1);

        resolved = (Map<String, String>) resolver.resolveName("x", MockAsyncRequest.aMockRequest()
                .withAttribute(attrKey, values1).build());
        assertEquals(1, resolved.size());
        assertEquals("y", resolved.get("x"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testResolveName5() throws Exception {
        // case5: matrixVariableMap == false, singleValueMap == false, pathVar != null
        AsyncRequest request = MockAsyncRequest.aMockRequest().withAttribute(attrKey,
                new LinkedMultiValueMap<>()).build();
        NameAndValue nav = new NameAndValue("m1", false, null);
        AbstractMatrixArgumentResolver.Resolver resolver = (AbstractMatrixArgumentResolver.Resolver)
                asResolver(nav, false).createResolver(new MethodParamImpl(
                                this.getClass().getMethod("m2", List.class), 0),
                        null);
        List<String> resolved = (List<String>) resolver.resolveName("", request);
        assertNull(resolved);

        final Map<String, MultiValueMap<String, String>> values1 = new LinkedHashMap<>();
        MultiValueMap<String, String> v1 = new LinkedMultiValueMap<>();
        v1.putSingle("x", "y");
        v1.add("x", "z");
        values1.put("m1", v1);

        resolved = (List<String>) resolver.resolveName("x", MockAsyncRequest.aMockRequest()
                .withAttribute(attrKey, values1).build());
        assertEquals(2, resolved.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testResolveName6() throws Exception {
        // case6: matrixVariableMap == true, singleValueMap == false, pathVar != null
        AsyncRequest request = MockAsyncRequest.aMockRequest().withAttribute(attrKey,
                new LinkedMultiValueMap<>()).build();
        NameAndValue nav = new NameAndValue("", false, null);
        AbstractMatrixArgumentResolver.Resolver resolver = (AbstractMatrixArgumentResolver.Resolver)
                asResolver(nav, false).createResolver(new MethodParamImpl(
                                this.getClass().getMethod("m3", Map.class), 0),
                        null);
        Map<String, List<String>> resolved = (Map<String, List<String>>)
                resolver.resolveName("", request);
        assertTrue(resolved.isEmpty());

        final Map<String, MultiValueMap<String, String>> values1 = new LinkedHashMap<>();
        MultiValueMap<String, String> v1 = new LinkedMultiValueMap<>();
        v1.putSingle("x", "y");
        v1.add("x", "z");
        values1.put("", v1);

        MultiValueMap<String, String> resolved1 = (MultiValueMap<String, String>)
                resolver.resolveName("x", MockAsyncRequest.aMockRequest()
                .withAttribute(attrKey, values1).build());
        assertEquals(1, resolved1.size());
        assertEquals("y", resolved1.get("x").get(0));
        assertEquals("z", resolved1.get("x").get(1));
    }

    private static AbstractMatrixArgumentResolver asResolver(NameAndValue nav, boolean nullPathValue) {
        return new MatrixArgumentResolverFactory(nav, nullPathValue);
    }

    private static class MatrixArgumentResolverFactory extends AbstractMatrixArgumentResolver {

        private final NameAndValue nav;
        private final boolean nullPathValue;

        private MatrixArgumentResolverFactory(NameAndValue nav, boolean nullPathValue) {
            this.nav = nav;
            this.nullPathValue = nullPathValue;
        }

        @Override
        protected String getPathVar(Param param) {
            return nullPathValue ? null : nav.name;
        }

        @Override
        protected NameAndValue createNameAndValue(Param param) {
            return nav;
        }

        @Override
        public boolean supports(Param param) {
            return true;
        }
    }

    public void a(String name) {

    }

    public void m1(Map<String, String> names) {

    }

    public void m2(List<String> name) {

    }

    public void m3(Map<String, List<String>> names) {

    }
}

