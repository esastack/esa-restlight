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
package io.esastack.restlight.core.spi.impl;

import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.resolver.StringConverterFactory;
import io.esastack.restlight.core.util.ConverterUtils;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;

public class DefaultStringConverterFactory implements StringConverterFactory {

    @Override
    public Optional<StringConverter> createConverter(Class<?> type, Type genericType, Param relatedParam) {
        Function<String, Object> converter = null;
        if (genericType != null) {
            converter = ConverterUtils.str2ObjectConverter(genericType);
        } else if (type != null) {
            converter = ConverterUtils.str2ObjectConverter(type);
        }

        if (converter == null) {
            return Optional.empty();
        }

        Function<String, Object> finalConverter = converter;
        return Optional.of(new StringConverter() {
            @Override
            public Object fromString(String value) {
                return finalConverter.apply(value);
            }

            @Override
            public boolean isLazy() {
                return false;
            }
        });
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
