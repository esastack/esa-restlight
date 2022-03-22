/*
 * Copyright 2022 OPPO ESA Stack Project
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
package io.esastack.restlight.core.util;

import esa.commons.Checks;

import java.util.Optional;
import java.util.function.Supplier;

public class LazyValue<T> implements Supplier<T> {

    private final Supplier<T> supplier;
    /**
     * Because the value loaded by supplier may be null,so there use {@link Optional} to declare whether
     * the value had been loaded
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private volatile Optional<T> loaded;

    public LazyValue(Supplier<T> supplier) {
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
