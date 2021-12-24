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

import esa.commons.Checks;
import esa.commons.spi.SPI;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.util.Ordered;

import java.lang.reflect.Type;
import java.util.Optional;

@SPI
public interface StringConverterFactory extends Ordered {

    /**
     * Converts given {@link StringConverterAdapter} to {@link StringConverterFactory} which
     * always use the given {@link StringConverterAdapter} as the result of
     * {@link #createConverter(Class, Type, Param)}
     *
     * @param converter converter
     * @return of factory bean
     */
    static StringConverterFactory singleton(StringConverterAdapter converter) {
        return new StringConverterFactory.Singleton(converter);
    }

    /**
     * Creates an instance of {@link StringConverter} for given {@link Param}.
     *
     * @param type         type
     * @param genericType  genericType
     * @param param        related Param
     * @return StringConverter
     */
    Optional<StringConverter> createConverter(Class<?> type, Type genericType, Param param);

    /**
     * Default to use the 0.
     *
     * @return order
     */
    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    class Singleton implements StringConverterFactory {

        private final StringConverterAdapter converter;

        Singleton(StringConverterAdapter converter) {
            Checks.checkNotNull(converter, "resolver");
            this.converter = converter;
        }

        @Override
        public Optional<StringConverter> createConverter(Class<?> type, Type genericType, Param param) {
            return Optional.of(converter);
        }

        @Override
        public int getOrder() {
            return converter.getOrder();
        }
    }
}

