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
package io.esastack.restlight.springmvc.spi.core;

import esa.commons.annotation.Internal;
import esa.commons.spi.Feature;
import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.server.ServerDeployContext;
import io.esastack.restlight.server.bootstrap.ExceptionHandlerChain;
import io.esastack.restlight.server.bootstrap.IExceptionHandler;
import io.esastack.restlight.server.config.ServerOptions;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.spi.ExceptionHandlerFactory;
import io.esastack.restlight.server.util.ErrorDetail;
import io.esastack.restlight.server.util.Futures;
import io.esastack.restlight.springmvc.util.ResponseStatusUtils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Internal
@Feature(tags = Constants.INTERNAL)
public class SpringMvcExceptionHandlerFactory implements ExceptionHandlerFactory {

    @Override
    public Optional<IExceptionHandler> handler(ServerDeployContext<? extends ServerOptions> ctx) {
        return Optional.of(new SpringMvcExceptionHandler());
    }

    private static class SpringMvcExceptionHandler implements IExceptionHandler {

        @Override
        public CompletableFuture<Void> handle(RequestContext context, Throwable th,
                                              ExceptionHandlerChain next) {
            final HttpStatus status = ResponseStatusUtils.getCustomResponse(th);
            if (status == null) {
                return next.handle(context, th);
            }

            final HttpResponse response = context.response();
            response.status(status.code());
            response.entity(new ErrorDetail<>(context.request().path(), status.reasonPhrase()));

            return Futures.completedFuture();
        }

        @Override
        public int getOrder() {
            return -200;
        }
    }

}

