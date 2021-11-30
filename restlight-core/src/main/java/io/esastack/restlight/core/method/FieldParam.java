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
import java.lang.reflect.Field;

/**
 * Represents a {@link Param} which is declared as a {@link Field}.
 */
public interface FieldParam extends Param {

    /**
     * An instance of {@link Field}.
     *
     * @return current instance
     */
    Field field();

    /**
     * An instance of {@link Field}.
     *
     * @return current instance
     */
    @Override
    default Field current() {
        return field();
    }

    /**
     * Returns all the declared annotations on the {@link #declaringClass()}.
     *
     * @return annotations or an empty array.
     */
    default Annotation[] classAnnotations() {
        return declaringClass().getAnnotations();
    }

    /**
     * Gets the instance of given annotation type if it is present on the {@link #declaringClass()}.
     *
     * @param ann ann
     * @param <A> type of given annotation
     *
     * @return instance of given annotation type if present otherwise {@code false}
     */
    default <A extends Annotation> A getClassAnnotation(Class<A> ann) {
        return declaringClass().getAnnotation(ann);
    }

    /**
     * Whether the given type of annotation is present on the {@link #declaringClass()}.
     *
     * @param ann ann
     * @param <A> type of given annotation
     *
     * @return {@code true} if given annotation is present otherwise {@code false}
     */
    default <A extends Annotation> boolean hasClassAnnotation(Class<A> ann) {
        return declaringClass().isAnnotationPresent(ann);
    }
}
