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

import java.util.Objects;

public class DefaultNamedAndValueAlias extends NamedAndValueAlias {

    private final String defaultValue;

    protected DefaultNamedAndValueAlias(String name, boolean required, String defaultValue) {
        super(name, required);
        Objects.requireNonNull(defaultValue, "defaultValue");
        this.defaultValue = defaultValue;
    }

    public String defaultValue() {
        return defaultValue;
    }
}
