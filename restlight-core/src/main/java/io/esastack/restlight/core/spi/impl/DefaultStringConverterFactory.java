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
package io.esastack.restlight.core.spi.impl;

import io.esastack.restlight.core.resolver.converter.StringConverter;
import io.esastack.restlight.core.resolver.converter.StringConverterFactory;
import io.esastack.restlight.core.util.ConverterUtils;

import java.util.Optional;
import java.util.function.Function;

public class DefaultStringConverterFactory implements StringConverterFactory {

    @Override
    public Optional<StringConverter> createConverter(Key key) {
        if (key.type() == null && key.genericType() == null) {
            return Optional.empty();
        }

        final Function<String, Object> converter;
        if (key.genericType() != null) {
            converter = ConverterUtils.str2ObjectConverter(key.genericType());
        } else {
            converter = ConverterUtils.str2ObjectConverter(key.type());
        }

        return Optional.of(new StringConverter() {
            @Override
            public Object fromString(String value) {
                if (converter == null) {
                    throw new IllegalArgumentException("There is no suitable StringConverter for class(" +
                            key.type() + "),It should have a constructor that accepts a single String argument" +
                            " or have a static method named valueOf() or fromString() that accepts a " +
                            "single String argument.");
                }

                return converter.apply(value);
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
