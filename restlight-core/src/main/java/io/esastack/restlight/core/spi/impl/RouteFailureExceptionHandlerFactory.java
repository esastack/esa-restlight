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
package io.esastack.restlight.core.spi.impl;

import esa.commons.annotation.Internal;
import esa.commons.spi.Feature;
import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.core.dispatcher.ExceptionHandlerChain;
import io.esastack.restlight.core.dispatcher.IExceptionHandler;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.route.RouteFailureException;
import io.esastack.restlight.core.spi.ExceptionHandlerFactory;
import io.esastack.restlight.core.util.ErrorDetail;
import io.esastack.restlight.core.util.Futures;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

@Internal
@Feature(tags = Constants.INTERNAL)
public class RouteFailureExceptionHandlerFactory implements ExceptionHandlerFactory {

    @Override
    public Optional<IExceptionHandler> handler(DeployContext ctx) {
        return Optional.of(new RouteFailureExceptionHandler());
    }

    private static class RouteFailureExceptionHandler implements IExceptionHandler {

        @Override
        public CompletionStage<Void> handle(RequestContext context, Throwable th,
                                            ExceptionHandlerChain next) {
            if (th instanceof RouteFailureException) {
                HttpResponse response = context.response();
                HttpStatus status = toStatus(((RouteFailureException) th).failureType());
                response.status(status.code());
                response.entity(new ErrorDetail<>(context.request().path(), status.reasonPhrase()));
                return Futures.completedFuture();
            } else {
                return next.handle(context, th);
            }
        }

        private HttpStatus toStatus(RouteFailureException.RouteFailure cause) {
            switch (cause) {
                case METHOD_MISMATCH:
                    return HttpStatus.METHOD_NOT_ALLOWED;
                case CONSUMES_MISMATCH:
                    return HttpStatus.UNSUPPORTED_MEDIA_TYPE;
                case PRODUCES_MISMATCH:
                    return HttpStatus.NOT_ACCEPTABLE;
                default:
                    return HttpStatus.NOT_FOUND;
            }
        }

        @Override
        public int getOrder() {
            return 100;
        }
    }
}

