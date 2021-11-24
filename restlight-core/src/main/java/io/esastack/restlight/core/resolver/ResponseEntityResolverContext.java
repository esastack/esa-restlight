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

import io.esastack.restlight.core.method.Param;

import java.io.OutputStream;

public interface ResponseEntityResolverContext extends HttpEntityResolverContext {

    /**
     * Obtains the {@link Param} to resolve.
     *
     * @return  param
     */
    @Override
    ResponseEntity entityInfo();

    /**
     * Obtains the current entity.
     *
     * @return  value
     */
    Object entity();

    /**
     * Sets entity value.
     *
     * @param entity    entity
     */
    void entity(Object entity);

    /**
     * Sets the input stream to resolve.
     *
     * @param os   output stream
     */
    void outputStream(OutputStream os);

    /**
     * Resolves the {@link #entity()} by given {@link #context()}.
     * @throws Exception exception
     */
    void proceed() throws Exception;

}
