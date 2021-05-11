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

import esa.commons.Checks;
import esa.commons.ClassUtils;
import esa.commons.StringUtils;
import esa.commons.UrlUtils;
import esa.commons.annotation.Beta;
import esa.commons.annotation.Internal;
import esa.commons.function.ThrowingIntFunction;
import esa.commons.reflect.ReflectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Converts one type to another type.
 */
@Beta
@Internal
public final class ConverterUtils {

    private static final String ARRAY_SEPARATOR_STR = ",";

    private ConverterUtils() {
    }

    private static final Map<Type, Function<String, Object>> STRING_CONVERTER_MAP
            = new HashMap<>(32);

    static {
        STRING_CONVERTER_MAP.put(Byte.class, Byte::valueOf);
        STRING_CONVERTER_MAP.put(byte.class, Byte::parseByte);

        STRING_CONVERTER_MAP.put(Character.class, v -> v.length() > 0 ? v.charAt(0) : v);
        STRING_CONVERTER_MAP.put(char.class, v -> v.length() >= 1 ? v.charAt(0) : v);

        STRING_CONVERTER_MAP.put(Boolean.class, v -> (Boolean.parseBoolean(v) || "1".equals(v)) ? Boolean.TRUE :
                Boolean.FALSE);
        STRING_CONVERTER_MAP.put(boolean.class, v -> (Boolean.parseBoolean(v) || "1".equals(v)));

        STRING_CONVERTER_MAP.put(Short.class, Short::valueOf);
        STRING_CONVERTER_MAP.put(short.class, Short::parseShort);

        STRING_CONVERTER_MAP.put(Integer.class, Integer::valueOf);
        STRING_CONVERTER_MAP.put(int.class, Integer::parseInt);

        STRING_CONVERTER_MAP.put(Long.class, Long::valueOf);
        STRING_CONVERTER_MAP.put(long.class, Long::parseLong);

        STRING_CONVERTER_MAP.put(Double.class, Double::valueOf);
        STRING_CONVERTER_MAP.put(double.class, Double::parseDouble);

        STRING_CONVERTER_MAP.put(Float.class, Float::valueOf);
        STRING_CONVERTER_MAP.put(float.class, Float::parseFloat);

        STRING_CONVERTER_MAP.put(Void.class, v -> null);
        STRING_CONVERTER_MAP.put(void.class, v -> null);

        STRING_CONVERTER_MAP.put(BigDecimal.class, v -> BigDecimal.valueOf(Double.parseDouble(v)));
        STRING_CONVERTER_MAP.put(Timestamp.class, Timestamp::valueOf);
        STRING_CONVERTER_MAP.put(String.class, v -> v);
        STRING_CONVERTER_MAP.put(Object.class, v -> v);
    }

    /**
     * Parses given context path to standard form.
     *
     * @param contextPath context path
     * @return context path, {@code ""} if given context path is null.
     */
    public static String standardContextPath(String contextPath) {
        if (contextPath == null) {
            return StringUtils.empty();
        }
        // Handle last '/'
        final String contextPathAfterClean = contextPath.endsWith("/")
                ? contextPath.substring(0, contextPath.length() - 1)
                : contextPath;
        return UrlUtils.prependLeadingSlash(contextPathAfterClean);
    }

    /**
     * Normalises the given origin default value which would be the value of {@link Constants#DEFAULT_NONE}.
     *
     * @param value origin default value
     * @return normalised value
     */
    public static String normaliseDefaultValue(String value) {
        if (Constants.DEFAULT_NONE.equals(value)) {
            return null;
        }
        return value;
    }

    /**
     * Converts the given {@link String} value to target type.
     *
     * @param value        value to convert
     * @param requiredType target generic type
     * @param <T>          target type
     * @return converted instance.
     * @throws IllegalArgumentException if failed to convert.
     */
    public static <T> T forceConvertStringValue(String value, Type requiredType) {
        Checks.checkNotNull(requiredType, "requiredType");
        if (StringUtils.isNotEmpty(value)) {
            return doForceConvertStringValue(value, requiredType);
        }

        final Class<?> requiredClass = forRawType(requiredType);
        if (requiredClass == null) {
            return null;
        }

        // convert null and empty string value to empty array or collection.
        // See https://github.com/esastack/esa-restlight/issues/43
        if (requiredClass.isArray() || Collection.class.isAssignableFrom(requiredClass)) {
            return doForceConvertStringValue(value, requiredType);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T doForceConvertStringValue(String value, Type requiredType) {
        Function<String, Object> converter = ConverterUtils.str2ObjectConverter(requiredType);
        if (converter == null) {
            throw new IllegalArgumentException("Could not convert given value '"
                    + value
                    + "'to target type '" + requiredType + "'");
        }
        return (T) converter.apply(value);
    }

    /**
     * @see #str2ObjectConverter(Type, Function)
     */
    public static Function<String, Object> str2ObjectConverter(Type requiredType) {
        return str2ObjectConverter(requiredType, null);
    }

    /**
     * Generates a converter that converts a {@link String} value to {@code requiredType}.
     *
     * @param requiredType target type
     * @return converter or {@code def} if we don't know how to convert it
     */
    public static Function<String, Object> str2ObjectConverter(Type requiredType, Function<String, Object> def) {
        Checks.checkNotNull(requiredType, "requiredType");
        final Class<?> requiredClass = forRawType(requiredType);
        final Function<String, Object> str2Object = getStr2ObjectConverter(requiredClass, requiredType);
        if (str2Object == null) {
            return def;
        }
        return p -> {
            if (p == null) {
                return null;
            }
            return str2Object.apply(p);
        };
    }

    /**
     * Generates a converter that converts a collection of {@link String} values to {@code requiredType}.
     *
     * @param requiredType target type
     * @return converter or {@code null} if we don't know how to convert it
     */
    public static Function<Collection<String>, Object> strs2ObjectConverter(Type requiredType) {
        Checks.checkNotNull(requiredType, "requiredType");
        final Class<?> requiredClass = forRawType(requiredType);
        final Function<Collection<String>, Object> strs2Object = strs2ObjectConverter(requiredClass, requiredType);
        if (strs2Object == null) {
            return null;
        }
        return p -> {
            if (p == null) {
                return null;
            }
            return strs2Object.apply(p);
        };
    }

    private static Function<Collection<String>, Object> strs2ObjectConverter(Class<?> requiredClass,
                                                                             Type requiredType) {
        Function<Collection<String>, Object> converter;
        if (requiredClass.isArray() && (converter = Strs2ArrayConverter.of(requiredClass)) != null) {
            return converter;
        }
        if (Collection.class.isAssignableFrom(requiredClass)
                && (converter = Strs2CollectionConverter.of(requiredClass, requiredType)) != null) {
            return converter;
        }
        // we don't know how to convert it
        return null;
    }

    /**
     * Converts a collection of {@link String} values to an array.
     */
    private static class Strs2ArrayConverter implements Function<Collection<String>, Object> {

        private final Class<?> elementType;
        private final Function<String, Object> elementConverter;

        private Strs2ArrayConverter(Class<?> elementType, Function<String, Object> elementConverter) {
            this.elementType = elementType;
            this.elementConverter = elementConverter;
        }

        private static Strs2ArrayConverter of(Class<?> requiredClass) {
            final Class<?> elementType = requiredClass.getComponentType();
            Function<String, Object> elementConverter = getStr2ObjectConverter(elementType, null);
            if (elementConverter == null) {
                // we don't know how to convert the elements
                return null;
            }
            return new Strs2ArrayConverter(elementType, elementConverter);
        }

        @Override
        public Object apply(Collection<String> value) {
            final Object array = Array.newInstance(elementType, value.size());

            int i = 0;
            for (String v : value) {
                // convert to target type and fill in the array.
                Array.set(array, i++, elementConverter.apply(v));
            }
            return array;
        }
    }

    /**
     * Converts {@link String} value to an array.
     */
    private static class Str2ArrayConverter implements Function<String, Object> {

        private final Strs2ArrayConverter strList2ArrayConverter;

        private Str2ArrayConverter(Strs2ArrayConverter strList2ArrayConverter) {
            this.strList2ArrayConverter = strList2ArrayConverter;
        }

        private static Str2ArrayConverter of(Class<?> requiredClass) {
            Strs2ArrayConverter strList2ArrayConverter = Strs2ArrayConverter.of(requiredClass);
            if (strList2ArrayConverter == null) {
                return null;
            }
            return new Str2ArrayConverter(strList2ArrayConverter);
        }

        @Override
        public Object apply(String value) {
            return strList2ArrayConverter.apply(extractStringFields(value));
        }
    }

    /**
     * Converts a collection of {@link String} values to an implementation of {@link Collection} who's type will be
     * dependent on the given required class.
     */
    @SuppressWarnings("rawtypes")
    private static class Strs2CollectionConverter implements Function<Collection<String>, Object> {

        private final IntFunction<Collection> collectionGenerator;
        private final Function<String, Object> elementConverter;

        private Strs2CollectionConverter(IntFunction<Collection> collectionGenerator,
                                         Function<String, Object> elementConverter) {
            this.collectionGenerator = collectionGenerator;
            this.elementConverter = elementConverter;
        }

        private static Strs2CollectionConverter of(Class<?> requiredClass, Type requiredType) {

            IntFunction<Collection> collectionGenerator = collectionGenerator(requiredClass);
            if (collectionGenerator == null) {
                // we don't know how to generate this type of collection
                return null;
            }

            Class<?> elementType = null;
            if (requiredType != null) {
                elementType = ClassUtils.retrieveFirstGenericType(requiredType).orElse(null);
            }

            if (elementType == null) {
                elementType = Object.class;
            }

            Function<String, Object> elementConverter = getStr2ObjectConverter(elementType, null);
            if (elementConverter == null) {
                // we don't know how to convert the elements
                return null;
            }
            return new Strs2CollectionConverter(collectionGenerator, elementConverter);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object apply(Collection<String> value) {
            final Collection collection = collectionGenerator.apply(value.size());
            for (String v : value) {
                // convert to target type and fill in the collection.
                collection.add(elementConverter.apply(v));
            }
            return collection;
        }
    }

    /**
     * Converts {@link String} value to an implementation of {@link Collection} who's type will be dependent on the
     * given required class.
     */
    private static class Str2CollectionConverter implements Function<String, Object> {

        private final Strs2CollectionConverter strs2CollectionConverter;

        private Str2CollectionConverter(Strs2CollectionConverter strs2CollectionConverter) {
            this.strs2CollectionConverter = strs2CollectionConverter;
        }

        private static Str2CollectionConverter of(Class<?> requiredClass, Type requiredType) {
            Strs2CollectionConverter strs2CollectionConverter =
                    Strs2CollectionConverter.of(requiredClass, requiredType);
            if (strs2CollectionConverter == null) {
                return null;
            }
            return new Str2CollectionConverter(strs2CollectionConverter);
        }

        @Override
        public Object apply(String value) {
            return strs2CollectionConverter.apply(extractStringFields(value));
        }
    }

    private static Function<String, Object> getStr2ObjectConverter(Class<?> requiredClass, Type requiredType) {
        Function<String, Object> converter = STRING_CONVERTER_MAP.get(requiredClass);
        if (converter != null) {
            return converter;
        }

        if (requiredClass.isArray() && (converter = Str2ArrayConverter.of(requiredClass)) != null) {
            return converter;
        }

        if (Collection.class.isAssignableFrom(requiredClass)
                && (converter = Str2CollectionConverter.of(requiredClass, requiredType)) != null) {
            return converter;
        }

        // have a constructor that accepts a single String argument.
        Constructor<?> constructor = getSingleStringParameterConstructor(requiredClass);
        if (constructor != null) {
            return v -> {
                try {
                    ReflectionUtils.makeConstructorAccessible(constructor);
                    return constructor.newInstance(v);
                } catch (Exception e) {
                    throw new IllegalStateException("Unexpected converter error.", e);
                }
            };
        }

        // Have a static method named valueOf() or fromString() that accepts a single String argument
        Method m = getValueOfOrFromStringMethod(requiredClass);
        if (m != null) {
            return v -> {
                try {
                    ReflectionUtils.makeMethodAccessible(m);
                    return m.invoke(null, v);
                } catch (Exception e) {
                    throw new IllegalStateException("Unexpected converter error.", e);
                }
            };
        }

        // we don't know how to convert it
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> forRawType(Type requiredType) {
        Class<T> requiredClass = null;
        if (requiredType instanceof Class<?>) {
            requiredClass = (Class<T>) requiredType;
        } else if (requiredType instanceof ParameterizedType) {
            requiredClass = (Class<T>) ((ParameterizedType) requiredType).getRawType();
        }
        return requiredClass;
    }

    private static List<String> extractStringFields(String value) {
        StringTokenizer tokenizer = new StringTokenizer(value, ARRAY_SEPARATOR_STR);
        List<String> tmp = new LinkedList<>();
        while (tokenizer.hasMoreElements()) {
            tmp.add(tokenizer.nextToken().trim());
        }
        return tmp;
    }

    private static Constructor<?> getSingleStringParameterConstructor(Class<?> requiredClass) {
        if (requiredClass == null || Modifier.isAbstract(requiredClass.getModifiers())) {
            return null;
        }
        Constructor<?> c = null;
        try {
            c = requiredClass.getDeclaredConstructor(String.class);
        } catch (NoSuchMethodException ignored) {
        }
        return (c != null
                && Modifier.isPublic(c.getModifiers()))
                ? c : null;
    }

    private static Method getValueOfOrFromStringMethod(Class<?> requiredClass) {
        if (requiredClass == null) {
            return null;
        }
        Method m = null;
        try {
            m = requiredClass.getDeclaredMethod("valueOf", String.class);
        } catch (NoSuchMethodException ignored) {
        }
        if (m == null
                || !Modifier.isStatic(m.getModifiers())
                || !Modifier.isPublic(m.getModifiers())
                || !m.getReturnType().isAssignableFrom(requiredClass)) {
            try {
                m = requiredClass.getDeclaredMethod("fromString", String.class);
            } catch (NoSuchMethodException ignored) {
            }
        }
        return (m != null
                && Modifier.isStatic(m.getModifiers())
                && Modifier.isPublic(m.getModifiers()))
                && m.getReturnType().isAssignableFrom(requiredClass)
                ? m : null;
    }

    @SuppressWarnings("rawtypes")
    private static IntFunction<Collection> collectionGenerator(Class<?> type) {
        if (type.isInterface()) {
            if (Set.class == type || Collection.class == type) {
                return LinkedHashSet::new;
            } else if (List.class == type) {
                return ArrayList::new;
            } else if (SortedSet.class == type || NavigableSet.class == type) {
                return c -> new TreeSet<>();
            } else {
                return null;
            }
        } else if (Collection.class.isAssignableFrom(type)) {
            try {
                final Constructor<?> constructor = ReflectionUtils.accessibleConstructor(type);
                if (constructor != null) {
                    return ThrowingIntFunction.rethrow(c -> (Collection) constructor.newInstance());
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

}
