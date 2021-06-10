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
package esa.restlight.springmvc.util;

import esa.restlight.core.method.HttpMethod;
import esa.restlight.core.util.Constants;
import esa.restlight.server.route.Mapping;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestMappingUtilsTest {

    @Test
    void testNormaliseDefaultValue() {
        assertNull(RequestMappingUtils.normaliseDefaultValue(null));
        assertEquals("foo", RequestMappingUtils.normaliseDefaultValue("foo"));
        assertNull(RequestMappingUtils.normaliseDefaultValue(Constants.DEFAULT_NONE));
    }

    @Test
    void testExtractMapping() throws NoSuchMethodException {
        final Optional<Mapping> mapping =
                RequestMappingUtils.extractMapping(Subject.class,
                        Subject.class.getDeclaredMethod("foo"));
        assertTrue(mapping.isPresent());
        assertEquals("foo", mapping.get().name());
        assertArrayEquals(new String[]{"/path"}, mapping.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.GET}, mapping.get().method());
        assertArrayEquals(new String[]{"a=1"}, mapping.get().params());
        assertArrayEquals(new String[]{"b=1"}, mapping.get().headers());
        assertArrayEquals(new String[]{"a/1"}, mapping.get().consumes());
        assertArrayEquals(new String[]{"b/1"}, mapping.get().produces());
    }

    @Test
    void testExtractMappingWithContextPath() throws NoSuchMethodException {
        final Optional<Mapping> mapping =
                RequestMappingUtils.extractMapping(Subject.class,
                        Subject.class.getDeclaredMethod("foo"),
                        "/foo");
        assertTrue(mapping.isPresent());
        assertEquals("foo", mapping.get().name());
        assertArrayEquals(new String[]{"/foo/path"}, mapping.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.GET}, mapping.get().method());
        assertArrayEquals(new String[]{"a=1"}, mapping.get().params());
        assertArrayEquals(new String[]{"b=1"}, mapping.get().headers());
        assertArrayEquals(new String[]{"a/1"}, mapping.get().consumes());
        assertArrayEquals(new String[]{"b/1"}, mapping.get().produces());
    }

    @Test
    void testExtractGetMapping() throws NoSuchMethodException {
        final Optional<Mapping> mapping =
                RequestMappingUtils.extractMapping(Subject.class,
                        Subject.class.getDeclaredMethod("get"));
        assertTrue(mapping.isPresent());
        assertNotNull(mapping);
        assertEquals("foo", mapping.get().name());
        assertArrayEquals(new String[]{"/path"}, mapping.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.GET}, mapping.get().method());
        assertArrayEquals(new String[]{"a=1"}, mapping.get().params());
        assertArrayEquals(new String[]{"b=1"}, mapping.get().headers());
        assertArrayEquals(new String[]{"a/1"}, mapping.get().consumes());
        assertArrayEquals(new String[]{"b/1"}, mapping.get().produces());
    }

    @Test
    void testExtractPostMapping() throws NoSuchMethodException {
        final Optional<Mapping> mapping =
                RequestMappingUtils.extractMapping(Subject.class,
                        Subject.class.getDeclaredMethod("post"));
        assertTrue(mapping.isPresent());
        assertEquals("foo", mapping.get().name());
        assertArrayEquals(new String[]{"/path"}, mapping.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.POST}, mapping.get().method());
        assertArrayEquals(new String[]{"a=1"}, mapping.get().params());
        assertArrayEquals(new String[]{"b=1"}, mapping.get().headers());
        assertArrayEquals(new String[]{"a/1"}, mapping.get().consumes());
        assertArrayEquals(new String[]{"b/1"}, mapping.get().produces());
    }

    @Test
    void testExtractDeleteMapping() throws NoSuchMethodException {
        final Optional<Mapping> mapping =
                RequestMappingUtils.extractMapping(Subject.class,
                        Subject.class.getDeclaredMethod("delete"));
        assertTrue(mapping.isPresent());
        assertEquals("foo", mapping.get().name());
        assertArrayEquals(new String[]{"/path"}, mapping.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.DELETE}, mapping.get().method());
        assertArrayEquals(new String[]{"a=1"}, mapping.get().params());
        assertArrayEquals(new String[]{"b=1"}, mapping.get().headers());
        assertArrayEquals(new String[]{"a/1"}, mapping.get().consumes());
        assertArrayEquals(new String[]{"b/1"}, mapping.get().produces());
    }

    @Test
    void testExtractPutMapping() throws NoSuchMethodException {
        final Optional<Mapping> mapping =
                RequestMappingUtils.extractMapping(Subject.class,
                        Subject.class.getDeclaredMethod("put"));
        assertTrue(mapping.isPresent());
        assertEquals("foo", mapping.get().name());
        assertArrayEquals(new String[]{"/path"}, mapping.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.PUT}, mapping.get().method());
        assertArrayEquals(new String[]{"a=1"}, mapping.get().params());
        assertArrayEquals(new String[]{"b=1"}, mapping.get().headers());
        assertArrayEquals(new String[]{"a/1"}, mapping.get().consumes());
        assertArrayEquals(new String[]{"b/1"}, mapping.get().produces());
    }

    @Test
    void testExtractPatchMapping() throws NoSuchMethodException {
        final Optional<Mapping> mapping =
                RequestMappingUtils.extractMapping(Subject.class,
                        Subject.class.getDeclaredMethod("patch"));
        assertTrue(mapping.isPresent());
        assertEquals("foo", mapping.get().name());
        assertArrayEquals(new String[]{"/path"}, mapping.get().path());
        assertArrayEquals(new HttpMethod[]{HttpMethod.PATCH}, mapping.get().method());
        assertArrayEquals(new String[]{"a=1"}, mapping.get().params());
        assertArrayEquals(new String[]{"b=1"}, mapping.get().headers());
        assertArrayEquals(new String[]{"a/1"}, mapping.get().consumes());
        assertArrayEquals(new String[]{"b/1"}, mapping.get().produces());
    }

    static class Subject {
        @RequestMapping(name = "foo",
                path = "path",
                method = RequestMethod.GET,
                params = "a=1",
                headers = "b=1",
                consumes = "a/1",
                produces = "b/1")
        public void foo() {
        }

        @GetMapping(name = "foo",
                path = "path",
                params = "a=1",
                headers = "b=1",
                consumes = "a/1",
                produces = "b/1")
        public void get() {
        }

        @PostMapping(name = "foo",
                path = "path",
                params = "a=1",
                headers = "b=1",
                consumes = "a/1",
                produces = "b/1")
        public void post() {
        }

        @DeleteMapping(name = "foo",
                path = "path",
                params = "a=1",
                headers = "b=1",
                consumes = "a/1",
                produces = "b/1")
        public void delete() {
        }

        @PutMapping(name = "foo",
                path = "path",
                params = "a=1",
                headers = "b=1",
                consumes = "a/1",
                produces = "b/1")
        public void put() {
        }

        @PatchMapping(name = "foo",
                path = "path",
                params = "a=1",
                headers = "b=1",
                consumes = "a/1",
                produces = "b/1")
        public void patch() {
        }
    }

}
