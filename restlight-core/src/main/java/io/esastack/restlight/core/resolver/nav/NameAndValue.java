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
package io.esastack.restlight.core.resolver.nav;

import esa.commons.annotation.Internal;

import java.util.function.Supplier;

@Internal
public class NameAndValue {

    public final String name;
    public final boolean required;
    public final Supplier<Object> defaultValue;
    public final boolean hasDefaultValue;
    private final static Supplier<Object> NULL_SUPPLIER = () -> null;

    public NameAndValue(String name, boolean required) {
        this(name, required, null);
    }

    public NameAndValue(String name, boolean required, Object defaultValue) {
        this(name, required, defaultValue, defaultValue != null);
    }

    @SuppressWarnings("unchecked")
    public NameAndValue(String name, boolean required, Object defaultValue, boolean hasDefaultValue) {
        this.name = name;
        this.required = required;
        if (defaultValue != null) {
            if (defaultValue instanceof Supplier) {
                this.defaultValue = (Supplier<Object>) defaultValue;
            } else {
                this.defaultValue = () -> defaultValue;
            }
        } else {
            this.defaultValue = NULL_SUPPLIER;
        }
        this.hasDefaultValue = hasDefaultValue;
    }

    public NameAndValue(String name, boolean required, Supplier<Object> defaultValue, boolean hasDefaultValue) {
        this.name = name;
        this.required = required;
        this.defaultValue = defaultValue;
        this.hasDefaultValue = hasDefaultValue;
    }

}
