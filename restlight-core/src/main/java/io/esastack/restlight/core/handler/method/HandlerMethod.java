/*
 * Copyright 2021 OPPO ESA Stack Project
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
package io.esastack.restlight.core.handler.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public interface HandlerMethod {

    /**
     * Get annotation type
     *
     * @param annotationType annotation type
     * @param recursive      whether to find in annotation in implemented method of current implementation.
     * @param <A>            A
     * @return A
     */
    <A extends Annotation> A getMethodAnnotation(Class<A> annotationType, boolean recursive);

    /**
     * Get annotation type
     *
     * @param annotationType annotation type
     * @param recursive      whether to find in annotation in interfaces and super class recursively.
     * @param <A>            A
     *
     * @return A
     */
    <A extends Annotation> A getClassAnnotation(Class<A> annotationType, boolean recursive);

    /**
     * Whether has annotation
     *
     * @param annotationType annotationType
     * @param recursive      whether to find in annotation in implemented method of current implementation.
     * @param <A>            A
     * @return true or false
     */
    default <A extends Annotation> boolean hasMethodAnnotation(Class<A> annotationType, boolean recursive) {
        return getMethodAnnotation(annotationType, recursive) != null;
    }

    /**
     * Whether has annotation
     *
     * @param annotationType annotation type
     * @param recursive      whether to find in annotation in interfaces and super class recursively.
     * @param <A>            A
     *
     * @return A
     */
    default <A extends Annotation> boolean hasClassAnnotation(Class<A> annotationType, boolean recursive) {
        return getClassAnnotation(annotationType, recursive) != null;
    }

    /**
     * Get handler bean type
     *
     * @return type of bean
     */
    Class<?> beanType();

    /**
     * Get the method
     *
     * @return method
     */
    Method method();

    /**
     * Get parameters.
     *
     * @return method parameters
     */
    MethodParam[] parameters();
}

