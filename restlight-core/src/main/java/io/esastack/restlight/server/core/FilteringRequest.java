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
package io.esastack.restlight.server.core;

import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.http.HttpMethod;

import java.io.InputStream;

/**
 * The {@link FilteringRequest} is allowed to be modified by {@link #method(HttpMethod)}, {@link #uri(String)} and
 * so on.
 * <p>
 * !NOTE: The modification of the {@link HttpRequest} will affect {@link #method()}, {@link #uri()}, {@link #path()},
 * {@link #body()} and so on...
 */
public interface FilteringRequest extends HttpRequest {

    /**
     * Set the method of the {@link HttpRequest}.
     *
     * @param method method
     */
    void method(HttpMethod method);

    /**
     * Set the uri of the {@link HttpRequest}.
     *
     * @param uri uri
     */
    void uri(String uri);

    /**
     * Set the body of the {@link HttpRequest} as byte[] format.
     *
     * @param body body
     */
    void body(byte[] body);

    /**
     * Set the body of the {@link HttpRequest} as bufferBody format.
     *
     * @param body body
     */
    void body(Buffer body);

    /**
     * Set the body of the {@link HttpRequest} as is format.
     *
     * @param ins input stream
     */
    void inputStream(InputStream ins);
}

