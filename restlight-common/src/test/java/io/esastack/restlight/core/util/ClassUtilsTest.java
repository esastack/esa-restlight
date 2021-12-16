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
package io.esastack.restlight.core.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ClassUtilsTest {

    @Test
    void testGetParameterNames() throws NoSuchMethodException {
        assertArrayEquals(new String[]{"a", "b"},
                ClassUtils.getParameterNames(ClassUtilsTest.class
                        .getDeclaredMethod("parameters", String.class, int.class)));

        assertArrayEquals(new String[]{"foo"},
                ClassUtils.getParameterNames(A.class
                        .getDeclaredMethod("foo", String.class)));

        assertArrayEquals(new String[]{"arg0"},
                ClassUtils.getParameterNames(A.class
                        .getDeclaredMethod("bar", String.class)));

    }

    private List<String> parameters(String a, int b) {
        return null;
    }

    private interface A {

        default void foo(String foo) {
        }

        void bar(String bar);
    }
}
