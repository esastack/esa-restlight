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
package esa.restlight.core.handler;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.method.InvocableMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * A {@link Handler} defines abstract of an bean {@link java.lang.reflect.Method} which probably will be expressed as
 * the return value of {@link #handler()}, and the {@link Handler} could be invoke by calling {@link
 * #invoke(AsyncRequest, AsyncResponse, Object[])} method.
 */
public interface Handler extends HandlerInvoker {

    /**
     * Gets the handler object.
     *
     * @return handler
     */
    InvocableMethod handler();

    /**
     * Gets customize response of current invoker
     *
     * @return custom response, or {@code null} if there's no custom response.
     */
    HttpResponseStatus customResponse();

    /**
     * Whether there is a custom response fo current invoker.
     *
     * @return {@code true} if there is, or else {@code false}.
     */
    default boolean hasCustomResponse() {
        return this.customResponse() != null;
    }
}
