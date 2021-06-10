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
package esa.restlight.springmvc.annotation.shaded;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AliasUtilsTest {

    @Test
    void testGetStringArrayFromValueAlias() {
        final String[] attrs = new String[] {"/abc", "/def"};
        final String[] paths0 = new String[] {"/abc", "/def"};
        final String[] paths1 = new String[] {"/def", "/abc"};
        final String name = "path";

        assertArrayEquals(attrs, AliasUtils.getStringArrayFromValueAlias(attrs, paths0, name));
        assertThrows(IllegalArgumentException.class,
                () -> AliasUtils.getStringArrayFromValueAlias(attrs, paths1, name));
    }

    @Test
    void testGetNamedStringFromValueAlias() {
        assertEquals("name", AliasUtils.getNamedStringFromValueAlias("name", "name"));
        assertThrows(IllegalArgumentException.class,
                () -> AliasUtils.getNamedStringFromValueAlias("name", "name0"));
    }

    @Test
    void testGetAnnotationDefaultValue() {
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                AliasUtils.getAnnotationDefaultValue(Response.class, "code"));
    }

    public @interface Response {
        HttpStatus code() default HttpStatus.INTERNAL_SERVER_ERROR;
    }
}

