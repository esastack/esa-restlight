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
package io.esastack.restlight.server.bootstrap;

import io.netty.handler.codec.http.HttpResponseStatus;

public class WebServerException extends RuntimeException {
    private static final long serialVersionUID = -132910193006756089L;

    public static final WebServerException BAD_REQUEST
            = new WebServerException(HttpResponseStatus.BAD_REQUEST);

    private final HttpResponseStatus status;

    public WebServerException() {
        this((HttpResponseStatus) null);
    }

    public WebServerException(String message) {
        this(null, message);
    }

    public WebServerException(String message, Throwable cause) {
        this(null, message, cause);
    }

    public WebServerException(Throwable cause) {
        this((HttpResponseStatus) null, cause);
    }

    public WebServerException(HttpResponseStatus status) {
        this.status = status == null ? HttpResponseStatus.INTERNAL_SERVER_ERROR : status;
    }

    public WebServerException(HttpResponseStatus status, String message) {
        super(message);
        this.status = status == null ? HttpResponseStatus.INTERNAL_SERVER_ERROR : status;
    }

    public WebServerException(HttpResponseStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status == null ? HttpResponseStatus.INTERNAL_SERVER_ERROR : status;
    }

    public WebServerException(HttpResponseStatus status, Throwable cause) {
        super(cause);
        this.status = status == null ? HttpResponseStatus.INTERNAL_SERVER_ERROR : status;
    }

    public static WebServerException wrap(Throwable t) {
        if (t instanceof WebServerException) {
            return (WebServerException) t;
        }
        return new WebServerException(t);
    }

    public static WebServerException badRequest() {
        return new WebServerException(HttpResponseStatus.BAD_REQUEST);
    }

    public static WebServerException badRequest(String message) {
        return new WebServerException(HttpResponseStatus.BAD_REQUEST, message);
    }

    public static WebServerException badRequest(String message, Throwable cause) {
        return new WebServerException(HttpResponseStatus.BAD_REQUEST, message, cause);
    }

    public static WebServerException badRequest(Throwable cause) {
        return new WebServerException(HttpResponseStatus.BAD_REQUEST, cause);
    }

    public HttpResponseStatus status() {
        return status;
    }
}
