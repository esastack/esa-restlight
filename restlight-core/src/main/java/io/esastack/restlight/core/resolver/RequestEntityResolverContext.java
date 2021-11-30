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

import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.method.Param;

import java.io.InputStream;

public interface RequestEntityResolverContext extends HttpEntityResolverContext {

    /**
     * Obtains the {@link Param} to resolve.
     *
     * @return  param
     */
    @Override
    RequestEntity entityInfo();

    /**
     * Obtains the {@link MediaType} corresponds to current entity.
     *
     * @return  media type
     */
    MediaType mediaType();

    /**
     * Sets the {@link MediaType}.
     *
     * @param mediaType mediaType
     */
    void mediaType(MediaType mediaType);

    /**
     * Sets the input stream to resolve.
     *
     * @param ins   ins
     */
    void inputStream(InputStream ins);

    /**
     * Resolves the {@link #entityInfo()} by given {@link #context()}.
     *
     * @return object resolved value
     * @throws Exception exception
     */
    Object proceed() throws Exception;

}

