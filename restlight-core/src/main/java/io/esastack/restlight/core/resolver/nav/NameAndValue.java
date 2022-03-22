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
import io.esastack.restlight.core.util.LazyValue;

import java.util.function.Supplier;

@Internal
public class NameAndValue<T> {

    private final String name;
    private final boolean required;
    private final Supplier<T> defaultValue;
    private final boolean isLazy;

    public NameAndValue(String name, boolean required) {
        this(name, required, null);
    }

    public NameAndValue(String name, boolean required, T defaultValue) {
        this(name, required, defaultValue == null ? null : () -> defaultValue, false);
    }

    public NameAndValue(String name,
                        boolean required,
                        Supplier<T> defaultValue,
                        boolean isLazy) {
        this.name = name;
        this.required = required;
        this.isLazy = isLazy;
        if (defaultValue == null) {
            this.defaultValue = null;
            return;
        }

        if (isLazy) {
            this.defaultValue = new LazyValue<>(defaultValue);
        } else {
            T loaded = defaultValue.get();
            this.defaultValue = () -> loaded;
        }
    }

    public String name() {
        return name;
    }

    public boolean required() {
        return required;
    }

    public Supplier<T> defaultValue() {
        return defaultValue;
    }

    public boolean isLazy() {
        return isLazy;
    }

}
