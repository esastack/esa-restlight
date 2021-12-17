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
        //The reason why the object is not used directly is that the object
        //may be null and cannot be used to judge whether it has been loaded
        private volatile Supplier<Object> loadedSupplier;

        public LazyDefaultValue(Supplier<Object> supplier) {
            this.supplier = Checks.checkNotNull(supplier);
        }

        @Override
        public Object get() {
            if (loadedSupplier != null) {
                return loadedSupplier.get();
            }
            synchronized (this) {
                if (loadedSupplier != null) {
                    return loadedSupplier.get();
                }
                Object obj = supplier.get();
                loadedSupplier = () -> obj;
                return obj;
            }
        }
    }

}
