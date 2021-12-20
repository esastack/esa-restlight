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

import esa.commons.Checks;
import esa.commons.annotation.Internal;

import java.util.Optional;
import java.util.function.Supplier;

@Internal
public class NameAndValue<T> {

    private final String name;
    private final boolean required;
    private final Supplier<T> defaultValue;

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
        if (defaultValue == null) {
            this.defaultValue = null;
            return;
        }

        if (isLazy) {
            this.defaultValue = new LazyDefaultValue<>(defaultValue);
        } else {
            this.defaultValue = defaultValue;
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

    private static class LazyDefaultValue<T> implements Supplier<T> {

        private final Supplier<T> supplier;
        /**
         * Because the value loaded by supplier may be null,so there use {@link Optional} to declare whether
         * the value had been loaded
         */
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private volatile Optional<T> loaded;

        private LazyDefaultValue(Supplier<T> supplier) {
            this.supplier = Checks.checkNotNull(supplier, "supplier");
        }

        @Override
        public T get() {
            if (loaded != null) {
                return getLoaded();
            }
            synchronized (this) {
                if (loaded != null) {
                    return getLoaded();
                }
                loaded = Optional.ofNullable(supplier.get());
                return getLoaded();
            }
        }

        private T getLoaded() {
            if (loaded == Optional.empty()) {
                return null;
            } else {
                return loaded.orElse(null);
            }
        }
    }

}
