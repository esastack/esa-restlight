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
package esa.restlight.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrimitivesTest {

    @Test
    void testIsWrapperType() {
        assertTrue(Primitives.isWrapperType(Boolean.class));
        assertTrue(Primitives.isWrapperType(Byte.class));
        assertTrue(Primitives.isWrapperType(Character.class));
        assertTrue(Primitives.isWrapperType(Short.class));
        assertTrue(Primitives.isWrapperType(Integer.class));
        assertTrue(Primitives.isWrapperType(Long.class));
        assertTrue(Primitives.isWrapperType(Double.class));
        assertTrue(Primitives.isWrapperType(Float.class));
        assertTrue(Primitives.isWrapperType(Void.class));

        assertFalse(Primitives.isWrapperType(Object.class));
        assertFalse(Primitives.isWrapperType(boolean.class));
        assertFalse(Primitives.isWrapperType(byte.class));
        assertFalse(Primitives.isWrapperType(char.class));
        assertFalse(Primitives.isWrapperType(short.class));
        assertFalse(Primitives.isWrapperType(int.class));
        assertFalse(Primitives.isWrapperType(long.class));
        assertFalse(Primitives.isWrapperType(double.class));
        assertFalse(Primitives.isWrapperType(float.class));
        assertFalse(Primitives.isWrapperType(void.class));
    }

}
