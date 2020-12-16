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

import esa.commons.ClassUtils;
import esa.commons.StringUtils;
import esa.commons.UrlUtils;
import esa.commons.annotation.Beta;
import esa.commons.annotation.Internal;
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

        STRING_CONVERTER_MAP.put(Double.class, Double::valueOf);
        STRING_CONVERTER_MAP.put(double.class, Double::parseDouble);

        STRING_CONVERTER_MAP.put(Float.class, Float::valueOf);
        STRING_CONVERTER_MAP.put(float.class, Float::parseFloat);

        STRING_CONVERTER_MAP.put(Long.class, Long::valueOf);
        STRING_CONVERTER_MAP.put(long.class, Long::parseLong);

        STRING_CONVERTER_MAP.put(Short.class, Short::valueOf);
        STRING_CONVERTER_MAP.put(short.class, Short::parseShort);

        STRING_CONVERTER_MAP.put(Integer.class, Integer::valueOf);
        STRING_CONVERTER_MAP.put(int.class, Integer::parseInt);

        STRING_CONVERTER_MAP.put(Void.class, v -> v);
        STRING_CONVERTER_MAP.put(void.class, v -> v);

        STRING_CONVERTER_MAP.put(Character.class, v -> v.length() > 0 ? v.charAt(0) : v);
        STRING_CONVERTER_MAP.put(char.class, v -> v.length() >= 1 ? v.charAt(0) : v);

        STRING_CONVERTER_MAP.put(Boolean.class, v -> (Boolean.parseBoolean(v) || "1".equals(v)) ? Boolean.TRUE :
                Boolean.FALSE);
        STRING_CONVERTER_MAP.put(boolean.class, v -> (Boolean.parseBoolean(v) || "1".equals(v)));

        STRING_CONVERTER_MAP.put(BigDecimal.class, v -> BigDecimal.valueOf(Double.parseDouble(v)));
        STRING_CONVERTER_MAP.put(Timestamp.class, Timestamp::valueOf);
    }

    /**
     * Parses given context path to standard form.
     *
     * @param contextPath context path
     *
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
     * Normalise the given origin default value which would be the value of {@link Constants#DEFAULT_NONE}.
     *
     * @param value origin default value
     *
     * @return normalised value
     */
    public static String normaliseDefaultValue(String value) {
        if (Constants.DEFAULT_NONE.equals(value)) {
            return null;
        }
        return value;
    }


    /**
     * @see #forceConvertStringValue(String, Type, Class)
     */
    public static <T> T forceConvertStringValue(String value, Class<T> rawType) {
        return forceConvertStringValue(value, rawType, rawType);
    }

    /**
     * Converts the given {@link String} value to target type.
     *
     * @param value   value to convert
     * @param generic target generic type
     * @param rawType target type
     * @param <T>     target type
     *
     * @return converted instance.
     * @throws IllegalArgumentException if failed to convert.
     */
    @SuppressWarnings("unchecked")
    public static <T> T forceConvertStringValue(String value, Type generic, Class<T> rawType) {
        if (StringUtils.isEmpty(value)) {
            return null;
        } else {

            Object converted = ConverterUtils.stringValueConverter(generic).apply(value);
            if (!rawType.isInstance(converted)) {
                throw new IllegalArgumentException("Could not convert given default value '"
                        + value
                        + "'to target type '" + generic + "'");
            }
            return (T) converted;
        }
    }

    /**
     * Converts given {@link String} value to an instance of target type.
     *
     * @param value        value to be converted
     * @param requiredType required type
     *
     * @return converted value or the given value itself if we do not figure out how to convert the given value.
     */
    public static Object convertIfNecessary(Object value, Type requiredType) {
        return converter(requiredType).apply(value);
    }

    /**
     * @see #convertIfNecessary(Object, Type)
     */
    public static Object convertIfNecessary(Object value, Class<?> requiredType) {
        return convertIfNecessary(value, (Type) requiredType);
    }

    /**
     * @see #stringValueConverter(Type)
     */
    public static Function<String, Object> stringValueConverter(Class<?> requiredType) {
        return stringValueConverter((Type) requiredType);
    }

    /**
     * Gets a converter of given required type for converting an {@link String} value to the required type if
     * necessary.
     *
     * @param requiredType required type
     *
     * @return converter
     */
    public static Function<String, Object> stringValueConverter(Type requiredType) {
        final Class<?> requiredClass = getRequiredClass(requiredType);
        if (requiredClass == null) {
            // we don't know how to convert
            // maybe this type is a custom implementation of java.lang.reflect.Type.
            return o -> o;
        }
        Function<String, Object> converter = STRING_CONVERTER_MAP.get(requiredClass);
        if (converter == null) {
            if (requiredClass.isArray()) {
                // array type
                converter = new ArrayConverter(requiredClass);
            } else if (Collection.class.isAssignableFrom(requiredClass)) {
                // collection type
                converter = new CollectionConverter(requiredType, requiredClass);
            } else {
                // have a constructor that accepts a single String argument.
                Constructor<?> constructor = getSingleStringParameterConstructor(requiredClass);
                if (constructor != null) {
                    converter = v -> {
                        try {
                            ReflectionUtils.makeConstructorAccessible(constructor);
                            return constructor.newInstance(v);
                        } catch (Exception e) {
                            throw new IllegalStateException("Unexpected converter error.", e);
                        }
                    };
                } else {
                    // Have a static method named valueOf() or fromString() that accepts a single String argument
                    Method m = getValueOfOrFromStringMethod(requiredClass);
                    if (m != null) {
                        converter = v -> {
                            try {
                                ReflectionUtils.makeMethodAccessible(m);
                                return m.invoke(null, v);
                            } catch (Exception e) {
                                throw new IllegalStateException("Unexpected converter error.", e);
                            }
                        };
                    }
                }
            }
        }
        return converter == null ? o -> o : converter;
    }

    /**
     * Gets a converter of given required type for converting an {@link Object} value to the required type if
     * necessary.
     *
     * @param requiredType required type
     *
     * @return converter
     */
    public static Function<Object, Object> converter(Class<?> requiredType) {
        return converter((Type) requiredType);
    }

    /**
     * Gets a converter of given required type for converting an {@link Object} value to the required type if
     * necessary.
     *
     * @param requiredType required type
     *
     * @return converter
     */
    public static Function<Object, Object> converter(Type requiredType) {
        final Class<?> requiredClass = getRequiredClass(requiredType);
        if (requiredClass == null) {
            // we don't know how to convert
            // maybe this type is a custom implementation of java.lang.reflect.Type.
            return o -> o;
        }
        if (requiredClass.isAssignableFrom(String.class)) {
            return new Delegate(requiredClass, Object::toString);
        }
        return new Delegate(requiredClass, new Default(requiredType));
    }

    private static Class<?> getRequiredClass(Type requiredType) {
        if (requiredType == null) {
            return null;
        }
        Class<?> requiredClass = null;
        if (requiredType instanceof Class<?>) {
            requiredClass = (Class<?>) requiredType;
        } else if (requiredType instanceof ParameterizedType) {
            requiredClass = (Class<?>) ((ParameterizedType) requiredType).getRawType();
        }
        return requiredClass;
    }

    private static class Delegate implements Function<Object, Object> {

        private final Class<?> requiredClass;
        private final Function<Object, Object> delegate;

        private Delegate(Class<?> requiredClass,
                         Function<Object, Object> delegate) {
            this.requiredClass = requiredClass;
            this.delegate = delegate;
        }

        @Override
        public Object apply(Object value) {
            // just return itself if it is exactly same type of the required type.
            if (value == null || requiredClass.isInstance(value)) {
                return value;
            }
            return delegate.apply(value);
        }
    }

    /**
     * Default converter
     */
    private static class Default implements Function<Object, Object> {

        private final Function<String, Object> strConverter;

        private Default(Type requiredType) {
            this.strConverter = stringValueConverter(requiredType);
        }

        @Override
        public Object apply(Object value) {
            if (value instanceof String) {
                return strConverter.apply((String) value);
            }
            return value;
        }
    }

    /**
     * Converts {@link String} value to an array.
     */
    private static class ArrayConverter implements Function<String, Object> {

        private final Class<?> elementType;
        private final Function<String, Object> elementConverter;

        private ArrayConverter(Class<?> requiredClass) {
            this.elementType = requiredClass.getComponentType();
            Function<String, Object> converter;
            if ((converter = STRING_CONVERTER_MAP.get(elementType)) == null) {
                // ignore the unsupported types
                converter = v -> v;
            }
            this.elementConverter = converter;
        }

        @Override
        public Object apply(String value) {
            final List<String> tmp = extractStringFields(value);
            final Object array = Array.newInstance(elementType, tmp.size());
            for (int i = 0, size = tmp.size(); i < size; i++) {
                // convert to target type and fill in the array.
                Array.set(array, i, elementConverter.apply(tmp.get(i)));
            }
            return array;

        }
    }

    /**
     * Converts {@link String} value to an implementation of {@link Collection} who's type will be dependent on the
     * given required class.
     */
    private static class CollectionConverter implements Function<String, Object> {

        private final Class<?> elementType;
        private final Class<?> requiredClass;
        private final Function<String, Object> elementConverter;

        private CollectionConverter(Type requiredType, Class<?> requiredClass) {
            this.requiredClass = requiredClass;
            this.elementType = ClassUtils.retrieveFirstGenericType(requiredType).orElse(Object.class);
            final Function<String, Object> converter;
            if (Object.class.equals(elementType) || elementType.isAssignableFrom(String.class)
                    || (converter = STRING_CONVERTER_MAP.get(elementType)) == null) {
                this.elementConverter = o -> o;
            } else {
                this.elementConverter = converter;
            }
        }

        @Override
        public Object apply(String value) {
            final List<String> tmp = extractStringFields(value);
            final Collection<Object> collection = createCollection(requiredClass,
                    elementType, tmp.size());
            for (String v : tmp) {
                // convert to target type and fill in the collection.
                collection.add(elementConverter.apply(v));
            }
            return collection;
        }
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

    private static List<String> extractStringFields(String value) {
        StringTokenizer tokenizer = new StringTokenizer(value, ARRAY_SEPARATOR_STR);
        // maybe it will be better if we use LinkedList ?
        List<String> tmp = new LinkedList<>();
        while (tokenizer.hasMoreElements()) {
            tmp.add(tokenizer.nextToken().trim());
        }
        return tmp;
    }

    @SuppressWarnings("all")
    private static <E> Collection<E> createCollection(Class<?> type, Class<?> elementType, int capacity) {
        if (type.isInterface()) {
            if (Set.class == type || Collection.class == type) {
                return new LinkedHashSet<>(capacity);
            } else if (List.class == type) {
                return new ArrayList<>(capacity);
            } else if (SortedSet.class == type || NavigableSet.class == type) {
                return new TreeSet<>();
            } else {
                throw new UnsupportedOperationException("Unsupported Collection interface: " + type.getName());
            }
        } else if (EnumSet.class.isAssignableFrom(type)) {
            return (Collection<E>) EnumSet.noneOf(elementType.asSubclass(Enum.class));
        } else if (Collection.class.isAssignableFrom(type)) {
            try {
                return (Collection<E>) ReflectionUtils.accessibleConstructor(type).newInstance();
            } catch (Throwable ex) {
                throw new IllegalArgumentException(
                        "Could not create collection of '" + type.getName() + "'", ex);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported Collection interface: " + type.getName());
        }
    }

}
