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

import io.esastack.commons.net.http.HttpStatus;

public class WebServerException extends RuntimeException {
    private static final long serialVersionUID = -132910193006756089L;

    public static final WebServerException BAD_REQUEST
            = new WebServerException(HttpStatus.BAD_REQUEST);

    private final HttpStatus status;

    public WebServerException() {
        this((HttpStatus) null);
    }

    public WebServerException(String message) {
        this(null, message);
    }

    public WebServerException(String message, Throwable cause) {
        this(null, message, cause);
    }

    public WebServerException(Throwable cause) {
        this((HttpStatus) null, cause);
    }

    public WebServerException(HttpStatus status) {
        this.status = status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
    }

    public WebServerException(HttpStatus status, String message) {
        super(message);
        this.status = status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
    }

    public WebServerException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
    }

    public WebServerException(HttpStatus status, Throwable cause) {
        super(cause);
        this.status = status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
    }

    public static WebServerException wrap(Throwable t) {
        if (t instanceof WebServerException) {
            return (WebServerException) t;
        }
        return new WebServerException(t);
    }

    public static WebServerException badRequest() {
        return new WebServerException(HttpStatus.BAD_REQUEST);
    }

    public static WebServerException badRequest(String message) {
        return new WebServerException(HttpStatus.BAD_REQUEST, message);
    }

    public static WebServerException badRequest(String message, Throwable cause) {
        return new WebServerException(HttpStatus.BAD_REQUEST, message, cause);
    }

    public static WebServerException badRequest(Throwable cause) {
        return new WebServerException(HttpStatus.BAD_REQUEST, cause);
    }

    public HttpStatus status() {
        return status;
    }
}
