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

import io.esastack.httpserver.core.HttpInputStream;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.core.method.MethodParam;
import io.esastack.restlight.core.method.Param;

import java.io.InputStream;

public interface RequestEntity extends HttpEntity {

    /**
     * Obtains the request entity as byte[] format.
     *
     * @return  byte[]
     */
    byte[] byteData();

    /**
     * Obtains the {@link HttpInputStream}.
     *
     * @return  ins
     */
    HttpInputStream inputStream();

    /**
     * Sets with the given {@code data}.
     *
     * @param data  data
     */
    void byteData(byte[] data);

    /**
     * Sets with the given {@code ins}.
     *
     * @param ins   ins
     */
    void inputStream(InputStream ins);

    /**
     * Obtains the {@link MethodParam} correspond with current entity.
     *
     * @return  method param.
     */
    Param param();

    /**
     * Obtains the {@link HttpRequest}.
     *
     * @return  request
     */
    HttpRequest request();
}

