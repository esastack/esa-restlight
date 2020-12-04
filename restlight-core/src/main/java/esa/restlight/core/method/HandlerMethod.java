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

import esa.commons.ClassUtils;
import esa.commons.reflect.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Obtains the information of one controller method in the controller bean. resolve the parameters associate with the
 * annotations.
 */
public class HandlerMethod implements InvocableMethod {

    private final Object bean;
    private final Class<?> beanType;
    private final Method method;
    private final MethodParam[] parameters;

    protected HandlerMethod(Class<?> userType, Method method, Object bean) {
        Objects.requireNonNull(bean, "Bean must be not null!");
        Objects.requireNonNull(method, "Method must be not null!");
        this.bean = bean;
        this.beanType = userType;
        this.method = method;
        this.parameters = initMethodParameters();
    }

    public static HandlerMethod of(Method method, Object bean) {
        return of(ClassUtils.getUserType(bean), method, bean);
    }

    public static HandlerMethod of(Class<?> userType, Method method, Object bean) {
        return new HandlerMethod(userType, method, bean);
    }

    /**
     * initialize the parameters
     *
     * @return parameters
     */
    private MethodParam[] initMethodParameters() {
        int count = this.method.getParameterCount();
        MethodParam[] result = new MethodParam[count];

        for (int i = 0; i < count; ++i) {
            result[i] = getMethodParam(i);
        }

        return result;
    }

    protected MethodParam getMethodParam(int i) {
        return new MethodParamImpl(method, i);
    }

    /**
     * get the annotation of this method
     *
     * @param annotationType annotation type
     * @param <A>            annotation generic
     *
     * @return annotation instance
     */
    @Override
    public <A extends Annotation> A getMethodAnnotation(Class<A> annotationType) {
        return AnnotationUtils.findAnnotation(this.method, annotationType);
    }

    /**
     * is annotation present
     *
     * @param annotationType annotation type
     * @param <A>            annotation generic
     *
     * @return annotation instance
     */
    @Override
    public <A extends Annotation> boolean hasMethodAnnotation(Class<A> annotationType) {
        return AnnotationUtils.hasAnnotation(this.method, annotationType);
    }

    @Override
    public Class<?> beanType() {
        return beanType;
    }

    @Override
    public Object object() {
        return bean;
    }

    @Override
    public Method method() {
        return method;
    }

    @Override
    public MethodParam[] parameters() {
        return this.parameters;
    }

    @Override
    public String toString() {
        return beanType().getName() + " => " + method().toGenericString();
    }
}
