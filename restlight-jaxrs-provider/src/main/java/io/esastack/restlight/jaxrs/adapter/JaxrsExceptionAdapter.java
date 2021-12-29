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
package io.esastack.restlight.jaxrs.adapter;

import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.server.bootstrap.ExceptionHandlerChain;
import io.esastack.restlight.server.bootstrap.IExceptionHandler;
import io.esastack.restlight.server.bootstrap.WebServerException;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.route.RouteFailureException;
import io.esastack.restlight.server.route.predicate.RoutePredicate;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.concurrent.CompletableFuture;

public class JaxrsExceptionAdapter implements IExceptionHandler {

    @Override
    public CompletableFuture<Void> handle(RequestContext context, Throwable th, ExceptionHandlerChain next) {
        if (th == null) {
            return next.handle(context, null);
        }
        return next.handle(context, toJakartaException(context, th));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    protected WebApplicationException toJakartaException(RequestContext context, Throwable th) {
        if (th instanceof WebApplicationException) {
            return (WebApplicationException) th;
        }
        if (th instanceof WebServerException) {
            WebServerException underlying = (WebServerException) th;
            if (HttpStatus.BAD_REQUEST == underlying.status()) {
                return new BadRequestException(th.getMessage(), extractCause(underlying));
            }
            if (HttpStatus.NOT_ACCEPTABLE == underlying.status()) {
                return new NotAcceptableException(underlying.getMessage(), extractCause(underlying));
            }
            if (HttpStatus.UNSUPPORTED_MEDIA_TYPE == underlying.status()) {
                return new NotAcceptableException(underlying.getMessage(), extractCause(underlying));
            }
            if (HttpStatus.NOT_FOUND == underlying.status()) {
                RouteFailureException.RouteFailure routeFailure;
                if ((routeFailure = context.attrs().attr(RoutePredicate.MISMATCH_ERR).get()) != null) {
                    switch (routeFailure) {
                        case METHOD_MISMATCH:
                            return new NotAllowedException(Response.status(HttpStatus.NOT_FOUND.code()).build());
                        case CONSUMES_MISMATCH:
                            return new NotSupportedException(underlying.getMessage(), extractCause(underlying));
                        case PRODUCES_MISMATCH:
                            return new NotAcceptableException(underlying.getMessage(), extractCause(underlying));
                        case PATTERN_MISMATCH:
                        case HEADER_MISMATCH:
                            return new BadRequestException(underlying.getMessage(), extractCause(underlying));
                        default:
                            return new NotFoundException(underlying.getMessage(), extractCause(underlying));
                    }
                }
                return new NotFoundException(underlying.getMessage(), extractCause(underlying));
            }
            if (HttpStatus.INTERNAL_SERVER_ERROR == underlying.status()) {
                return new InternalServerErrorException(underlying.getMessage(), extractCause(underlying));
            }
        }
        return new InternalServerErrorException(th.getMessage(), th);
    }

    private Throwable extractCause(WebServerException ex) {
        if (ex.getCause() != null) {
            return ex.getCause();
        } else {
            return ex;
        }
    }
}

