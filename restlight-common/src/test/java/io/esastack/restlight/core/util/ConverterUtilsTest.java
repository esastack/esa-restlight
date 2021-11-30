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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unchecked")
class ConverterUtilsTest {

    @Test
    void testConvertString2Primitives() {
        assertNull(ConverterUtils.str2ObjectConverter(int.class).apply(null));
        assertTrue((boolean) ConverterUtils.str2ObjectConverter(boolean.class).apply("true"));
        assertTrue((boolean) ConverterUtils.str2ObjectConverter(Boolean.class).apply("true"));
        assertEquals((byte) 10, (byte) ConverterUtils.str2ObjectConverter(byte.class).apply("10"));
        assertEquals((byte) 10, (byte) ConverterUtils.str2ObjectConverter(Byte.class).apply("10"));
        assertEquals("10".charAt(0), (char) ConverterUtils.str2ObjectConverter(char.class).apply("10"));
        assertEquals("10".charAt(0), (char) ConverterUtils.str2ObjectConverter(Character.class).apply("10"));
        assertEquals((short) 10, (short) ConverterUtils.str2ObjectConverter(short.class).apply("10"));
        assertEquals((short) 10, (short) ConverterUtils.str2ObjectConverter(Short.class).apply("10"));
        assertEquals(10, (int) ConverterUtils.str2ObjectConverter(int.class).apply("10"));
        assertEquals(10, (int) ConverterUtils.str2ObjectConverter(Integer.class).apply("10"));
        assertEquals(10L, (long) ConverterUtils.str2ObjectConverter(long.class).apply("10"));
        assertEquals(10L, (long) ConverterUtils.str2ObjectConverter(Long.class).apply("10"));
        assertEquals(10.0D, (double) ConverterUtils.str2ObjectConverter(double.class).apply("10.0"));
        assertEquals(10.0D, (double) ConverterUtils.str2ObjectConverter(Double.class).apply("10.0"));
        assertEquals(10.0f, (float) ConverterUtils.str2ObjectConverter(float.class).apply("10.0"));
        assertEquals(10.0f, (float) ConverterUtils.str2ObjectConverter(Float.class).apply("10.0"));
        assertNull(ConverterUtils.str2ObjectConverter(void.class).apply("10"));
        assertNull(ConverterUtils.str2ObjectConverter(Void.class).apply("10"));
    }

    @Test
    void testConvertString2Wrappers() {
        assertNull(ConverterUtils.str2ObjectConverter(Byte.class).apply(""));
        assertNull(ConverterUtils.str2ObjectConverter(Character.class).apply(""));
        assertNull(ConverterUtils.str2ObjectConverter(Boolean.class).apply(""));
        assertNull(ConverterUtils.str2ObjectConverter(Short.class).apply(""));
        assertNull(ConverterUtils.str2ObjectConverter(Integer.class).apply(""));
        assertNull(ConverterUtils.str2ObjectConverter(Long.class).apply(""));
        assertNull(ConverterUtils.str2ObjectConverter(Double.class).apply(""));
        assertNull(ConverterUtils.str2ObjectConverter(Float.class).apply(""));
        assertNull(ConverterUtils.str2ObjectConverter(Void.class).apply(""));
        assertNull(ConverterUtils.str2ObjectConverter(BigDecimal.class).apply(""));
        assertThrows(IllegalArgumentException.class,
                () -> ConverterUtils.str2ObjectConverter(Timestamp.class).apply(""));
        assertNull(ConverterUtils.str2ObjectConverter(String.class).apply(null));
        assertNull(ConverterUtils.str2ObjectConverter(Object.class).apply(null));
    }

    @Test
    void testConvertString2Objects() {
        assertEquals(new BigDecimal("10.0"), ConverterUtils.str2ObjectConverter(BigDecimal.class).apply("10.0"));
        assertEquals(Timestamp.valueOf("2019-12-30 19:55:01"),
                ConverterUtils.str2ObjectConverter(Timestamp.class).apply("2019-12-30 19:55:01"));

        final String str = "foo";
        assertSame(str, ConverterUtils.str2ObjectConverter(String.class).apply(str));
    }

    @Test
    void testConvertString2PrimitiveArray() {
        final String forTest = "1, 2,3,4";
        assertArrayEquals(new byte[]{1, 2, 3, 4},
                (byte[]) ConverterUtils.str2ObjectConverter(byte[].class).apply(forTest));
        assertArrayEquals(new Byte[]{1, 2, 3, 4},
                (Byte[]) ConverterUtils.str2ObjectConverter(Byte[].class).apply(forTest));
        assertArrayEquals(new Character[]{"1".charAt(0), "2".charAt(0), "3".charAt(0), "4".charAt(0)},
                (Character[]) ConverterUtils.str2ObjectConverter(Character[].class).apply(forTest));
        assertArrayEquals(new char[]{"1".charAt(0), "2".charAt(0), "3".charAt(0), "4".charAt(0)},
                (char[]) ConverterUtils.str2ObjectConverter(char[].class).apply(forTest));
        assertArrayEquals(new short[]{1, 2, 3, 4},
                (short[]) ConverterUtils.str2ObjectConverter(short[].class).apply(forTest));
        assertArrayEquals(new Short[]{1, 2, 3, 4},
                (Short[]) ConverterUtils.str2ObjectConverter(Short[].class).apply(forTest));
        assertArrayEquals(new int[]{1, 2, 3, 4},
                (int[]) ConverterUtils.str2ObjectConverter(int[].class).apply(forTest));
        assertArrayEquals(new Integer[]{1, 2, 3, 4},
                (Integer[]) ConverterUtils.str2ObjectConverter(Integer[].class).apply(forTest));
        assertArrayEquals(new long[]{1L, 2L, 3L, 4L},
                (long[]) ConverterUtils.str2ObjectConverter(long[].class).apply(forTest));
        assertArrayEquals(new Long[]{1L, 2L, 3L, 4L},
                (Long[]) ConverterUtils.str2ObjectConverter(Long[].class).apply(forTest));
        assertArrayEquals(new double[]{1D, 2D, 3D, 4D},
                (double[]) ConverterUtils.str2ObjectConverter(double[].class).apply(forTest));
        assertArrayEquals(new Double[]{1D, 2D, 3D, 4D},
                (Double[]) ConverterUtils.str2ObjectConverter(Double[].class).apply(forTest));
        assertArrayEquals(new float[]{1f, 2f, 3f, 4f},
                (float[]) ConverterUtils.str2ObjectConverter(float[].class).apply(forTest));
        assertArrayEquals(new Float[]{1f, 2f, 3f, 4f},
                (Float[]) ConverterUtils.str2ObjectConverter(Float[].class).apply(forTest));
    }

    @Test
    void testConvertString2RefArray() {
        assertArrayEquals(new String[]{"a", "b", "c", "d"},
                (String[]) ConverterUtils.str2ObjectConverter(String[].class).apply("a,b, c,d "));
    }

    @Test
    void testConvertString2Collection() throws NoSuchMethodException {
        final String forTest = "1, 2,3,4";

        final Set<Integer> setResult =
                (Set<Integer>) ConverterUtils.str2ObjectConverter(Subject.class.getDeclaredMethod("intSet")
                        .getGenericReturnType()).apply(forTest);
        assertTrue(setResult instanceof LinkedHashSet);
        assertArrayEquals(new Integer[]{1, 2, 3, 4}, setResult.toArray(new Integer[0]));

        final Collection<Short> collectionResult =
                (Collection<Short>) ConverterUtils.str2ObjectConverter(Subject.class.getDeclaredMethod(
                        "shortCollection")
                        .getGenericReturnType()).apply(forTest);
        assertTrue(collectionResult instanceof LinkedHashSet);
        assertArrayEquals(new Short[]{1, 2, 3, 4}, collectionResult.toArray(new Short[0]));

        final SortedSet<Integer> sortedSetResult =
                (SortedSet<Integer>) ConverterUtils.str2ObjectConverter(Subject.class.getDeclaredMethod("intSortedSet")
                        .getGenericReturnType()).apply(forTest);
        assertTrue(sortedSetResult instanceof TreeSet);
        assertArrayEquals(new Integer[]{1, 2, 3, 4}, sortedSetResult.toArray(new Integer[0]));

        final NavigableSet<Integer> navigableSetResult =
                (NavigableSet<Integer>) ConverterUtils.str2ObjectConverter(Subject.class
                        .getDeclaredMethod("intNavigableSet")
                        .getGenericReturnType()).apply(forTest);
        assertTrue(navigableSetResult instanceof TreeSet);
        assertArrayEquals(new Integer[]{1, 2, 3, 4}, navigableSetResult.toArray(new Integer[0]));

        final List<Long> listResult = (List<Long>) ConverterUtils.str2ObjectConverter(Subject.class
                .getDeclaredMethod("longList")
                .getGenericReturnType()).apply(forTest);
        assertTrue(listResult instanceof ArrayList);
        assertArrayEquals(new Long[]{1L, 2L, 3L, 4L}, listResult.toArray(new Long[0]));


        final LinkedList<Double> linkedListResult =
                (LinkedList<Double>) ConverterUtils.str2ObjectConverter(Subject.class
                        .getDeclaredMethod("doubleLinkedList")
                        .getGenericReturnType()).apply(forTest);
        assertArrayEquals(new Double[]{1D, 2D, 3D, 4D}, linkedListResult.toArray(new Double[0]));

        final CopyOnWriteArrayList<String> copyOnWriteArrayListResult =
                (CopyOnWriteArrayList<String>) ConverterUtils.str2ObjectConverter(CopyOnWriteArrayList.class)
                        .apply(forTest);
        assertArrayEquals(new String[]{"1", "2", "3", "4"}, copyOnWriteArrayListResult.toArray(new String[0]));

        assertNull(ConverterUtils.str2ObjectConverter(MyList.class));
    }

    @Test
    void testConvertString2Optional() throws NoSuchMethodException {
        final Optional<String> optional0 = (Optional<String>)
                ConverterUtils.str2ObjectConverter(Subject.class.getDeclaredMethod("optionalString")
                .getGenericReturnType()).apply(null);
        assertFalse(optional0.isPresent());

        final Optional<Long> optional1 = (Optional<Long>)
                ConverterUtils.str2ObjectConverter(Subject.class.getDeclaredMethod("optionalLong")
                        .getGenericReturnType()).apply(null);
        assertFalse(optional1.isPresent());

        final Optional<String[]> optional2 = (Optional<String[]>)
                ConverterUtils.str2ObjectConverter(Subject.class.getDeclaredMethod("optionalArray")
                        .getGenericReturnType()).apply(null);
        assertFalse(optional2.isPresent());

        final Optional<String[]> optional3 = (Optional<String[]>)
                ConverterUtils.str2ObjectConverter(Subject.class.getDeclaredMethod("optionalArray")
                        .getGenericReturnType()).apply("");
        assertTrue(optional3.isPresent());
        assertEquals(0, optional3.get().length);

        final Optional<Collection<String>> optional4 = (Optional<Collection<String>>)
                ConverterUtils.str2ObjectConverter(Subject.class.getDeclaredMethod("optionalCollection")
                        .getGenericReturnType()).apply(null);
        assertFalse(optional4.isPresent());

        final Optional<Collection<String>> optional5 = (Optional<Collection<String>>)
                ConverterUtils.str2ObjectConverter(Subject.class.getDeclaredMethod("optionalCollection")
                        .getGenericReturnType()).apply("");
        assertTrue(optional5.isPresent());
        assertTrue(optional5.get().isEmpty());
    }

    @Test
    void testConvertStringCollection2PrimitiveArray() {
        final List<String> forTest = Arrays.asList("1", "2", "3", "4");
        assertArrayEquals(new byte[]{1, 2, 3, 4},
                (byte[]) ConverterUtils.strs2ObjectConverter(byte[].class).apply(forTest));
        assertArrayEquals(new Byte[]{1, 2, 3, 4},
                (Byte[]) ConverterUtils.strs2ObjectConverter(Byte[].class).apply(forTest));
        assertArrayEquals(new Character[]{"1".charAt(0), "2".charAt(0), "3".charAt(0), "4".charAt(0)},
                (Character[]) ConverterUtils.strs2ObjectConverter(Character[].class).apply(forTest));
        assertArrayEquals(new char[]{"1".charAt(0), "2".charAt(0), "3".charAt(0), "4".charAt(0)},
                (char[]) ConverterUtils.strs2ObjectConverter(char[].class).apply(forTest));
        assertArrayEquals(new short[]{1, 2, 3, 4},
                (short[]) ConverterUtils.strs2ObjectConverter(short[].class).apply(forTest));
        assertArrayEquals(new Short[]{1, 2, 3, 4},
                (Short[]) ConverterUtils.strs2ObjectConverter(Short[].class).apply(forTest));
        assertArrayEquals(new int[]{1, 2, 3, 4},
                (int[]) ConverterUtils.strs2ObjectConverter(int[].class).apply(forTest));
        assertArrayEquals(new Integer[]{1, 2, 3, 4},
                (Integer[]) ConverterUtils.strs2ObjectConverter(Integer[].class).apply(forTest));
        assertArrayEquals(new long[]{1L, 2L, 3L, 4L},
                (long[]) ConverterUtils.strs2ObjectConverter(long[].class).apply(forTest));
        assertArrayEquals(new Long[]{1L, 2L, 3L, 4L},
                (Long[]) ConverterUtils.strs2ObjectConverter(Long[].class).apply(forTest));
        assertArrayEquals(new double[]{1D, 2D, 3D, 4D},
                (double[]) ConverterUtils.strs2ObjectConverter(double[].class).apply(forTest));
        assertArrayEquals(new Double[]{1D, 2D, 3D, 4D},
                (Double[]) ConverterUtils.strs2ObjectConverter(Double[].class).apply(forTest));
        assertArrayEquals(new float[]{1f, 2f, 3f, 4f},
                (float[]) ConverterUtils.strs2ObjectConverter(float[].class).apply(forTest));
        assertArrayEquals(new Float[]{1f, 2f, 3f, 4f},
                (Float[]) ConverterUtils.strs2ObjectConverter(Float[].class).apply(forTest));
    }

    private static class MyList extends ArrayList {
        private MyList(Object a) {
        }
    }

    private interface Subject {
        Collection<Short> shortCollection();

        Set<Integer> intSet();

        SortedSet<Integer> intSortedSet();

        NavigableSet<Integer> intNavigableSet();

        List<Long> longList();

        LinkedList<Double> doubleLinkedList();

        Collection<int[]> intArrayCollection();

        Optional<String> optionalString();

        Optional<Long> optionalLong();

        Optional<String[]> optionalArray();

        Optional<Collection<String>> optionalCollection();
    }

    @Test
    void testConstructor() {
        final String value = "foo";
        final Object ret = ConverterUtils.str2ObjectConverter(ConstructorSubject0.class).apply(value);
        assertNotNull(ret);
        assertTrue(ret instanceof ConstructorSubject0);
        assertEquals(value, ((ConstructorSubject0) ret).value);
    }

    @Test
    void testAbstractConstructor() {
        assertNull(ConverterUtils.str2ObjectConverter(ConstructorSubject1.class));
    }

    @Test
    void testNonePublicConstructor() {
        assertNull(ConverterUtils.str2ObjectConverter(ConstructorSubject2.class));
    }

    @Test
    void testWrongArgConstructor() {
        assertNull(ConverterUtils.str2ObjectConverter(ConstructorSubject3.class));
    }

    @Test
    void testErrorInConstructor() {
        assertThrows(IllegalStateException.class,
                () -> ConverterUtils.str2ObjectConverter(ConstructorSubject4.class).apply("foo"));
    }

    @Test
    void testValueOf() {
        final String value = "foo";
        final Object ret = ConverterUtils.str2ObjectConverter(ValueOfSubject0.class).apply(value);
        assertNotNull(ret);
        assertTrue(ret instanceof ValueOfSubject0);
        assertEquals(value, ((ValueOfSubject0) ret).value);
    }

    @Test
    void testNoneStaticValueOf() {
        assertNull(ConverterUtils.str2ObjectConverter(ValueOfSubject1.class));
    }

    @Test
    void testNonePublicValueOf() {
        assertNull(ConverterUtils.str2ObjectConverter(ValueOfSubject2.class));
    }

    @Test
    void testWrongArgValueOf() {
        assertNull(ConverterUtils.str2ObjectConverter(ValueOfSubject3.class));
    }

    @Test
    void testErrorInValueOf() {
        assertThrows(IllegalStateException.class,
                () -> ConverterUtils.str2ObjectConverter(ValueOfSubject4.class).apply("foo"));

    }

    @Test
    void testWrongReturnTypeValueOf() {
        assertNull(ConverterUtils.str2ObjectConverter(ValueOfSubject5.class));
    }


    @Test
    void testFromString() {
        final String value = "foo";
        final Object ret = ConverterUtils.str2ObjectConverter(FromStringSubject0.class).apply(value);
        assertNotNull(ret);
        assertTrue(ret instanceof FromStringSubject0);
        assertEquals(value, ((FromStringSubject0) ret).value);
    }

    @Test
    void testNoneStaticFromString() {
        assertNull(ConverterUtils.str2ObjectConverter(FromStringSubject1.class));
    }

    @Test
    void testNonePublicFromString() {
        assertNull(ConverterUtils.str2ObjectConverter(FromStringSubject2.class));
    }

    @Test
    void testWrongArgFromString() {
        assertNull(ConverterUtils.str2ObjectConverter(FromStringSubject3.class));
    }

    @Test
    void testErrorFromString() {
        assertThrows(IllegalStateException.class,
                () -> ConverterUtils.str2ObjectConverter(FromStringSubject4.class).apply("foo"));
    }

    @Test
    void testWrongReturnTypeFromString() {
        assertNull(ConverterUtils.str2ObjectConverter(FromStringSubject5.class));
    }

    @Test
    void testForceConvertStringValue() {
        assertNull(ConverterUtils.forceConvertStringValue(null, Integer.class));
        assertNull(ConverterUtils.forceConvertStringValue("", Integer.class));
        assertThrows(IllegalArgumentException.class,
                () -> ConverterUtils.forceConvertStringValue("foo", MyList.class));

        assertNull(ConverterUtils.forceConvertStringValue(null, Collection.class));
        assertNull(ConverterUtils.forceConvertStringValue(null, String[].class));
        assertNotNull(ConverterUtils.forceConvertStringValue("", Collection.class));
        assertNotNull(ConverterUtils.forceConvertStringValue("", String[].class));
    }

    @Test
    void testForceConvertStringValueWithWrongType() {
        assertThrows(IllegalArgumentException.class,
                () -> ConverterUtils.forceConvertStringValue("1", FromStringSubject5.class));
    }

    @Test
    void testStandardContextPath() {
        assertEquals("", ConverterUtils.standardContextPath(null));
        assertEquals("/abc", ConverterUtils.standardContextPath("/abc"));
        assertEquals("/abc", ConverterUtils.standardContextPath("abc"));
        assertEquals("/abc", ConverterUtils.standardContextPath("/abc/"));
    }

    @Test
    void testNormaliseDefaultValue() {
        assertNull(ConverterUtils.normaliseDefaultValue(null));
        assertEquals("", ConverterUtils.normaliseDefaultValue(""));
        assertEquals("abc", ConverterUtils.normaliseDefaultValue("abc"));
        assertNull(ConverterUtils.normaliseDefaultValue(Constants.DEFAULT_NONE));
    }

    private static class ConstructorSubject0 {

        private final String value;

        public ConstructorSubject0(String value) {
            this.value = value;
        }
    }

    private abstract static class ConstructorSubject1 {
        public ConstructorSubject1(String value) {
        }
    }

    private abstract static class ConstructorSubject2 {
        ConstructorSubject2(String value) {
        }
    }

    private static class ConstructorSubject3 {
        public ConstructorSubject3(String value, int a) {
        }
    }

    private static class ConstructorSubject4 {
        public ConstructorSubject4(String value) {
            if (value != null) {
                throw new IllegalArgumentException();
            }
        }
    }

    private static class ValueOfSubject0 {
        private String value;

        public static ValueOfSubject0 valueOf(String value) {
            ValueOfSubject0 in = new ValueOfSubject0();
            in.value = value;
            return in;
        }
    }

    private static class ValueOfSubject1 {

        public ValueOfSubject1 valueOf(String value) {
            return null;
        }
    }

    private static class ValueOfSubject2 {

        static ValueOfSubject2 valueOf(String value) {
            return null;
        }
    }

    private static class ValueOfSubject3 {

        public static ValueOfSubject3 valueOf(String value, int a) {
            return null;
        }
    }

    private static class ValueOfSubject4 {

        public static ValueOfSubject4 valueOf(String value) {
            if (value != null) {
                throw new IllegalArgumentException();
            }
            return null;
        }
    }

    private static class ValueOfSubject5 {

        public static ValueOfSubject4 valueOf(String value) {
            return null;
        }
    }

    private static class FromStringSubject0 {
        private String value;

        public static FromStringSubject0 fromString(String value) {
            FromStringSubject0 in = new FromStringSubject0();
            in.value = value;
            return in;
        }
    }

    private static class FromStringSubject1 {

        public FromStringSubject1 fromString(String value) {
            return new FromStringSubject1();
        }
    }

    private static class FromStringSubject2 {

        static FromStringSubject2 fromString(String value) {
            return new FromStringSubject2();
        }
    }

    private static class FromStringSubject3 {

        public static FromStringSubject3 fromString(String value, int a) {
            return new FromStringSubject3();
        }
    }

    private static class FromStringSubject4 {

        public static FromStringSubject4 fromString(String value) {
            if (value != null) {
                throw new IllegalArgumentException();
            }
            return null;
        }
    }

    private static class FromStringSubject5 {

        public static FromStringSubject4 fromString(String value) {
            return null;
        }
    }
}
