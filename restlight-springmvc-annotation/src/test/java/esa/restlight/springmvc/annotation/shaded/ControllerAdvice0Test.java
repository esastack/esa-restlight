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
package esa.restlight.springmvc.annotation.shaded;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ControllerAdvice0Test {

    @Test
    void testShadedClass() {
        assertEquals(ControllerAdvice.class, ControllerAdvice0.shadedClass());
    }

    @Test
    void testExtendedClasses() {
        assertEquals(1, ControllerAdvice0.extendedClasses().length);
        assertEquals(RestControllerAdvice.class, ControllerAdvice0.extendedClasses()[0]);
    }

    @Test
    void testFromShade() {
        assertNull(ControllerAdvice0.fromShade(null));

        // ControllerAdvice
        ControllerAdvice0 advice0 = ControllerAdvice0.fromShade(Subject0.class.getAnnotation(ControllerAdvice.class));
        assertEquals(2, advice0.basePackages().length);
        assertEquals("abc", advice0.basePackages()[0]);
        assertEquals("def", advice0.basePackages()[1]);

        assertEquals(1, advice0.basePackageClasses().length);
        assertEquals(ControllerAdvice0Test.class, advice0.basePackageClasses()[0]);

        assertEquals(2, advice0.annotations().length);
        assertEquals(RestController.class, advice0.annotations()[0]);
        assertEquals(RequestBody.class, advice0.annotations()[1]);

        assertEquals(2, advice0.assignableTypes().length);
        assertEquals(RequestBody.class, advice0.assignableTypes()[0]);
        assertEquals(RequestHeader.class, advice0.assignableTypes()[1]);

        // RestControllerAdvice
        advice0 = ControllerAdvice0.fromShade(Subject1.class.getAnnotation(RestControllerAdvice.class));
        assertEquals(2, advice0.basePackages().length);
        assertEquals("abc", advice0.basePackages()[0]);
        assertEquals("def", advice0.basePackages()[1]);

        assertEquals(1, advice0.basePackageClasses().length);
        assertEquals(ControllerAdvice0Test.class, advice0.basePackageClasses()[0]);

        assertEquals(2, advice0.annotations().length);
        assertEquals(RestController.class, advice0.annotations()[0]);
        assertEquals(RequestBody.class, advice0.annotations()[1]);

        assertEquals(2, advice0.assignableTypes().length);
        assertEquals(RequestBody.class, advice0.assignableTypes()[0]);
    }

    @Test
    void testError() {
        assertThrows(IllegalArgumentException.class,
                () -> ControllerAdvice0.fromShade(Subject2.class.getAnnotation(RestController.class)));
    }

    @ControllerAdvice(basePackages = {"abc", "def"}, basePackageClasses = ControllerAdvice0Test.class,
            annotations = {RestController.class, RequestBody.class},
            assignableTypes = {RequestBody.class, RequestHeader.class})
    private static class Subject0 {
    }

    @RestControllerAdvice(basePackages = {"abc", "def"}, basePackageClasses = ControllerAdvice0Test.class,
            annotations = {RestController.class, RequestBody.class},
            assignableTypes = {RequestBody.class, RequestHeader.class})
    private static class Subject1 {

    }

    @RestController
    private static class Subject2 {

    }
}
