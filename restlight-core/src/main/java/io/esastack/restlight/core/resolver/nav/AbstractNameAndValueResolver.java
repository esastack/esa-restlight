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
package io.esastack.restlight.core.resolver.nav;

import esa.commons.ObjectUtils;
import io.esastack.restlight.core.method.Param;

import java.util.Optional;
import java.util.function.Supplier;

//TODO need delete
public abstract class AbstractNameAndValueResolver {

    protected final Param param;
    protected final NameAndValue nav;

    public AbstractNameAndValueResolver(Param param) {
        this.param = param;
        this.nav = getNameAndValue(param);
    }

    protected NameAndValue getNameAndValue(Param param) {
        NameAndValue nav = createNameAndValue(param);
        return updatedNamedValue(param, nav);
    }

    /**
     * Create an instance of {@link NameAndValue} for the parameter.
     *
     * @param param parameter
     * @return name and value
     */
    protected abstract NameAndValue createNameAndValue(Param param);

    protected boolean useObjectDefaultValueIfRequired(Param param, NameAndValue info) {
        return !param.isFieldParam();
    }

    private NameAndValue updatedNamedValue(Param param, NameAndValue nav) {
        String name = nav.name();
        if (name.isEmpty()) {
            name = param.name();
            if (name == null) {
                throw new IllegalArgumentException(
                        "Name for argument type [" + param.type().getName() +
                                "] not available, and parameter name information not found in class file either.");
            }
        }

        Supplier<?> defaultValue = nav.defaultValue();
        if (defaultValue == null) {
            if (!nav.required() && (useObjectDefaultValueIfRequired(param))) {
                defaultValue = () -> defaultValue(param.type());
            } else if (Optional.class.equals(param.type())) {
                defaultValue = Optional::empty;
            }
        }

        return new NameAndValue<>(name, nav.required(), defaultValue, false);
    }

    private static Object defaultValue(Class<?> type) {
        if (Optional.class.equals(type)) {
            return Optional.empty();
        }

        return ObjectUtils.defaultValue(type);
    }

    private boolean useObjectDefaultValueIfRequired(Param param) {
        return !param.isFieldParam();
    }

}

