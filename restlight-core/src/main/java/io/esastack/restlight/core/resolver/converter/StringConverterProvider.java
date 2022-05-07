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
package io.esastack.restlight.core.resolver.converter;

import io.esastack.restlight.core.handler.method.Param;

import java.lang.reflect.Type;

/**
 * This {@link StringConverterProvider} is designed to get a {@link StringConverter} to resolve
 * the specified {@link Key}.
 */
public interface StringConverterProvider {

    /**
     * Obtains a {@link StringConverter} to resolve the given {@link Key}.
     *
     * @param key   key
     * @return      converter
     */
    StringConverter get(Key key);

    /**
     * In current, only {@code genericType} and {@code type} are used when getting {@link StringConverter}.
     */
    final class Key {

        private final Type genericType;
        private final Class<?> type;

        private Key(Type genericType, Class<?> type) {
            this.genericType = genericType;
            this.type = type;
        }

        public static Key of(Type genericType, Class<?> type) {
            return new Key(genericType, type);
        }

        public static Key from(Param param) {
            return new Key(param.genericType(), param.type());
        }

        public Type genericType() {
            return genericType;
        }

        public Class<?> type() {
            return type;
        }
    }

}

