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
package io.esastack.restlight.server.core;

import esa.commons.Checks;
import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.HttpStatus;

import java.util.function.Consumer;

/**
 * An interface defines a http server response.
 * <p>
 * !Note: This response is not designed as thread-safe.
 */
public interface HttpResponse {

    /**
     * Set the response code
     *
     * @param code code
     */
    void status(int code);

    /**
     * Get current http response code.
     *
     * @return status
     */
    int status();

    /**
     * Set the response entity.
     *
     * @param entity entity
     */
    void entity(Object entity);

    /**
     * Get current http response entity.
     *
     * @return entity
     */
    Object entity();

    /**
     * Is current request should be keepAlive mode.
     *
     * @return keepalive
     */
    boolean isKeepAlive();

    /**
     * Obtains the headers.
     *
     * @return headers.
     */
    HttpHeaders headers();

    /**
     * Sends a temporary redirect response to the client using the given redirect uri.
     * <p>
     * If the new url is relative without a leading '/' it will be regarded as a relative path to the current request
     * URI. otherwise it will be regarded as a relative path to root path.
     *
     * @param newUri target uri
     */
    default void redirect(String newUri) {
        Checks.checkNotEmptyArg(newUri);
        headers().set(HttpHeaderNames.LOCATION, newUri);
        status(HttpStatus.FOUND.code());
    }

    /**
     * Adds the specified cookie to the response. This method can be called multiple times to set more than one cookie.
     *
     * @param cookie the Cookie to return to the client
     */
    void addCookie(Cookie cookie);

    /**
     * Adds the specified cookie to the response. This method can be called multiple times to set more than one cookie.
     *
     * @param name  cookie name
     * @param value value
     */
    void addCookie(String name, String value);

    /**
     * Obtains the trailer headers.
     *
     * @return trailers.
     */
    HttpHeaders trailers();

    /**
     * Add a listener to this response, this listener will be called after current response has been write.
     *
     * @param listener listener
     */
    void onEnd(Consumer<HttpResponse> listener);

}
