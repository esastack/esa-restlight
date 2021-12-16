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

import io.esastack.restlight.server.core.HttpOutputStream;

/**
 * This serializer is used to serialize the data to byte[] for http response.
 */
public interface TxSerializer {

    /**
     * serialize the object to byte array
     *
     * @param target target
     *
     * @return byte array
     * @throws Exception error
     */
    byte[] serialize(Object target) throws Exception;

    /**
     * serialize the object to byte array
     *
     * @param target       target
     * @param outputStream out
     *
     * @throws Exception error
     */
    void serialize(Object target, HttpOutputStream outputStream) throws Exception;

}
