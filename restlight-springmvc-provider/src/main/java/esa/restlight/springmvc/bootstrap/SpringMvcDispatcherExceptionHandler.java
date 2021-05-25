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
package esa.restlight.springmvc.bootstrap;

import esa.commons.annotation.Internal;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.util.MediaType;
import esa.restlight.server.bootstrap.DispatcherExceptionHandler;
import esa.restlight.springmvc.util.ResponseStatusUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.StandardCharsets;

@Internal
public class SpringMvcDispatcherExceptionHandler implements DispatcherExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(SpringMvcDispatcherExceptionHandler.class);

    @Override
    public HandleStatus handleException(AsyncRequest request, AsyncResponse response, Throwable throwable) {
        final HttpResponseStatus status = ResponseStatusUtils.getCustomResponse(throwable);
        if (status == null) {
            return HandleStatus.UNHANDLED_RETAINED;
        }

        response.setHeader(HttpHeaderNames.CONTENT_TYPE, MediaType.TEXT_PLAIN.value());
        response.sendResult(status.code(), status.reasonPhrase().getBytes(StandardCharsets.UTF_8));

        logger.error("Error occurred when doing request(url={}, method={})",
                request.path(), request.method(), throwable);

        return HandleStatus.HANDLED_CLEAN;
    }

    @Override
    public int getOrder() {
        return -200;
    }
}

