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
package esa.restlight.jaxrs.util;

import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.HttpMethod;
import esa.restlight.server.route.Mapping;
import org.junit.jupiter.api.Test;

import javax.ws.rs.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JaxrsMappingUtilsTest {

    @Test
    void testExtractDefaultValue() throws NoSuchMethodException {
        assertNull(JaxrsMappingUtils.extractDefaultValue(null));
        final HandlerMethod handlerMethod = HandlerMethod.of(Subject4DefaultValue.class
                        .getDeclaredMethod("method1", String.class, String.class, String.class),
                new Subject4DefaultValue());
        assertNull(JaxrsMappingUtils.extractDefaultValue(handlerMethod.parameters()[0]));
        assertEquals("", JaxrsMappingUtils.extractDefaultValue(handlerMethod.parameters()[1]));
        assertEquals("foo", JaxrsMappingUtils.extractDefaultValue(handlerMethod.parameters()[2]));
    }


    @Test
    void testExtractGetMapping() throws NoSuchMethodException {
        final Optional<Mapping> mapping =
                JaxrsMappingUtils.extractMapping(Subject.class,
                        Subject.class.getDeclaredMethod("get"));
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
                        Subject.class.getDeclaredMethod("post"));
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
                        Subject.class.getDeclaredMethod("delete"));
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
                        Subject.class.getDeclaredMethod("put"));
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
                        Subject.class.getDeclaredMethod("patch"));
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
                        Subject.class.getDeclaredMethod("options"));
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
                        Subject.class.getDeclaredMethod("head"));
        assertTrue(mapping.isPresent());
        assertNull(mapping.get().name());
        assertArrayEquals(new String[]{"/path"}, mapping.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.HEAD}, mapping.get().method());
        assertArrayEquals(new String[]{"a/1"}, mapping.get().consumes());
        assertArrayEquals(new String[]{"b/1"}, mapping.get().produces());
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
    }

    private static class Subject4DefaultValue {

        void method1(String p0,
                     @DefaultValue("") String p1,
                     @DefaultValue("foo") String p2) {
        }
    }

}
