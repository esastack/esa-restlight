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

import io.esastack.commons.net.buffer.Buffer;
import io.esastack.restlight.core.handler.Handler;
import io.esastack.restlight.server.core.HttpResponse;

import java.io.File;

/**
 * The handled value which maybe obtained from {@link Handler#invoke(RequestContext, Object[])} or
 * {@link HttpResponse#entity()}.
 */
public interface ResponseEntity extends HttpEntity {

    /**
     * Sends the {@code buffer} to remote endpoint and end current request.
     *
     * @param buffer    buffer
     */
    void sendResult(Buffer buffer);

    /**
     * Sends the {@code file} to remote endpoint and end current request.
     *
     * @param file  file
     */
    void sendFile(File file);

    /**
     * Obtains the {@link HttpResponse} corresponding to current entity.
     *
     * @return  response.
     */
    HttpResponse response();
}

