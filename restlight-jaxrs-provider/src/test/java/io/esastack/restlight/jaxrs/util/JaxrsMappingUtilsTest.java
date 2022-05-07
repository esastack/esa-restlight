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
package io.esastack.restlight.jaxrs.util;

import esa.commons.StringUtils;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.HandlerMethodImpl;
import io.esastack.restlight.core.route.Mapping;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JaxrsMappingUtilsTest {

    @Test
    void testExtractDefaultValue() throws NoSuchMethodException {
        assertNull(JaxrsMappingUtils.extractDefaultValue(null));
        final HandlerMethod handlerMethod = HandlerMethodImpl.of(Subject4DefaultValue.class,
                Subject4DefaultValue.class.getDeclaredMethod("method1", String.class,
                        String.class, String.class));
        assertNull(JaxrsMappingUtils.extractDefaultValue(handlerMethod.parameters()[0]));
        assertEquals("", JaxrsMappingUtils.extractDefaultValue(handlerMethod.parameters()[1]));
        assertEquals("foo", JaxrsMappingUtils.extractDefaultValue(handlerMethod.parameters()[2]));
    }

    @Test
    void testExtractGetMapping() throws NoSuchMethodException {
        assertFalse(JaxrsMappingUtils.extractMapping(Subject.class, null, StringUtils.empty()).isPresent());
        assertFalse(JaxrsMappingUtils.extractMapping(null, Subject.class.getDeclaredMethod("get"),
                StringUtils.empty()).isPresent());
        final Optional<Mapping> mapping =
                JaxrsMappingUtils.extractMapping(Subject.class,
                        Subject.class.getDeclaredMethod("get"),
                        StringUtils.empty());
        assertTrue(mapping.isPresent());
        assertNull(mapping.get().name());
        assertArrayEquals(new String[]{"/path"}, mapping.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.GET}, mapping.get().method());
        assertArrayEquals(new String[]{"a/1"}, mapping.get().consumes());
        assertArrayEquals(new String[]{"b/1"}, mapping.get().produces());
    }

    @Test
    void testExtractGetMappingWithContextPath() throws NoSuchMethodException {
        final Optional<Mapping> mapping =
                JaxrsMappingUtils.extractMapping(Subject.class,
                        Subject.class.getDeclaredMethod("get"), "foo");
        assertTrue(mapping.isPresent());
        assertNull(mapping.get().name());
        assertArrayEquals(new String[]{"/foo/path"}, mapping.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.GET}, mapping.get().method());
        assertArrayEquals(new String[]{"a/1"}, mapping.get().consumes());
        assertArrayEquals(new String[]{"b/1"}, mapping.get().produces());
    }

    @Test
    void testExtractPostMapping() throws NoSuchMethodException {
        final Optional<Mapping> mapping =
                JaxrsMappingUtils.extractMapping(Subject.class,
                        Subject.class.getDeclaredMethod("post"),
                        StringUtils.empty());
        assertTrue(mapping.isPresent());
        assertNull(mapping.get().name());
        assertArrayEquals(new String[]{"/path"}, mapping.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.POST}, mapping.get().method());
        assertArrayEquals(new String[]{"a/1"}, mapping.get().consumes());
        assertArrayEquals(new String[]{"b/1"}, mapping.get().produces());
    }

    @Test
    void testExtractDeleteMapping() throws NoSuchMethodException {
        final Optional<Mapping> mapping =
                JaxrsMappingUtils.extractMapping(Subject.class,
                        Subject.class.getDeclaredMethod("delete"),
                        StringUtils.empty());
        assertTrue(mapping.isPresent());
        assertNull(mapping.get().name());
        assertArrayEquals(new String[]{"/path"}, mapping.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.DELETE}, mapping.get().method());
        assertArrayEquals(new String[]{"a/1"}, mapping.get().consumes());
        assertArrayEquals(new String[]{"b/1"}, mapping.get().produces());
    }

    @Test
    void testExtractPutMapping() throws NoSuchMethodException {
        final Optional<Mapping> mapping =
                JaxrsMappingUtils.extractMapping(Subject.class,
                        Subject.class.getDeclaredMethod("put"),
                        StringUtils.empty());
        assertTrue(mapping.isPresent());
        assertNull(mapping.get().name());
        assertArrayEquals(new String[]{"/path"}, mapping.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.PUT}, mapping.get().method());
        assertArrayEquals(new String[]{"a/1"}, mapping.get().consumes());
        assertArrayEquals(new String[]{"b/1"}, mapping.get().produces());
    }

    @Test
    void testExtractPatchMapping() throws NoSuchMethodException {
        final Optional<Mapping> mapping =
                JaxrsMappingUtils.extractMapping(Subject.class,
                        Subject.class.getDeclaredMethod("patch"),
                        StringUtils.empty());
        assertTrue(mapping.isPresent());
        assertNull(mapping.get().name());
        assertArrayEquals(new String[]{"/path"}, mapping.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.PATCH}, mapping.get().method());
        assertArrayEquals(new String[]{"a/1"}, mapping.get().consumes());
        assertArrayEquals(new String[]{"b/1"}, mapping.get().produces());
    }

    @Test
    void testExtractOptionsMapping() throws NoSuchMethodException {
        final Optional<Mapping> mapping =
                JaxrsMappingUtils.extractMapping(Subject.class,
                        Subject.class.getDeclaredMethod("options"),
                        StringUtils.empty());
        assertTrue(mapping.isPresent());
        assertNull(mapping.get().name());
        assertArrayEquals(new String[]{"/path"}, mapping.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.OPTIONS}, mapping.get().method());
        assertArrayEquals(new String[]{"a/1"}, mapping.get().consumes());
        assertArrayEquals(new String[]{"b/1"}, mapping.get().produces());
    }

    @Test
    void testExtractHeadMapping() throws NoSuchMethodException {
        final Optional<Mapping> mapping =
                JaxrsMappingUtils.extractMapping(Subject.class,
                        Subject.class.getDeclaredMethod("head"),
                        StringUtils.empty());
        assertTrue(mapping.isPresent());
        assertNull(mapping.get().name());
        assertArrayEquals(new String[]{"/path"}, mapping.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.HEAD}, mapping.get().method());
        assertArrayEquals(new String[]{"a/1"}, mapping.get().consumes());
        assertArrayEquals(new String[]{"b/1"}, mapping.get().produces());
    }

    @Test
    void testExtractOverriddenMapping() throws NoSuchMethodException {
        Optional<Mapping> mapping0 = JaxrsMappingUtils.extractMapping(Subject0.class,
                Subject0.class.getDeclaredMethod("sup"),
                StringUtils.empty());
        assertTrue(mapping0.isPresent());
        assertNull(mapping0.get().name());
        assertArrayEquals(new String[]{"/sub/path1"}, mapping0.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.POST}, mapping0.get().method());
        assertArrayEquals(new String[]{"x/1"}, mapping0.get().consumes());
        assertArrayEquals(new String[]{"y/1"}, mapping0.get().produces());

        Optional<Mapping> mapping1 = JaxrsMappingUtils.extractMapping(Subject1.class,
                Subject1.class.getDeclaredMethod("sup"),
                StringUtils.empty());
        assertTrue(mapping1.isPresent());
        assertNull(mapping1.get().name());
        assertArrayEquals(new String[]{"/sub/path1"}, mapping1.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.POST}, mapping1.get().method());
        assertArrayEquals(new String[]{"x/1"}, mapping1.get().consumes());
        assertArrayEquals(new String[]{"y/1"}, mapping1.get().produces());
    }

    @Test
    void testExtractImplementedMapping() throws NoSuchMethodException {
        Optional<Mapping> mapping0 = JaxrsMappingUtils.extractMapping(Sub.class,
                Sub.class.getDeclaredMethod("sup0"),
                StringUtils.empty());
        assertTrue(mapping0.isPresent());
        assertNull(mapping0.get().name());
        assertArrayEquals(new String[]{"/sub"}, mapping0.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.GET}, mapping0.get().method());
        assertArrayEquals(new String[]{"x/1"}, mapping0.get().consumes());
        assertArrayEquals(new String[]{"y/1"}, mapping0.get().produces());

        Optional<Mapping> mapping1 = JaxrsMappingUtils.extractMapping(Sub.class,
                Sup.class.getDeclaredMethod("sup1"),
                StringUtils.empty());
        assertTrue(mapping1.isPresent());
        assertNull(mapping1.get().name());
        assertArrayEquals(new String[]{"/sub/path2"}, mapping1.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.POST}, mapping1.get().method());
        assertArrayEquals(new String[]{"x1/1"}, mapping1.get().consumes());
        assertArrayEquals(new String[]{"y1/1"}, mapping1.get().produces());
    }

    static class Subject {
        @GET
        @Path("path")
        @Consumes("a/1")
        @Produces("b/1")
        public void get() {
        }

        @POST
        @Path("path")
        @Consumes("a/1")
        @Produces("b/1")
        public void post() {
        }

        @DELETE
        @Path("path")
        @Consumes("a/1")
        @Produces("b/1")
        public void delete() {
        }

        @PUT
        @Path("path")
        @Consumes("a/1")
        @Produces("b/1")
        public void put() {
        }

        @PATCH
        @Path("path")
        @Consumes("a/1")
        @Produces("b/1")
        public void patch() {
        }

        @OPTIONS
        @Path("path")
        @Consumes("a/1")
        @Produces("b/1")
        public void options() {
        }

        @HEAD
        @Path("path")
        @Consumes("a/1")
        @Produces("b/1")
        public void head() {
        }

        @GET
        @Path("path")
        @Consumes("a/1")
        @Produces("b/1")
        public void sup() {
        }
    }

    @Consumes("x1/1")
    @Produces("y1/1")
    @Path("/sub")
    static class Subject0 extends Subject {

        @POST
        @Path("path1")
        @Consumes("x/1")
        @Produces("y/1")
        @Override
        public void sup() {
            super.sup();
        }
    }

    static class Subject1 extends Subject0 {

        @Override
        public void sup() {
            super.sup();
        }
    }

    @Consumes("x/1")
    @Produces("y/1")
    @Path("/sub")
    interface Sup {

        @POST
        @Path("path1")
        @Consumes("x0/1")
        @Produces("y0/1")
        void sup0();

        @POST
        @Path("path2")
        @Consumes("x1/1")
        @Produces("y1/1")
        void sup1();
    }

    static class Sub implements Sup {

        @GET
        @Override
        public void sup0() {

        }

        @Override
        public void sup1() {

        }
    }

    private static class Subject4DefaultValue {

        void method1(String p0,
                     @DefaultValue("") String p1,
                     @DefaultValue("foo") String p2) {
        }
    }

}

