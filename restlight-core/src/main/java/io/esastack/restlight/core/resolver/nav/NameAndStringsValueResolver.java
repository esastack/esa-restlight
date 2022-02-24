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
import esa.commons.function.Function3;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.util.ConverterUtils;
import io.esastack.restlight.server.context.RequestContext;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class NameAndStringsValueResolver implements NameAndValueResolver {

    private final StringConverter strConverter;
    private final Function<Collection<String>, Object> strsConverter;
    private final BiFunction<String, RequestContext, Collection<String>> paramValuesFunc;
    private final NameAndValue<Object> nav;

    public NameAndStringsValueResolver(Param param,
                                       Function3<Class<?>, Type, Param, StringConverter> converterFunc,
                                       BiFunction<String, RequestContext, Collection<String>> paramValuesFunc,
                                       NameAndValue<String> nav) {
        Checks.checkNotNull(converterFunc, "converterFunc");
        this.paramValuesFunc = Checks.checkNotNull(paramValuesFunc, "paramValuesFunc");
        this.strConverter = converterFunc.apply(param.type(), param.genericType(), param);

        BiFunction<Class<?>, Type, StringConverter> converterLookup = (baseType, baseGenericType) ->
                converterFunc.apply(baseType, baseGenericType, param);

        this.strsConverter = ConverterUtils.strs2ObjectConverter(param.type(),
                param.genericType(),
                converterLookup.andThen((converter) -> converter == null ? null : (converter::fromString)));

        if (strConverter == null && strsConverter == null) {
            throw new IllegalStateException("There is no suitable StringConverter for param(" + param + ").");
        }

        this.nav = transNameAndValue(nav);
    }

    @Override
    public Object resolve(String name, RequestContext ctx) {
        Collection<String> values = paramValuesFunc.apply(name, ctx);
        if (values == null || values.isEmpty()) {
            return null;
        }
        if (strsConverter != null) {
            if ((values.size() > 1) || (strConverter == null)) {
                return strsConverter.apply(values);
            } else {
                return strConverter.fromString(values.iterator().next());
            }
        } else {
            return strConverter.fromString(values.iterator().next());
        }
    }

    @Override
    public NameAndValue<Object> createNameAndValue(Param param) {
        return nav;
    }

    private NameAndValue<Object> transNameAndValue(NameAndValue<String> nav) {
        Supplier<String> defaultValue = nav.defaultValue();
        if (defaultValue == null) {
            return new NameAndValue<>(nav.name(),
                    nav.required(),
                    null);
        } else {
            if (strConverter == null) {
                return new NameAndValue<>(nav.name(),
                        nav.required(),
                        strsConverter.apply(Collections.singletonList(nav.defaultValue().get())));
            } else {
                return new NameAndValue<>(nav.name(),
                        nav.required(),
                        () -> strConverter.fromString(defaultValue.get()),
                        strConverter.isLazy());
            }
        }
    }
}
