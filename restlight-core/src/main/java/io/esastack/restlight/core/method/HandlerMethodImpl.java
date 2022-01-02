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
package io.esastack.restlight.core.method;

import esa.commons.Checks;
import esa.commons.reflect.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;

public class HandlerMethodImpl implements HandlerMethod {

    private final Class<?> beanType;
    private final Method method;
    private final MethodParam[] parameters;

    protected HandlerMethodImpl(Class<?> userType, Method method) {
        Checks.checkNotNull(userType, "userType");
        Checks.checkNotNull(method, "method");
        this.beanType = userType;
        this.method = method;
        this.parameters = initMethodParams();
    }

    public static HandlerMethodImpl of(Class<?> userType, Method method) {
        return new HandlerMethodImpl(userType, method);
    }

    /**
     * initialize the parameters
     *
     * @return parameters
     */
    private MethodParam[] initMethodParams() {
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
    public Method method() {
        return method;
    }

    @Override
    public MethodParam[] parameters() {
        return this.parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HandlerMethodImpl that = (HandlerMethodImpl) o;
        return Objects.equals(beanType, that.beanType) && Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beanType, method);
    }

    @Override
    public String toString() {
        return "HandlerMethod: {" + beanType().getName() + " => " + method().toGenericString() + "}";
    }
}

