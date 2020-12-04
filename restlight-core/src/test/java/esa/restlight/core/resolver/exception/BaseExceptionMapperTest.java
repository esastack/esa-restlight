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
package esa.restlight.core.resolver.exception;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.resolver.ExceptionResolver;
import esa.restlight.server.bootstrap.WebServerException;

import java.util.concurrent.CompletableFuture;

abstract class BaseExceptionMapperTest {

    abstract static class AbstractExceptionResolver implements ExceptionResolver<Throwable> {

        @Override
        public CompletableFuture<Void> handleException(AsyncRequest request, AsyncResponse response,
                                                       Throwable throwable) {
            return null;
        }
    }

    static class SubWebServerException extends WebServerException {

    }

    static class WebServerExceptionResolver extends AbstractExceptionResolver {
    }

    static class IllegalArgumentExceptionResolver extends AbstractExceptionResolver {
    }

    static class SubWebServerExceptionResolver extends AbstractExceptionResolver {
    }

    static class RuntimeExceptionResolver extends AbstractExceptionResolver {
    }

}
