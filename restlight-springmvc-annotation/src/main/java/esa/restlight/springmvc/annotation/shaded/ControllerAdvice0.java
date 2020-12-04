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
package esa.restlight.springmvc.annotation.shaded;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.annotation.Annotation;
import java.util.Objects;

public class ControllerAdvice0 {

    private static final Class<?>[] EXTENDED_CLASSES = {RestControllerAdvice.class};

    private final String[] basePackages;
    private final Class<?>[] basePackageClasses;
    private final Class<?>[] assignableTypes;
    private final Class<? extends Annotation>[] annotations;

    public ControllerAdvice0(String[] basePackages,
                             Class<?>[] basePackageClasses,
                             Class<?>[] assignableTypes,
                             Class<? extends Annotation>[] annotations) {
        Objects.requireNonNull(basePackages, "basePackages");
        Objects.requireNonNull(basePackageClasses, "basePackageClasses");
        Objects.requireNonNull(assignableTypes, "assignableTypes");
        Objects.requireNonNull(annotations, "annotations");
        this.basePackages = basePackages;
        this.basePackageClasses = basePackageClasses;
        this.assignableTypes = assignableTypes;
        this.annotations = annotations;
    }

    public static Class<? extends Annotation> shadedClass() {
        return ControllerAdvice.class;
    }

    @SuppressWarnings("unchecked")
    public static Class<Annotation>[] extendedClasses() {
        return (Class<Annotation>[]) EXTENDED_CLASSES;
    }

    public static ControllerAdvice0 fromShade(Annotation ann) {
        if (ann == null) {
            return null;
        }
        if (ann instanceof ControllerAdvice) {
            ControllerAdvice instance = (ControllerAdvice) ann;
            return new ControllerAdvice0(AliasUtils.getStringArrayFromValueAlias(instance.basePackages(),
                    instance.value(), "basePackages"),
                    instance.basePackageClasses(),
                    instance.assignableTypes(),
                    instance.annotations());
        } else if (ann instanceof RestControllerAdvice) {
            RestControllerAdvice instance = (RestControllerAdvice) ann;
            return new ControllerAdvice0(AliasUtils.getStringArrayFromValueAlias(instance.basePackages(),
                    instance.value(), "basePackages"),
                    instance.basePackageClasses(),
                    instance.assignableTypes(),
                    instance.annotations());
        }
        throw new IllegalArgumentException("Annotation type mismatch");
    }

    public String[] value() {
        return basePackages();
    }

    public String[] basePackages() {
        return basePackages;
    }

    public Class<?>[] basePackageClasses() {
        return basePackageClasses;
    }

    public Class<?>[] assignableTypes() {
        return assignableTypes;
    }

    public Class<? extends Annotation>[] annotations() {
        return annotations;
    }
}
