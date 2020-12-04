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
package esa.restlight.springmvc.annotation.shaded;

import java.util.Objects;

public class NamedAndValueAlias {

    private final String name;
    private final boolean required;

    protected NamedAndValueAlias(String name, boolean required) {
        Objects.requireNonNull(name, "name");
        this.name = name;
        this.required = required;
    }

    public String value() {
        return name;
    }

    public String name() {
        return value();
    }

    public boolean required() {
        return required;
    }

}
