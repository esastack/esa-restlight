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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClassUtilsTest {

    @Test
    void testForName() {
        assertSame(String.class, ClassUtils.forName("java.lang.String"));
        assertSame(String.class, ClassUtils.forName("java.lang.String", false));
        assertNull(ClassUtils.forName("absent.Clz"));
    }

    @Test
    void testGetUserType() {
        assertEquals(Object.class, ClassUtils.getUserType(new Object()));
        assertEquals(Object.class, ClassUtils.getUserType(Object.class));
    }

    @Test
    void testGetParameters() throws NoSuchMethodException {
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

    private Map<String, Integer> parameters() {
        return null;
    }

    @Test
    void retrieveGenericTypes() throws NoSuchMethodException {
        assertEquals(0, ClassUtils.retrieveGenericTypes(Object.class).length);
        assertEquals(0, ClassUtils.retrieveGenericTypes(ArrayList.class).length);
        assertEquals(1, ClassUtils.retrieveGenericTypes(ClassUtilsTest.class
                .getDeclaredMethod("parameters", String.class, int.class).getGenericReturnType())
                .length);
        assertEquals(2, ClassUtils.retrieveGenericTypes(ClassUtilsTest.class
                .getDeclaredMethod("parameters").getGenericReturnType()).length);

        assertEquals(String.class, ClassUtils.retrieveGenericTypes(ClassUtilsTest.class
                .getDeclaredMethod("parameters", String.class, int.class).getGenericReturnType())[0]);
        assertEquals(String.class, ClassUtils.retrieveGenericTypes(ClassUtilsTest.class
                .getDeclaredMethod("parameters").getGenericReturnType())[0]);
        assertEquals(Integer.class, ClassUtils.retrieveGenericTypes(ClassUtilsTest.class
                .getDeclaredMethod("parameters").getGenericReturnType())[1]);

        assertTrue(ClassUtils.retrieveFirstGenericType(ClassUtilsTest.class
                .getDeclaredMethod("parameters", String.class, int.class).getGenericReturnType()).isPresent());
        assertEquals(String.class, ClassUtils.retrieveFirstGenericType(ClassUtilsTest.class
                .getDeclaredMethod("parameters", String.class, int.class).getGenericReturnType()).get());
    }

    @Test
    void retrieveTargetGenericTypes() throws NoSuchMethodException {
        assertEquals(0, ClassUtils.findGenericTypes(Object.class).length);
        assertEquals(0, ClassUtils.retrieveGenericTypes(ArrayList.class).length);
        assertEquals(1, ClassUtils.retrieveGenericTypes(ClassUtilsTest.class
                .getDeclaredMethod("parameters", String.class, int.class).getGenericReturnType())
                .length);

        assertEquals(String.class, ClassUtils.retrieveGenericTypes(ClassUtilsTest.class
                .getDeclaredMethod("parameters", String.class, int.class).getGenericReturnType())[0]);

        assertTrue(ClassUtils.retrieveFirstGenericType(ClassUtilsTest.class
                .getDeclaredMethod("parameters", String.class, int.class).getGenericReturnType()).isPresent());
        assertEquals(String.class, ClassUtils.retrieveFirstGenericType(ClassUtilsTest.class
                .getDeclaredMethod("parameters", String.class, int.class).getGenericReturnType()).get());
    }


    @Test
    void findGenericTypes() {
        assertEquals(0, ClassUtils.findGenericTypes(Object.class).length);
        assertEquals(1, ClassUtils.findGenericTypes(ArrayList.class).length);

        assertTrue(ClassUtils.findFirstGenericType(ArrayList.class).isPresent());
        assertEquals(Object.class, ClassUtils.findFirstGenericType(ArrayList.class).get());

        assertEquals(1, ClassUtils.findGenericTypes(new ArrayList<String>() {
        }.getClass())
                .length);
        assertEquals(String.class, ClassUtils.findGenericTypes(new ArrayList<String>() {
        }.getClass())[0]);
        assertTrue(ClassUtils.findFirstGenericType(new ArrayList<String>() {
        }.getClass()).isPresent());
        assertEquals(String.class, ClassUtils.findFirstGenericType(new ArrayList<String>() {
        }.getClass()).get());

        assertEquals(2, ClassUtils.findGenericTypes(new HashMap<String, Integer>() {
        }.getClass()).length);
        assertEquals(String.class, ClassUtils.findGenericTypes(new HashMap<String, Integer>() {
        }.getClass())[0]);
        assertEquals(Integer.class, ClassUtils.findGenericTypes(new HashMap<String, Integer>() {
        }.getClass())[1]);

        assertTrue(ClassUtils.findFirstGenericType(ForTest0.class).isPresent());
        assertEquals(String.class, ClassUtils.findFirstGenericType(ForTest0.class).get());

        assertTrue(ClassUtils.findFirstGenericType(ForTest1.class).isPresent());
        assertEquals(String.class, ClassUtils.findFirstGenericType(ForTest1.class).get());

        assertTrue(ClassUtils.findFirstGenericType(ForTest2.class).isPresent());
        assertEquals(String.class, ClassUtils.findFirstGenericType(ForTest2.class).get());

        assertTrue(ClassUtils.findFirstGenericType(ForTest3.class).isPresent());
        assertEquals(String.class, ClassUtils.findFirstGenericType(ForTest3.class).get());

        assertTrue(ClassUtils.findFirstGenericType(ForTest4.class).isPresent());
        assertEquals(String.class, ClassUtils.findFirstGenericType(ForTest4.class).get());

        assertTrue(ClassUtils.findFirstGenericType(ForTest5.class).isPresent());
        assertEquals(String.class, ClassUtils.findFirstGenericType(ForTest5.class).get());

        assertTrue(ClassUtils.findFirstGenericType(ForTest6.class).isPresent());
        assertEquals(String.class, ClassUtils.findFirstGenericType(ForTest6.class).get());
        assertTrue(ClassUtils.findFirstGenericType(ForTest6.class, Comparable.class).isPresent());
        assertEquals(String.class, ClassUtils.findFirstGenericType(ForTest6.class, Comparable.class).get());
        assertTrue(ClassUtils.findFirstGenericType(ForTest6.class, Iterator.class).isPresent());
        assertEquals(Integer.class, ClassUtils.findFirstGenericType(ForTest6.class, Iterator.class).get());

        assertTrue(ClassUtils.findFirstGenericType(ForTest7.class).isPresent());
        assertEquals(String.class, ClassUtils.findFirstGenericType(ForTest7.class).get());

        assertTrue(ClassUtils.findFirstGenericType(ForTest8.class).isPresent());
        assertEquals(String.class, ClassUtils.findFirstGenericType(ForTest8.class).get());

    }

    private static class ForTest0 implements Comparable<String> {

        @Override
        public int compareTo(String o) {
            return 0;
        }
    }

    private static class ForTest1 extends ForTest0 {
    }

    private static class ForTest2 extends ForTest1 {
    }

    private interface C extends Comparable<String> {
    }

    private static class ForTest3 implements C {
        @Override
        public int compareTo(String o) {
            return 0;
        }
    }

    private static class ForTest4 extends ForTest3 {
    }

    private static class ForTest5 extends ForTest3 implements Comparable<String> {
    }

    private static class ForTest6 extends ForTest3 implements Comparable<String>,
            Iterator<Integer> {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Integer next() {
            return null;
        }
    }

    private static class T<E> {
    }

    private static class ForTest7 extends T<String> {
    }

    private static class ForTest8 extends ForTest7 {
    }

    private interface A {

        default void foo(String foo) {
        }

        void bar(String bar);
    }
}
