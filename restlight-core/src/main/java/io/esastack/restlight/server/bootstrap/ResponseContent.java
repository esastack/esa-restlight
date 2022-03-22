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
package io.esastack.restlight.server.bootstrap;

import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.buffer.BufferAllocator;
import io.netty.buffer.ByteBufAllocator;

import java.io.File;

public interface ResponseContent {

    /**
     * Sends the given {@code data} to remote endpoint.
     *
     * @param data data
     */
    void write(byte[] data);

    /**
     * Sends the {@code data} to remote endpoint and end current request.
     *
     * @param data data
     */
    void writeThenEnd(byte[] data);

    /**
     * Send the given {@code buffer} to remote endpoint.
     *
     * @param buffer buffer
     */
    void write(Buffer buffer);

    /**
     * Sends the {@code buffer} to remote endpoint and end current request.
     *
     * @param buffer data
     */
    void writeThenEnd(Buffer buffer);

    /**
     * Sends the {@code file} to remote endpoint and end current request.
     *
     * @param file file
     */
    void writeThenEnd(File file);

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

    /**
     * Is current response has been write.
     *
     * @return isCommitted
     */
    boolean isEnded();

    /**
     * Obtains {@link BufferAllocator} of current content.
     *
     * @return allocator
     */
    ByteBufAllocator alloc();

}

