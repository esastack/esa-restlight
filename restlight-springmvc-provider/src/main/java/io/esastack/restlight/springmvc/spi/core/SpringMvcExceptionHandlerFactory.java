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
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import esa.commons.spi.Feature;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.spi.ExceptionHandler;
import io.esastack.restlight.core.spi.ExceptionHandlerFactory;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.server.bootstrap.ExceptionHandlerChain;
import io.esastack.restlight.server.util.Futures;
import io.esastack.restlight.springmvc.util.ResponseStatusUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Internal
@Feature(tags = Constants.INTERNAL)
public class SpringMvcExceptionHandlerFactory implements ExceptionHandlerFactory {

    @Override
    public Optional<ExceptionHandler> handler(DeployContext<? extends RestlightOptions> ctx) {
        return Optional.of(new SpringMvcExceptionHandler());
    }

    private static class SpringMvcExceptionHandler implements ExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(SpringMvcExceptionHandler.class);

        @Override
        public CompletableFuture<Void> handle(RequestContext context, Throwable th,
                                              ExceptionHandlerChain<RequestContext> next) {
            final HttpResponseStatus status = ResponseStatusUtils.getCustomResponse(th);
            if (status == null) {
                return next.handle(context, th);
            }

            final HttpRequest request = context.request();
            final HttpResponse response = context.response();
            response.setHeader(HttpHeaderNames.CONTENT_TYPE, MediaType.TEXT_PLAIN.value());
            response.sendResult(status.code(), status.reasonPhrase().getBytes(StandardCharsets.UTF_8));

            logger.error("Error occurred when doing request(url={}, method={})",
                    request.path(), request.method(), th);

            return Futures.completedFuture();
        }

        @Override
        public int getOrder() {
            return -200;
        }
    }

}

