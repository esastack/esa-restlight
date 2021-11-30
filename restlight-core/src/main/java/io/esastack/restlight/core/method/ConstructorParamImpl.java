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

import io.esastack.restlight.core.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public class ConstructorParamImpl implements ConstructorParam {

    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];

    private final Constructor<?> constructor;
    private final int index;
    private final Parameter parameter;
    private final String name;
    private final Class<?> type;
    private final Type genericType;
    private final Annotation[] annotations;

    public ConstructorParamImpl(Constructor<?> constructor, int index) {
        this.constructor = constructor;
        this.index = index;
        this.parameter = constructor.getParameters()[index];
        this.name = ClassUtils.getParameterNames(constructor)[index];
        this.type = constructor.getParameterTypes()[index];
        this.genericType = constructor.getGenericParameterTypes()[index];
        Annotation[][] annotations = constructor.getParameterAnnotations();
        this.annotations = annotations.length > index ? annotations[index] : EMPTY_ANNOTATIONS;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<?> declaringClass() {
        return constructor.getDeclaringClass();
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
    public Constructor<?> constructor() {
        return constructor;
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
        return "ConstructorParam: " + constructor.getName() + "@" + name;
    }
}

