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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
class ConverterUtilsTest {

    @Test
    void testPrimitives() {
        String forTest = "10";
        final int intResult = (int) ConverterUtils.convertIfNecessary(forTest, int.class);
        assertEquals(10, intResult);

        final Integer integerResult = (Integer) ConverterUtils.convertIfNecessary(forTest, Integer.class);
        assertEquals(Integer.valueOf(forTest), integerResult);

        final short shortResult = (short) ConverterUtils.convertIfNecessary(forTest, short.class);
        assertEquals(10, shortResult);

        final Short aShortResult = (Short) ConverterUtils.convertIfNecessary(forTest, Short.class);
        assertEquals(Short.valueOf(forTest), aShortResult);

        final long longResult = (long) ConverterUtils.convertIfNecessary(forTest, long.class);
        assertEquals(10L, longResult);

        final Long aLongResult = (Long) ConverterUtils.convertIfNecessary(forTest, Long.class);
        assertEquals(Long.valueOf(forTest), aLongResult);

        final byte byteResult = (byte) ConverterUtils.convertIfNecessary(forTest, byte.class);
        assertEquals(10, byteResult);

        final Byte aByteResult = (Byte) ConverterUtils.convertIfNecessary(forTest, Byte.class);
        assertEquals(Byte.valueOf(forTest), aByteResult);

        final char charResult = (char) ConverterUtils.convertIfNecessary(forTest, char.class);
        assertEquals(forTest.charAt(0), charResult);

        final Character aCharResult = (Character) ConverterUtils.convertIfNecessary(forTest, Character.class);
        assertEquals(Character.valueOf(forTest.charAt(0)), aCharResult);

        forTest = "true";
        final boolean boolResult = (boolean) ConverterUtils.convertIfNecessary(forTest, boolean.class);
        assertTrue(boolResult);

        final Boolean aBooleanResult = (Boolean) ConverterUtils.convertIfNecessary(forTest, Boolean.class);
        assertEquals(Boolean.valueOf(forTest), aBooleanResult);

        forTest = "1.11";
        final double doubleResult = (double) ConverterUtils.convertIfNecessary(forTest, double.class);
        assertEquals(1.11D, doubleResult, 0.00);

        final Double aDoubleResult = (Double) ConverterUtils.convertIfNecessary(forTest, Double.class);
        assertEquals(Double.valueOf(forTest), aDoubleResult);

        final float floatResult = (float) ConverterUtils.convertIfNecessary(forTest, float.class);
        assertEquals(1.11f, floatResult, 0.00);

        final Float aFloatResult = (Float) ConverterUtils.convertIfNecessary(forTest, Float.class);
        assertEquals(Float.valueOf(forTest), aFloatResult);

        final BigDecimal bigDecimalResult = (BigDecimal) ConverterUtils.converter(BigDecimal.class).apply(forTest);
        assertEquals(new BigDecimal(forTest), bigDecimalResult);

        forTest = "2019-12-30 19:55:01";
        final Timestamp timestampResult =
                (Timestamp) ConverterUtils.stringValueConverter(Timestamp.class).apply(forTest);
        assertEquals(Timestamp.valueOf(forTest), timestampResult);

    }


    @Test
    void testPrimitiveArray() {
        final String forTest = "1, 2,34,4";
        final int[] intArrayResult = (int[]) ConverterUtils.convertIfNecessary(forTest, int[].class);
        assertArrayEquals(new int[]{1, 2, 34, 4}, intArrayResult);
    }

    @Test
    void testRefArray() {
        final String forTest = "a, b,c,d ";
        final String[] intArrayResult = (String[]) ConverterUtils.convertIfNecessary(forTest, String[].class);
        assertArrayEquals(new String[]{"a", "b", "c", "d"}, intArrayResult);
    }

    @Test
    void testCollection() {
        final String forTest = "1, 2,34,4";
        final List listResult = (List) ConverterUtils.convertIfNecessary(forTest, List.class);
        assertNotNull(listResult);
        assertEquals("1", listResult.get(0));
        assertEquals("2", listResult.get(1));
        assertEquals("34", listResult.get(2));
        assertEquals("4", listResult.get(3));
    }

    @Test
    void testRefCollection() throws NoSuchMethodException {
        final String forTest = "1, 2,3,4";
        final List<Integer> listResult = (List<Integer>) ConverterUtils.convertIfNecessary(forTest,
                this.getClass().getDeclaredMethod("forTestGenericType").getGenericReturnType());
        assertNotNull(listResult);
        assertEquals(Integer.valueOf(1), listResult.get(0));
        assertEquals(Integer.valueOf(2), listResult.get(1));
        assertEquals(Integer.valueOf(3), listResult.get(2));
        assertEquals(Integer.valueOf(4), listResult.get(3));
    }

    List<Integer> forTestGenericType() {
        return null;
    }

    @Test
    void testSet() throws NoSuchMethodException {
        final String forTest = "1, 2,3,4";
        final Set<Integer> set = (Set<Integer>) ConverterUtils.convertIfNecessary(forTest,
                this.getClass().getDeclaredMethod("forTestGenericSet").getGenericReturnType());
        assertNotNull(set);
        final Iterator<Integer> iterator = set.iterator();
        assertEquals(Integer.valueOf(1), iterator.next());
        assertEquals(Integer.valueOf(2), iterator.next());
        assertEquals(Integer.valueOf(3), iterator.next());
        assertEquals(Integer.valueOf(4), iterator.next());
    }

    private Set<Integer> forTestGenericSet() {
        return null;
    }

    @Test
    void testSortedSet() throws NoSuchMethodException {
        final String forTest = "4,2,1,3";
        final SortedSet<Integer> set = (SortedSet<Integer>) ConverterUtils.convertIfNecessary(forTest,
                this.getClass().getDeclaredMethod("forTestGenericSortedSet").getGenericReturnType());
        assertNotNull(set);
        final Iterator<Integer> iterator = set.iterator();
        assertEquals(Integer.valueOf(1), iterator.next());
        assertEquals(Integer.valueOf(2), iterator.next());
        assertEquals(Integer.valueOf(3), iterator.next());
        assertEquals(Integer.valueOf(4), iterator.next());
    }

    private SortedSet<Integer> forTestGenericSortedSet() {
        return null;
    }

    @Test
    void testActualCollection() throws NoSuchMethodException {
        final String forTest = "1, 2,3,4";
        final List<Integer> listResult = (List<Integer>) ConverterUtils.convertIfNecessary(forTest,
                this.getClass().getDeclaredMethod("forTestLinkedListType").getGenericReturnType());
        assertNotNull(listResult);
        assertEquals(LinkedList.class, listResult.getClass());
        assertEquals(Integer.valueOf(1), listResult.get(0));
        assertEquals(Integer.valueOf(2), listResult.get(1));
        assertEquals(Integer.valueOf(3), listResult.get(2));
        assertEquals(Integer.valueOf(4), listResult.get(3));
    }

    private LinkedList<Integer> forTestLinkedListType() {
        return null;
    }

    @Test
    void testSameType() {
        final Map<String, String> forTest = new HashMap<>(16);
        final Object r = ConverterUtils.convertIfNecessary(forTest, Map.class);
        assertSame(r, forTest);
    }

    @Test
    void testTypeNotMatch() {
        final String forTest = "1, 2,3,4";
        final Object r = ConverterUtils.convertIfNecessary(forTest, Map.class);
        assertSame(r, forTest);
    }

    @Test
    void testConstructor() {
        final String value = "foo";
        final Object ret = ConverterUtils.convertIfNecessary(value, ConstructorSubject0.class);
        assertNotNull(ret);
        assertTrue(ret instanceof ConstructorSubject0);
        assertEquals(value, ((ConstructorSubject0) ret).value);
    }

    @Test
    void testAbstractConstructor() {
        final String value = "foo";
        final Object ret = ConverterUtils.convertIfNecessary(value, ConstructorSubject1.class);
        assertNotNull(ret);
        assertEquals(value, ret);
    }

    @Test
    void testNonePublicConstructor() {
        final String value = "foo";
        final Object ret = ConverterUtils.convertIfNecessary(value, ConstructorSubject2.class);
        assertNotNull(ret);
        assertEquals(value, ret);
    }

    @Test
    void testWrongArgConstructor() {
        final String value = "foo";
        final Object ret = ConverterUtils.convertIfNecessary(value, ConstructorSubject3.class);
        assertNotNull(ret);
        assertEquals(value, ret);
    }

    @Test
    void testErrorInConstructor() {
        final String value = "foo";
        assertThrows(IllegalStateException.class,
                () -> ConverterUtils.convertIfNecessary(value, ConstructorSubject4.class));
    }

    @Test
    void testValueOf() {
        final String value = "foo";
        final Object ret = ConverterUtils.convertIfNecessary(value, ValueOfSubject0.class);
        assertNotNull(ret);
        assertTrue(ret instanceof ValueOfSubject0);
        assertEquals(value, ((ValueOfSubject0) ret).value);
    }

    @Test
    void testNoneStaticValueOf() {
        final String value = "foo";
        final Object ret = ConverterUtils.convertIfNecessary(value, ValueOfSubject1.class);
        assertNotNull(ret);
        assertEquals(value, ret);
    }

    @Test
    void testNonePublicValueOf() {
        final String value = "foo";
        final Object ret = ConverterUtils.convertIfNecessary(value, ValueOfSubject2.class);
        assertNotNull(ret);
        assertEquals(value, ret);
    }

    @Test
    void testWrongArgValueOf() {
        final String value = "foo";
        final Object ret = ConverterUtils.convertIfNecessary(value, ValueOfSubject3.class);
        assertNotNull(ret);
        assertEquals(value, ret);
    }

    @Test
    void testErrorInValueOf() {
        final String value = "foo";
        assertThrows(IllegalStateException.class,
                () -> ConverterUtils.convertIfNecessary(value, ValueOfSubject4.class));

    }

    @Test
    void testWrongReturnTypeValueOf() {
        final String value = "foo";
        final Object ret = ConverterUtils.convertIfNecessary(value, ValueOfSubject5.class);
        assertNotNull(ret);
        assertEquals(value, ret);
    }


    @Test
    void testFromString() {
        final String value = "foo";
        final Object ret = ConverterUtils.convertIfNecessary(value, FromStringSubject0.class);
        assertNotNull(ret);
        assertTrue(ret instanceof FromStringSubject0);
        assertEquals(value, ((FromStringSubject0) ret).value);
    }

    @Test
    void testNoneStaticFromString() {
        final String value = "foo";
        final Object ret = ConverterUtils.convertIfNecessary(value, FromStringSubject1.class);
        assertNotNull(ret);
        assertEquals(value, ret);
    }

    @Test
    void testNonePublicFromString() {
        final String value = "foo";
        final Object ret = ConverterUtils.convertIfNecessary(value, FromStringSubject2.class);
        assertNotNull(ret);
        assertEquals(value, ret);
    }

    @Test
    void testWrongArgFromString() {
        final String value = "foo";
        final Object ret = ConverterUtils.convertIfNecessary(value, FromStringSubject3.class);
        assertNotNull(ret);
        assertEquals(value, ret);
    }

    @Test
    void testErrorFromString() {
        assertThrows(IllegalStateException.class,
                () -> ConverterUtils.convertIfNecessary("foo", FromStringSubject4.class));
    }

    @Test
    void testWrongReturnTypeFromString() {
        final String value = "foo";
        final Object ret = ConverterUtils.convertIfNecessary(value, FromStringSubject5.class);
        assertNotNull(ret);
        assertEquals(value, ret);
    }

    @Test
    void testForceConvertStringValue() throws NoSuchMethodException {
        assertNull(ConverterUtils.forceConvertStringValue(null, Integer.class));
        assertNull(ConverterUtils.forceConvertStringValue("", Integer.class));
        assertEquals(1, ConverterUtils.forceConvertStringValue("1", Integer.class).intValue());
        final List<Integer> listResult = ConverterUtils.forceConvertStringValue("1, 2,3",
                this.getClass().getDeclaredMethod("forTestLinkedListType").getGenericReturnType(), List.class);
        assertNotNull(listResult);
        assertEquals(LinkedList.class, listResult.getClass());
        assertEquals(Integer.valueOf(1), listResult.get(0));
        assertEquals(Integer.valueOf(2), listResult.get(1));
        assertEquals(Integer.valueOf(3), listResult.get(2));
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
