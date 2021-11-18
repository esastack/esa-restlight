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

import org.springframework.web.bind.annotation.ExceptionHandler;

import java.lang.annotation.Annotation;
import java.util.Objects;

public class ExceptionHandler0 {

    private final Class<? extends Throwable>[] value;

    public ExceptionHandler0(Class<? extends Throwable>[] value) {
        Objects.requireNonNull(value, "value");
        this.value = value;
    }

    public static Class<? extends Annotation> shadedClass() {
        return ExceptionHandler.class;
    }

    public static ExceptionHandler0 fromShade(Annotation ann) {
        if (ann == null) {
            return null;
        }
        if (ann instanceof ExceptionHandler) {
            ExceptionHandler instance = (ExceptionHandler) ann;
            return new ExceptionHandler0(instance.value());
        }
        throw new IllegalArgumentException("Annotation type mismatch");
    }

    public Class<? extends Throwable>[] value() {
        return value;
    }

}
