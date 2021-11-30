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
package io.esastack.restlight.springmvc.annotation.shaded;

import org.springframework.web.bind.annotation.MatrixVariable;

import java.lang.annotation.Annotation;
import java.util.Objects;

public class MatrixVariable0 extends DefaultNamedAndValueAlias {

    private final String pathVar;

    public MatrixVariable0(String name, boolean required, String defaultValue, String pathVar) {
        super(name, required, defaultValue);
        Objects.requireNonNull(pathVar, "pathVar");
        this.pathVar = pathVar;
    }

    public static Class<? extends Annotation> shadedClass() {
        return MatrixVariable.class;
    }

    public static MatrixVariable0 fromShade(Annotation ann) {
        if (ann == null) {
            return null;
        }
        if (ann instanceof MatrixVariable) {
            MatrixVariable instance = (MatrixVariable) ann;
            return new MatrixVariable0(AliasUtils.getNamedStringFromValueAlias(instance.name(), instance.value()),
                    instance.required(),
                    instance.defaultValue(),
                    instance.pathVar());
        }
        throw new IllegalArgumentException("Annotation type mismatch");
    }

    public String pathVar() {
        return pathVar;
    }
}
