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

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.annotation.Annotation;
import java.util.Objects;

import static io.esastack.restlight.springmvc.annotation.shaded.AliasUtils.getAnnotationDefaultValue;
import static io.esastack.restlight.springmvc.annotation.shaded.AliasUtils.getFromValueAlias;

public class ResponseStatus0 {

    private final int code;
    private final String reason;

    public ResponseStatus0(HttpStatus code, String reason) {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(reason, "reason");
        this.code = code.value();
        this.reason = reason;
    }

    public static Class<? extends Annotation> shadedClass() {
        return ResponseStatus.class;
    }

    public static ResponseStatus0 fromShade(Annotation ann) {
        if (ann == null) {
            return null;
        }
        if (ann instanceof ResponseStatus) {
            ResponseStatus instance = (ResponseStatus) ann;
            return new ResponseStatus0(getFromValueAlias(instance.code(), instance.value(),
                    (HttpStatus) getAnnotationDefaultValue(ResponseStatus.class, "code"),
                    "code"),
                    instance.reason());
        }
        throw new IllegalArgumentException("Annotation type mismatch");
    }

    public int value() {
        return code();
    }

    public int code() {
        return code;
    }

    public String reason() {
        return reason;
    }
}

