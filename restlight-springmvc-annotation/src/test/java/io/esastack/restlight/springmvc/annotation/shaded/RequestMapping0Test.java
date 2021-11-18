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
package io.esastack.restlight.springmvc.annotation.shaded;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequestMapping0Test {

    @Test
    void testShadedClass() {
        assertEquals(RequestMapping.class, RequestMapping0.shadedClass());
    }

    @Test
    void testExtendedClasses() {
        List<Class<Annotation>> annos = Arrays.asList(RequestMapping0.extendedClasses());
        assertEquals(5, annos.size());
        assertTrue(annos.contains(GetMapping.class));
        assertTrue(annos.contains(PostMapping.class));
        assertTrue(annos.contains(PutMapping.class));
        assertTrue(annos.contains(DeleteMapping.class));
        assertTrue(annos.contains(PatchMapping.class));
    }

    @Test
    void testFromShade() throws NoSuchMethodException {
        assertNull(RequestMapping0.fromShade(null));

        final RequestMapping0 mapping0 = RequestMapping0.fromShade(Subject.class.getDeclaredMethod("method0")
                .getAnnotation(RequestMapping.class));
        assertEquals("method0", mapping0.name());
        assertEquals(1, mapping0.value().length);
        assertEquals("/method0", mapping0.value()[0]);
        assertEquals(1, mapping0.headers().length);
        assertEquals("content-type=text/*", mapping0.headers()[0]);
        assertEquals(1, mapping0.params().length);
        assertEquals("myParam!=myValue", mapping0.params()[0]);
        assertEquals(2, mapping0.consumes().length);
        assertEquals("text/plain", mapping0.consumes()[0]);
        assertEquals("application/*", mapping0.consumes()[1]);
        assertEquals(2, mapping0.produces().length);
        assertEquals("text/plain", mapping0.produces()[0]);
        assertEquals("application/*", mapping0.produces()[1]);
        assertEquals(0, mapping0.method().length);

        final RequestMapping0 mapping1 = RequestMapping0.fromShade(Subject.class.getDeclaredMethod("method1")
                .getAnnotation(GetMapping.class));
        assertEquals("method1", mapping1.name());
        assertEquals(1, mapping1.value().length);
        assertEquals("/method1", mapping1.value()[0]);
        assertEquals(1, mapping1.headers().length);
        assertEquals("content-type=text/*", mapping1.headers()[0]);
        assertEquals(1, mapping1.params().length);
        assertEquals("myParam!=myValue", mapping1.params()[0]);
        assertEquals(2, mapping1.consumes().length);
        assertEquals("text/plain", mapping1.consumes()[0]);
        assertEquals("application/*", mapping1.consumes()[1]);
        assertEquals(2, mapping1.produces().length);
        assertEquals("text/plain", mapping1.produces()[0]);
        assertEquals("application/*", mapping1.produces()[1]);
        assertEquals(1, mapping1.method().length);
        assertEquals(RequestMethod.GET.name(), mapping1.method()[0]);

        final RequestMapping0 mapping2 = RequestMapping0.fromShade(Subject.class.getDeclaredMethod("method2")
                .getAnnotation(PostMapping.class));
        assertEquals("method2", mapping2.name());
        assertEquals(1, mapping2.value().length);
        assertEquals("/method2", mapping2.value()[0]);
        assertEquals(1, mapping2.headers().length);
        assertEquals("content-type=text/*", mapping2.headers()[0]);
        assertEquals(1, mapping2.params().length);
        assertEquals("myParam!=myValue", mapping2.params()[0]);
        assertEquals(2, mapping2.consumes().length);
        assertEquals("text/plain", mapping2.consumes()[0]);
        assertEquals("application/*", mapping2.consumes()[1]);
        assertEquals(2, mapping2.produces().length);
        assertEquals("text/plain", mapping2.produces()[0]);
        assertEquals("application/*", mapping2.produces()[1]);
        assertEquals(1, mapping2.method().length);
        assertEquals(RequestMethod.POST.name(), mapping2.method()[0]);

        final RequestMapping0 mapping3 = RequestMapping0.fromShade(Subject.class.getDeclaredMethod("method3")
                .getAnnotation(PutMapping.class));
        assertEquals("method3", mapping3.name());
        assertEquals(1, mapping3.value().length);
        assertEquals("/method3", mapping3.value()[0]);
        assertEquals(1, mapping3.headers().length);
        assertEquals("content-type=text/*", mapping3.headers()[0]);
        assertEquals(1, mapping3.params().length);
        assertEquals("myParam!=myValue", mapping3.params()[0]);
        assertEquals(2, mapping3.consumes().length);
        assertEquals("text/plain", mapping3.consumes()[0]);
        assertEquals("application/*", mapping3.consumes()[1]);
        assertEquals(2, mapping3.produces().length);
        assertEquals("text/plain", mapping3.produces()[0]);
        assertEquals("application/*", mapping3.produces()[1]);
        assertEquals(1, mapping3.method().length);
        assertEquals(RequestMethod.PUT.name(), mapping3.method()[0]);

        final RequestMapping0 mapping4 = RequestMapping0.fromShade(Subject.class.getDeclaredMethod("method4")
                .getAnnotation(DeleteMapping.class));
        assertEquals("method4", mapping4.name());
        assertEquals(1, mapping4.value().length);
        assertEquals("/method4", mapping4.value()[0]);
        assertEquals(1, mapping4.headers().length);
        assertEquals("content-type=text/*", mapping4.headers()[0]);
        assertEquals(1, mapping4.params().length);
        assertEquals("myParam!=myValue", mapping4.params()[0]);
        assertEquals(2, mapping4.consumes().length);
        assertEquals("text/plain", mapping4.consumes()[0]);
        assertEquals("application/*", mapping4.consumes()[1]);
        assertEquals(2, mapping4.produces().length);
        assertEquals("text/plain", mapping4.produces()[0]);
        assertEquals("application/*", mapping4.produces()[1]);
        assertEquals(1, mapping4.method().length);
        assertEquals(RequestMethod.DELETE.name(), mapping4.method()[0]);

        final RequestMapping0 mapping5 = RequestMapping0.fromShade(Subject.class.getDeclaredMethod("method5")
                .getAnnotation(PatchMapping.class));
        assertEquals("method5", mapping5.name());
        assertEquals(1, mapping5.value().length);
        assertEquals("/method5", mapping5.value()[0]);
        assertEquals(1, mapping5.headers().length);
        assertEquals("content-type=text/*", mapping5.headers()[0]);
        assertEquals(1, mapping5.params().length);
        assertEquals("myParam!=myValue", mapping5.params()[0]);
        assertEquals(2, mapping5.consumes().length);
        assertEquals("text/plain", mapping5.consumes()[0]);
        assertEquals("application/*", mapping5.consumes()[1]);
        assertEquals(2, mapping5.produces().length);
        assertEquals("text/plain", mapping5.produces()[0]);
        assertEquals("application/*", mapping5.produces()[1]);
        assertEquals(1, mapping5.method().length);
        assertEquals(RequestMethod.PATCH.name(), mapping5.method()[0]);
    }

    @Test
    void testError() {
        assertThrows(IllegalArgumentException.class,
                () -> RequestMapping0.fromShade(Subject.class
                        .getDeclaredMethod("method6")
                        .getAnnotation(ResponseBody.class)));
    }

    private static class Subject {

        @RequestMapping(name = "method0", value = "/method0", headers = "content-type=text/*",
                params = "myParam!=myValue", consumes = {"text/plain", "application/*"},
                produces = {"text/plain", "application/*"})
        private void method0() {
        }

        @GetMapping(name = "method1", value = "/method1", headers = "content-type=text/*",
                params = "myParam!=myValue", consumes = {"text/plain", "application/*"},
                produces = {"text/plain", "application/*"})
        private void method1() {
        }

        @PostMapping(name = "method2", value = "/method2", headers = "content-type=text/*",
                params = "myParam!=myValue", consumes = {"text/plain", "application/*"},
                produces = {"text/plain", "application/*"})
        private void method2() {
        }

        @PutMapping(name = "method3", value = "/method3", headers = "content-type=text/*",
                params = "myParam!=myValue", consumes = {"text/plain", "application/*"},
                produces = {"text/plain", "application/*"})
        private void method3() {
        }

        @DeleteMapping(name = "method4", value = "/method4", headers = "content-type=text/*",
                params = "myParam!=myValue", consumes = {"text/plain", "application/*"},
                produces = {"text/plain", "application/*"})
        private void method4() {
        }

        @PatchMapping(name = "method5", value = "/method5", headers = "content-type=text/*",
                params = "myParam!=myValue", consumes = {"text/plain", "application/*"},
                produces = {"text/plain", "application/*"})
        private void method5() {
        }

        @ResponseBody
        private void method6() {
        }
    }
}
