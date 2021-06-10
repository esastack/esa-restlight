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
import static org.junit.jupiter.api.Assertions.assertNull;
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
        assertThrows(IllegalArgumentException.class,
                () -> AliasUtils.getStringArrayFromValueAlias(attrs, null, name));
        assertThrows(IllegalArgumentException.class,
                () -> AliasUtils.getStringArrayFromValueAlias(null, paths1, name));

        final boolean[] a1 = new boolean[] {true, false};
        final boolean[] b1 = new boolean[] {true, false};
        assertArrayEquals(a1, AliasUtils.getFromValueAlias(a1, b1, b1, name));

        final byte[] a2 = new byte[] {(byte) 1, (byte) 1};
        final byte[] b2 = new byte[] {(byte) 1, (byte) 1};
        assertArrayEquals(a2, AliasUtils.getFromValueAlias(a2, b2, b2, name));

        final char[] a3 = new char[] {'1', '2'};
        final char[] b3 = new char[] {'1', '2'};
        assertArrayEquals(a3, AliasUtils.getFromValueAlias(a3, b3, b3, name));

        final double[] a4 = new double[] {1d, 2d};
        final double[] b4 = new double[] {1d, 2d};
        assertArrayEquals(a4, AliasUtils.getFromValueAlias(a4, b4, b4, name));

        final float[] a5 = new float[] {1f, 2f};
        final float[] b5 = new float[] {1f, 2f};
        assertArrayEquals(a5, AliasUtils.getFromValueAlias(a5, b5, b5, name));

        final int[] a6 = new int[] {1, 2};
        final int[] b6 = new int[] {1, 2};
        assertArrayEquals(a6, AliasUtils.getFromValueAlias(a6, b6, b6, name));

        final long[] a7 = new long[] {1L, 2L};
        final long[] b7 = new long[] {1L, 2L};
        assertArrayEquals(a7, AliasUtils.getFromValueAlias(a7, b7, b7, name));

        final short[] a8 = new short[] {1, 2};
        final short[] b8 = new short[] {1, 2};
        assertArrayEquals(a8, AliasUtils.getFromValueAlias(a8, b8, b8, name));
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
        assertNull(AliasUtils.getAnnotationDefaultValue(null, "code"));
        assertNull(AliasUtils.getAnnotationDefaultValue(Response.class, null));
    }

    public @interface Response {
        HttpStatus code() default HttpStatus.INTERNAL_SERVER_ERROR;
    }
}

