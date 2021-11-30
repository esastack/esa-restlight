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

import io.esastack.restlight.core.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public class MethodParamImpl implements MethodParam {

    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];

    private final Method method;
    private final int index;
    private final Parameter parameter;
    private final String name;
    private final Class<?> type;
    private final Type genericType;
    private final Annotation[] annotations;

    public MethodParamImpl(Method method, int index) {
        this.method = method;
        this.index = index;
        this.parameter = method.getParameters()[index];
        this.name = ClassUtils.getParameterNames(method)[index];
        this.type = method.getParameterTypes()[index];
        this.genericType = method.getGenericParameterTypes()[index];
        Annotation[][] annotations = method.getParameterAnnotations();
        this.annotations = annotations.length > index ? annotations[index] : EMPTY_ANNOTATIONS;
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<?> declaringClass() {
        return method.getDeclaringClass();
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public Parameter parameter() {
        return parameter;
    }

    @Override
    public Method method() {
        return method;
    }

    @Override
    public Class<?> type() {
        return type;
    }

    @Override
    public Type genericType() {
        return genericType;
    }

    @Override
    public Annotation[] annotations() {
        return annotations;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends Annotation> A getAnnotation(Class<A> ann) {
        Annotation[] anns = annotations();
        for (Annotation a : anns) {
            if (ann.isInstance(a)) {
                return (A) a;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "MethodParam: " + method.getDeclaringClass().getName() + "=>" + method.getName() + "@" + name;
    }
}
