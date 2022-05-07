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
package io.esastack.restlight.core.context;

import io.esastack.commons.net.buffer.Buffer;

import java.io.File;

/**
 * This {@link ResponseEntityChannel} is designed to just write response content to remote endpoint. There is no
 * necessary for user to care about how and when to write response headers and trailers, all of them will be
 * done automatically if you have set the {@link HttpResponse#status(int)}, {@link HttpResponse#headers()} correctly
 * before calling {@link ResponseEntityChannel}. When using {@code this}, the way to write response content is
 * the only one that you should concern.
 */
public interface ResponseEntityChannel {

    /**
     * Sends the given {@code data} to remote endpoint.
     *
     * @param data data
     */
    void write(byte[] data);

    /**
     * Send the given {@code buffer} to remote endpoint.
     *
     * @param buffer buffer
     */
    void write(Buffer buffer);

    /**
     * Sends the {@code data} to remote endpoint and end current request.
     *
     * @param data data
     */
    void end(byte[] data);

    /**
     * Sends the {@code buffer} to remote endpoint and end current request.
     *
     * @param buffer buffer
     */
    void end(Buffer buffer);

    /**
     * Sends the {@code file} to remote endpoint and end current request.
     *
     * @param file file
     */
    void end(File file);

    /**
     * Just ends current response.
     */
    void end();

    /**
     * Is current response has been write.
     *
     * @return isCommitted
     */
    boolean isCommitted();
}

