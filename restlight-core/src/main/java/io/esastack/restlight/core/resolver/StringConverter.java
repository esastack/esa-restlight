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

public interface StringConverter {

    /**
     * Converts the given {@code value} to an object.
     *
     * @param value value
     * @return object
     */
    Object fromString(String value);

    /**
     * Declare weather a conversion of any default value delegated to this {@link StringConverter string
     * converter} is lazy.
     * <p>
     * If {@code true},it will occur only once the default value is actually required (e.g. to be injected for
     * the first time).
     * <p>
     * If {@code false},any default value will be convert before the runtime, that is during the application
     * deployment, before any value is actually required. This conversion strategy ensures that any errors
     * in the default values are reported as early as possible.
     *
     * @return isLazy
     */
    default boolean isLazy() {
        return false;
    }

}

