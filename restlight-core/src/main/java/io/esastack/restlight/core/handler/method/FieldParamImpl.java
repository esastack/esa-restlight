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
package io.esastack.restlight.core.handler.method;

import esa.commons.Checks;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class FieldParamImpl implements FieldParam {

    private final Field field;
    private final Annotation[] annotations;

    public FieldParamImpl(Field field) {
        Checks.checkNotNull(field, "field");
        this.field = field;
        this.annotations = field.getAnnotations();
    }

    @Override
    public Field field() {
        return field;
    }

    @Override
    public String name() {
        return field.getName();
    }

    @Override
    public Class<?> declaringClass() {
        return field.getDeclaringClass();
    }

    @Override
    public Class<?> type() {
        return field.getType();
    }

    @Override
    public Type genericType() {
        return field.getGenericType();
    }

    @Override
    public Annotation[] annotations() {
        return annotations;
    }

    @Override
    public String toString() {
        return "FieldParam: " + field.getDeclaringClass() + "@" + name();
    }
}
