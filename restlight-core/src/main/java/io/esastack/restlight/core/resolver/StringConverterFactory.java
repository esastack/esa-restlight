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
     * {@link #createConverter(Key)}
     *
     * @param converter converter
     * @return of factory bean
     */
    static StringConverterFactory singleton(StringConverterAdapter converter) {
        return new StringConverterFactory.Singleton(converter);
    }

    /**
     * Creates an instance of {@link StringConverter} for given {@link Key}.
     *
     * @param key which is used to create resolver.
     * @return StringConverter
     */
    Optional<StringConverter> createConverter(Key key);

    /**
     * Default to use the 0.
     *
     * @return order
     */
    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * This {@link Key} is designed as a key when {@link #createConverter(Key)}ing.
     * <p>
     * !NOTE: The {@link Key#genericType} and {@link Key#type} are not always same with
     * {@link Param#genericType()} or {@link Param#type()}. eg, the {@link Param} is defined as
     * {@link Integer} of {@link java.util.List}, and you just only want to create a {@link StringConverter}
     * to convert the item {@link Integer} from string.
     */
    final class Key {

        private final Type genericType;
        private final Class<?> type;
        private final Param param;

        private Key(Type genericType, Class<?> type, Param param) {
            this.genericType = genericType;
            this.type = type;
            this.param = param;
        }

        public static Key from(Param param) {
            return new Key(param.genericType(), param.type(), param);
        }

        public static Key of(Type genericType, Class<?> type, Param param) {
            return new Key(genericType, type, param);
        }

        public Type genericType() {
            return genericType;
        }

        public Class<?> type() {
            return type;
        }

        public Param param() {
            return param;
        }
    }

    class Singleton implements StringConverterFactory {

        private final StringConverterAdapter converter;

        Singleton(StringConverterAdapter converter) {
            Checks.checkNotNull(converter, "resolver");
            this.converter = converter;
        }

        @Override
        public Optional<StringConverter> createConverter(Key param) {
            return Optional.of(converter);
        }

        @Override
        public int getOrder() {
            return converter.getOrder();
        }
    }
}

