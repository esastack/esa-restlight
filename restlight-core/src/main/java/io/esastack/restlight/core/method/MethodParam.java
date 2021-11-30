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

import esa.commons.reflect.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Represents a {@link Param} which is declared as a {@link Parameter} of {@link Method}.
 */
public interface MethodParam extends Param {

    /**
     * Returns the index of current parameter. This value should be zero or greater than zero.
     *
     * @return index
     */
    int index();

    /**
     * Returns the internal instance of {@link Parameter}.
     *
     * @return parameter
     */
    Parameter parameter();

    /**
     * An instance of {@link Parameter} such as a {@link java.lang.reflect.Parameter}.
     *
     * @return current instance
     */
    @Override
    default Parameter current() {
        return parameter();
    }

    /**
     * Returns the method which {@link #parameter()} is declared in.
     *
     * @return method
     */
    Method method();

    /**
     * Returns all the declared annotations of this method.
     *
     * @return annotations or an empty array.
     */
    default Annotation[] methodAnnotations() {
        return method().getAnnotations();
    }

    /**
     * Gets the instance of given annotation type if it is present on the {@link #method()}.
     *
     * @param ann ann
     * @param <A> type of given annotation
     * @return instance of given annotation type if present otherwise {@code false}
     */
    default <A extends Annotation> A getMethodAnnotation(Class<A> ann) {
        return AnnotationUtils.findAnnotation(method(), ann);
    }

    /**
     * Whether the given type of annotation is present on the {@link #method()}..
     *
     * @param ann ann
     * @param <A> type of given annotation
     * @return {@code true} if given annotation is present otherwise {@code false}
     */
    default <A extends Annotation> boolean hasMethodAnnotation(Class<A> ann) {
        return AnnotationUtils.hasAnnotation(method(), ann);
    }

}
