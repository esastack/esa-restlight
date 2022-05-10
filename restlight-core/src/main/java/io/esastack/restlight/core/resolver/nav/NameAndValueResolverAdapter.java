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

import esa.commons.Checks;
import esa.commons.ObjectUtils;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.param.ParamResolver;
import io.esastack.restlight.core.exception.WebServerException;
import io.esastack.restlight.core.context.RequestContext;

import java.util.Optional;
import java.util.function.Supplier;

public class NameAndValueResolverAdapter implements ParamResolver {

    private final NameAndValue<?> nav;
    private final NameAndValueResolver resolver;

    public NameAndValueResolverAdapter(Param param,
                                       NameAndValueResolver resolver) {
        Checks.checkNotNull(param, "param");
        this.resolver = Checks.checkNotNull(resolver, "resolver");
        this.nav = getNameAndValue(param, resolver.createNameAndValue(param));
    }

    @Override
    public Object resolve(RequestContext ctx) {
        Object arg = resolver.resolve(nav.name(), ctx);
        if (arg == null) {
            Supplier<?> defaultValue = nav.defaultValue();
            if (defaultValue != null) {
                arg = defaultValue.get();
            }
            if (nav.required() && arg == null) {
                throw WebServerException.badRequest("Missing required value: " + nav.name());
            }
        }
        return arg;
    }

    private NameAndValue<?> getNameAndValue(Param param, NameAndValue<?> nav) {
        Checks.checkNotNull(nav);
        return updatedNamedValue(param, nav);
    }

    private NameAndValue<?> updatedNamedValue(Param param, NameAndValue<?> nav) {
        String name = nav.name();
        if (name.isEmpty()) {
            name = param.name();
            if (name == null) {
                throw new IllegalArgumentException(
                        "Name for param type [" + param.type().getName() +
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

        return new NameAndValue<>(name, nav.required(), defaultValue, nav.isLazy());
    }

    private boolean useObjectDefaultValueIfRequired(Param param) {
        return !param.isFieldParam();
    }

    private static Object defaultValue(Class<?> type) {
        if (Optional.class.equals(type)) {
            return Optional.empty();
        }

        return ObjectUtils.defaultValue(type);
    }
}
