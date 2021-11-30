/*
 * Copyright 2020 OPPO ESA Stack Project
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
package io.esastack.restlight.core.serialize;

import io.esastack.httpserver.core.HttpInputStream;

import java.lang.reflect.Type;

/**
 * This serializer is used to deserialize the byte[] from request to data of the given type.
 */
public interface RxSerializer {

    /**
     * deSerialize the data from byte array to the object
     *
     * @param data data
     * @param type data type
     * @param <T>  generic type
     * @return decoded value
     * @throws Exception error
     */
    <T> T deserialize(byte[] data, Type type) throws Exception;

    /**
     * deSerialize the data from byte array to the object
     *
     * @param inputStream inputStream
     * @param type        data type
     * @param <T>         generic type
     * @return decoded value
     * @throws Exception error
     */
    <T> T deserialize(HttpInputStream inputStream, Type type) throws Exception;

}
