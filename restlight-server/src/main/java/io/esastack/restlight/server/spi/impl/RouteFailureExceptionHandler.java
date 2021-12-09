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
package io.esastack.restlight.server.spi.impl;

import esa.commons.StringUtils;
import esa.commons.annotation.Internal;
import esa.commons.spi.Feature;
import io.esastack.commons.net.http.HttpStatus;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpserver.core.HttpResponse;
import io.esastack.httpserver.core.RequestContext;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.server.bootstrap.ExceptionHandlerChain;
import io.esastack.restlight.server.route.RouteFailureException;
import io.esastack.restlight.server.spi.ExceptionHandler;
import io.esastack.restlight.server.util.ErrorDetail;
import io.esastack.restlight.server.util.Futures;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.util.concurrent.CompletableFuture;

@Internal
@Feature(tags = Constants.INTERNAL)
public class RouteFailureExceptionHandler implements ExceptionHandler {

    @Override
    public CompletableFuture<Void> handle(RequestContext context, Throwable th,
                                          ExceptionHandlerChain<RequestContext> next) {
        if (th instanceof RouteFailureException) {
            HttpResponse response = context.response();
            HttpStatus status = toStatus(((RouteFailureException) th).getFailureType());
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, MediaType.TEXT_PLAIN.value());
            response.sendResult(status.code(),
                    ErrorDetail.buildErrorMsg(context.request().path(), StringUtils.empty(),
                            status.reasonPhrase(), status.code()));
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
        return Ordered.LOWEST_PRECEDENCE;
    }
}

