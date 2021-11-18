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

import java.lang.annotation.Annotation;
import java.util.Arrays;

class AliasUtils {

    private static final String EMPTY = "";
    private static final String[] EMPTY_STRING_ARRAY = {};

    private AliasUtils() {
    }

    static String[] getStringArrayFromValueAlias(String[] attr, String[] alias, String attributeName) {
        return getFromValueAlias(attr, alias, EMPTY_STRING_ARRAY, attributeName);
    }

    static String getNamedStringFromValueAlias(String attr, String alias) {
        return getFromValueAlias(attr, alias, EMPTY, "name");
    }

    static <T> T getFromValueAlias(T attr, T alias, T defaultValue, String attributeName) {
        if (!equals0(attr, alias) &&
                !equals0(attr, defaultValue) &&
                !equals0(alias, defaultValue)) {

            throw new IllegalArgumentException("attribute '" + attributeName +
                    "' and its alias 'value' are present with different values, but only one is permitted.");
        }
        if (equals0(attr, defaultValue)) {
            return alias;
        }
        return attr;
    }

    static Object getAnnotationDefaultValue(Class<? extends Annotation> clz, String attribute) {
        if (attribute == null || clz == null) {
            return null;
        }
        try {
            return clz.getDeclaredMethod(attribute).getDefaultValue();
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static boolean equals0(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        if (o1.equals(o2)) {
            return true;
        }
        if (o1.getClass().isArray() && o2.getClass().isArray()) {
            if (o1 instanceof Object[] && o2 instanceof Object[]) {
                return Arrays.equals((Object[]) o1, (Object[]) o2);
            }
            if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
                return Arrays.equals((boolean[]) o1, (boolean[]) o2);
            }
            if (o1 instanceof byte[] && o2 instanceof byte[]) {
                return Arrays.equals((byte[]) o1, (byte[]) o2);
            }
            if (o1 instanceof char[] && o2 instanceof char[]) {
                return Arrays.equals((char[]) o1, (char[]) o2);
            }
            if (o1 instanceof double[] && o2 instanceof double[]) {
                return Arrays.equals((double[]) o1, (double[]) o2);
            }
            if (o1 instanceof float[] && o2 instanceof float[]) {
                return Arrays.equals((float[]) o1, (float[]) o2);
            }
            if (o1 instanceof int[] && o2 instanceof int[]) {
                return Arrays.equals((int[]) o1, (int[]) o2);
            }
            if (o1 instanceof long[] && o2 instanceof long[]) {
                return Arrays.equals((long[]) o1, (long[]) o2);
            }
            if (o1 instanceof short[] && o2 instanceof short[]) {
                return Arrays.equals((short[]) o1, (short[]) o2);
            }
        }
        return false;
    }
}
