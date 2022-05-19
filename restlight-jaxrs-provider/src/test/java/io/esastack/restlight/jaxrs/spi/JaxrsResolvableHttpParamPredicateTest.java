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
package io.esastack.restlight.jaxrs.spi;

import io.esastack.restlight.core.handler.method.ConstructorParamImpl;
import io.esastack.restlight.core.handler.method.FieldParamImpl;
import io.esastack.restlight.core.handler.method.MethodParamImpl;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JaxrsResolvableHttpParamPredicateTest {

    @Test
    void testPredicate() throws Throwable {
        final JaxrsResolvableParamPredicate predicate = new JaxrsResolvableParamPredicate();
        assertTrue(predicate.test(new MethodParamImpl(HelloDemo1.class.getDeclaredMethod("demo1",
                String.class), 0)));

        final Class<?> target = HelloDemo2.class;
        final Constructor<?> constructor = target.getConstructors()[0];
        assertTrue(predicate.test(new ConstructorParamImpl(constructor, 0)));
        assertTrue(predicate.test(new ConstructorParamImpl(constructor, 1)));
        assertTrue(predicate.test(new ConstructorParamImpl(constructor, 2)));
        assertTrue(predicate.test(new ConstructorParamImpl(constructor, 3)));
        assertTrue(predicate.test(new ConstructorParamImpl(constructor, 4)));
        assertTrue(predicate.test(new ConstructorParamImpl(constructor, 5)));
        assertTrue(predicate.test(new ConstructorParamImpl(constructor, 6)));
        assertTrue(predicate.test(new ConstructorParamImpl(constructor, 7)));
        assertFalse(predicate.test(new ConstructorParamImpl(constructor, 8)));
        assertTrue(predicate.test(new FieldParamImpl(target.getDeclaredField("configuration"))));
        assertTrue(predicate.test(new FieldParamImpl(target.getDeclaredField("beanParam"))));
        assertTrue(predicate.test(new FieldParamImpl(target.getDeclaredField("cookiePram"))));
        assertTrue(predicate.test(new FieldParamImpl(target.getDeclaredField("formParam"))));
        assertTrue(predicate.test(new FieldParamImpl(target.getDeclaredField("headerParam"))));
        assertTrue(predicate.test(new FieldParamImpl(target.getDeclaredField("matrixParam"))));
        assertTrue(predicate.test(new FieldParamImpl(target.getDeclaredField("pathParam"))));
        assertTrue(predicate.test(new FieldParamImpl(target.getDeclaredField("queryParam"))));
        assertFalse(predicate.test(new FieldParamImpl(target.getDeclaredField("simple"))));

        final Class<?> target1 = HelloDemo3.class;
        final Constructor<?> constructor1 = target1.getConstructors()[0];
        assertTrue(predicate.test(new ConstructorParamImpl(constructor1, 0)));
        assertFalse(predicate.test(new ConstructorParamImpl(constructor1, 1)));
        assertFalse(predicate.test(new ConstructorParamImpl(constructor1, 2)));
        assertFalse(predicate.test(new ConstructorParamImpl(constructor1, 3)));
        assertFalse(predicate.test(new ConstructorParamImpl(constructor1, 4)));
        assertFalse(predicate.test(new ConstructorParamImpl(constructor1, 5)));
        assertFalse(predicate.test(new ConstructorParamImpl(constructor1, 6)));
        assertFalse(predicate.test(new ConstructorParamImpl(constructor1, 7)));
        assertFalse(predicate.test(new ConstructorParamImpl(constructor1, 8)));
        assertTrue(predicate.test(new FieldParamImpl(target1.getDeclaredField("configuration"))));
        assertFalse(predicate.test(new FieldParamImpl(target1.getDeclaredField("beanParam"))));
        assertFalse(predicate.test(new FieldParamImpl(target1.getDeclaredField("cookiePram"))));
        assertFalse(predicate.test(new FieldParamImpl(target1.getDeclaredField("formParam"))));
        assertFalse(predicate.test(new FieldParamImpl(target1.getDeclaredField("headerParam"))));
        assertFalse(predicate.test(new FieldParamImpl(target1.getDeclaredField("matrixParam"))));
        assertFalse(predicate.test(new FieldParamImpl(target1.getDeclaredField("pathParam"))));
        assertFalse(predicate.test(new FieldParamImpl(target1.getDeclaredField("queryParam"))));
        assertFalse(predicate.test(new FieldParamImpl(target1.getDeclaredField("simple"))));
    }

    private static final class HelloDemo1 {

        public void demo1(String name) {

        }

    }

    @Path("/abc")
    private static final class HelloDemo2 {

        @Context
        private Configuration configuration;

        @BeanParam
        private String beanParam;

        @CookieParam("name")
        private String cookiePram;

        @FormParam("name")
        private String formParam;

        @HeaderParam("name")
        private String headerParam;

        @MatrixParam("name")
        private String matrixParam;

        @PathParam("name")
        private String pathParam;

        @QueryParam("name")
        private String queryParam;

        private String simple;

        public HelloDemo2(@Context Configuration configuration,
                          @BeanParam String beanParam,
                          @CookieParam("name") String cookiePram,
                          @FormParam("name") String formParam,
                          @HeaderParam("name") String headerParam,
                          @MatrixParam("name") String matrixParam,
                          @PathParam("name") String pathParam,
                          @QueryParam("name") String queryParam,
                          String simple) {

        }

    }

    private static final class HelloDemo3 {

        @Context
        private Configuration configuration;

        @BeanParam
        private String beanParam;

        @CookieParam("name")
        private String cookiePram;

        @FormParam("name")
        private String formParam;

        @HeaderParam("name")
        private String headerParam;

        @MatrixParam("name")
        private String matrixParam;

        @PathParam("name")
        private String pathParam;

        @QueryParam("name")
        private String queryParam;

        private String simple;

        public HelloDemo3(@Context Configuration configuration,
                          @BeanParam String beanParam,
                          @CookieParam("name") String cookiePram,
                          @FormParam("name") String formParam,
                          @HeaderParam("name") String headerParam,
                          @MatrixParam("name") String matrixParam,
                          @PathParam("name") String pathParam,
                          @QueryParam("name") String queryParam,
                          String simple) {

        }
    }

}

