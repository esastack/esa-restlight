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
package esa.restlight.core.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Method which can be invoke through reflection
 */
public interface InvocableMethod {

    /**
     * Get annotation type
     *
     * @param annotationType annotation type
     * @param <A>            A
     *
     * @return A
     */
    <A extends Annotation> A getMethodAnnotation(Class<A> annotationType);

    /**
     * Whether has annotation
     *
     * @param annotationType annotationType
     * @param <A>            A
     *
     * @return true or false
     */
    <A extends Annotation> boolean hasMethodAnnotation(Class<A> annotationType);

    /**
     * Get handler bean type
     *
     * @return type of bean
     */
    Class<?> beanType();

    /**
     * Get the object on which the method is invoked
     *
     * @return obj
     */
    Object object();

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
