/*
 * Copyright 2021 OPPO ESA Stack Project
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
package io.esastack.restlight.core.resolver;

public class HandledValue<T> {

    private static final HandledValue<?> FAILED = new HandledValue<>(false, null);

    private final boolean success;
    private final T value;

    private HandledValue(boolean success, T value) {
        this.success = success;
        this.value = value;
    }

    public boolean isSuccess() {
        return success;
    }

    public T value() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public static <V> HandledValue<V> failed() {
        return (HandledValue<V>) FAILED;
    }

    public static <V> HandledValue<V> succeed(V value) {
        return new HandledValue<>(true, value);
    }
}

