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
     * Converts given {@link StringConverter} to {@link StringConverterFactory} which
     * always use the given {@link StringConverter} as the result of
     * {@link #createConverter(Param, Class, Type)}
     *
     * @param converter converter
     * @return of factory bean
     */
    static StringConverterFactory singleton(StringConverter converter) {
        return new StringConverterFactory.Singleton(converter);
    }

    /**
     * Creates an instance of {@link StringConverter} for given {@link Param}.
     *
     * @param param           param
     * @param baseType        baseType
     * @param baseGenericType baseGenericType
     * @return StringConverter
     */
    Optional<StringConverter> createConverter(Param param, Class<?> baseType, Type baseGenericType);

    /**
     * Default to use the 0.
     *
     * @return order
     */
    @Override
    default int getOrder() {
        return 0;
    }

    class Singleton implements StringConverterFactory {

        private final StringConverter converter;

        Singleton(StringConverter converter) {
            Checks.checkNotNull(converter, "resolver");
            this.converter = converter;
        }

        @Override
        public Optional<StringConverter> createConverter(Param param, Class<?> baseType, Type baseGenericType) {
            return Optional.of(converter);
        }
    }
}

