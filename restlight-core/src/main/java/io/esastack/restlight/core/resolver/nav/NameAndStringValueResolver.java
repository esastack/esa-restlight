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
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.StringConverter;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class NameAndStringValueResolver implements NameAndValueResolver {

    private final StringConverter converter;
    private final BiFunction<String, RequestContext, String> valueExtractor;
    private final NameAndValue<Object> nav;

    public NameAndStringValueResolver(Param param,
                                      HandlerResolverFactory resolverFactory,
                                      BiFunction<String, RequestContext, String> valueExtractor,
                                      NameAndValue<String> nav) {
        Checks.checkNotNull(resolverFactory, "resolverFactory");
        this.converter = Checks.checkNotNull(resolverFactory.getStringConverter(param.type(),
                param.genericType(),
                param), "converter");
        this.valueExtractor = Checks.checkNotNull(valueExtractor, "valueExtractor");

        Supplier<String> defaultValue = nav.defaultValue();
        if (defaultValue == null) {
            this.nav = new NameAndValue<>(nav.name(), nav.required(), null);
        } else {
            this.nav = new NameAndValue<>(nav.name(),
                    nav.required(),
                    () -> converter.fromString(defaultValue.get()),
                    converter.isLazy());
        }
    }

    @Override
    public Object resolve(String name, RequestContext ctx) {
        return converter.fromString(valueExtractor.apply(name, ctx));
    }

    @Override
    public NameAndValue<Object> createNameAndValue(Param param) {
        return nav;
    }
}
