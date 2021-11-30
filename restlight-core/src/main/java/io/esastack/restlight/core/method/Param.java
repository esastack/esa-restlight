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
package io.esastack.restlight.core.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.function.Consumer;

/**
 * Param indicates a parameter of an {@link AnnotatedElement} which could be a {@link java.lang.reflect.Parameter} in a
 * {@link java.lang.reflect.Method} or a {@link java.lang.reflect.Parameter} in a {@link java.lang.reflect.Constructor}
 * or even a {@link java.lang.reflect.Field}.
 */
public interface Param {

    /**
     * Gets the name of this parameter which would be de
     *
     * @return name
     */
    String name();

    /**
     * Class which holding this parameter.
     *
     * @return class
     */
    Class<?> declaringClass();

    /**
     * Class type of this parameter.
     *
     * @return type
     */
    Class<?> type();

    /**
     * Generic type of this parameter.
     *
     * @return type
     */
    Type genericType();

    /**
     * An instance of {@link AnnotatedElement} such as a {@link java.lang.reflect.Parameter}, {@link
     * java.lang.reflect.Field}, {@link java.lang.reflect.Constructor} and so on...
     *
     * @return current instance
     */
    AnnotatedElement current();

    /**
     * Whether this parameter is is an instance of {@link MethodParam}.
     *
     * @return {@code true} if current param is an instance of {@link MethodParam}
     */
    default boolean isMethodParam() {
        return methodParam() != null;
    }

    /**
     * Gets the instance current parameter as an instance of {@link MethodParam} if possible.
     *
     * @return instance of {@link MethodParam} or else {@code null} if current param is not an instance of {@link
     * MethodParam}
     */
    default MethodParam methodParam() {
        if (this instanceof MethodParam) {
            return (MethodParam) this;
        }
        return null;
    }

    /**
     * Calls the given {@link Consumer} if current param is not an instance of {@link MethodParam}.
     *
     * @param c consumer
     */
    default void ifMethodParam(Consumer<MethodParam> c) {
        MethodParam p = methodParam();
        if (p != null) {
            c.accept(p);
        }
    }

    /**
     * Whether this parameter is is an instance of {@link ConstructorParam}.
     *
     * @return {@code true} if current param is an instance of {@link ConstructorParam}
     */
    default boolean isConstructorParam() {
        return constructorParam() != null;
    }

    /**
     * Gets the instance current parameter as an instance of {@link ConstructorParam} if possible.
     *
     * @return instance of {@link ConstructorParam} or else {@code null} if current param is not an instance of {@link
     * ConstructorParam}
     */
    default ConstructorParam constructorParam() {
        if (this instanceof ConstructorParam) {
            return (ConstructorParam) this;
        }
        return null;
    }

    /**
     * Calls the given {@link Consumer} if current param is not an instance of {@link ConstructorParam}.
     *
     * @param c consumer
     */
    default void ifConstructorParam(Consumer<ConstructorParam> c) {
        ConstructorParam p = constructorParam();
        if (p != null) {
            c.accept(p);
        }
    }

    /**
     * Whether this parameter is is an instance of {@link FieldParam}.
     *
     * @return {@code true} if current param is an instance of {@link FieldParam}
     */
    default boolean isFieldParam() {
        return fieldParam() != null;
    }

    /**
     * Gets the instance current parameter as an instance of {@link FieldParam} if possible.
     *
     * @return instance of {@link FieldParam} or else {@code null} if current param is not an instance of {@link
     * FieldParam}
     */
    default FieldParam fieldParam() {
        if (this instanceof FieldParam) {
            return (FieldParam) this;
        }
        return null;
    }

    /**
     * Calls the given {@link Consumer} if current param is not an instance of {@link FieldParam}.
     *
     * @param c consumer
     */
    default void ifFieldParam(Consumer<FieldParam> c) {
        FieldParam p = fieldParam();
        if (p != null) {
            c.accept(p);
        }
    }

    /**
     * Returns all the declared annotations of this parameter.
     *
     * @return annotations or an empty array.
     */
    default Annotation[] annotations() {
        return current().getAnnotations();
    }

    /**
     * Gets the instance of given annotation type if it is present.
     *
     * @param ann ann
     * @param <A> type of given annotation
     *
     * @return instance of given annotation type if present otherwise {@code false}
     */
    default <A extends Annotation> A getAnnotation(Class<A> ann) {
        return current().getAnnotation(ann);
    }

    /**
     * Whether the given type of annotation is present.
     *
     * @param ann ann
     * @param <A> type of given annotation
     *
     * @return {@code true} if given annotation is present otherwise {@code false}
     */
    default <A extends Annotation> boolean hasAnnotation(Class<A> ann) {
        return getAnnotation(ann) != null;
    }
}
