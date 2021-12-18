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

import java.util.function.Supplier;

@Internal
public class NameAndValue<T> {

    public final String name;
    public final boolean required;
    public final T defaultValue;
    public final boolean hasDefaultValue;

    public NameAndValue(String name, boolean required) {
        this(name, required, null);
    }

    public NameAndValue(String name, boolean required, T defaultValue) {
        this(name, required, defaultValue, defaultValue != null);
    }

    public NameAndValue(String name, boolean required, T defaultValue, boolean hasDefaultValue) {
        this.name = name;
        this.required = required;
        this.defaultValue = defaultValue;
        this.hasDefaultValue = hasDefaultValue;
    }

    public static class LazyDefaultValue implements Supplier<Object> {

        private final Supplier<Object> supplier;
        private volatile Object value;
        //Because the value may be null,so there need a flag which declare whether the value had been loaded
        private volatile boolean loaded = false;

        public LazyDefaultValue(Supplier<Object> supplier) {
            this.supplier = Checks.checkNotNull(supplier);
        }

        @Override
        public Object get() {
            if (loaded) {
                return value;
            }
            synchronized (this) {
                if (loaded) {
                    return value;
                }
                value = supplier.get();
                loaded = true;
                return value;
            }
        }
    }

}
