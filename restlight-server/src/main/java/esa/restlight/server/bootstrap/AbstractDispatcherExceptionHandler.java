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
package esa.restlight.server.bootstrap;

import esa.commons.StringUtils;
import esa.commons.annotation.Internal;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.util.MediaType;
import esa.restlight.server.util.ErrorDetail;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

@Internal
public abstract class AbstractDispatcherExceptionHandler implements DispatcherExceptionHandler {

    protected void sendErrorResult(AsyncRequest request,
                                          AsyncResponse response,
                                          Throwable ex,
                                          HttpResponseStatus status) {
        final String msg = StringUtils.isNotEmpty(ex.getMessage()) ? ex.getMessage() : status.reasonPhrase();
        sendErrorResult(request, response, msg, status);
    }

    protected static void sendErrorResult(AsyncRequest request,
                                          AsyncResponse response,
                                          String msg,
                                          HttpResponseStatus status) {
        final byte[] errorInfo = ErrorDetail.buildError(request.path(),
                msg,
                status.reasonPhrase(),
                status.code());
        response.setHeader(HttpHeaderNames.CONTENT_TYPE, MediaType.TEXT_PLAIN.value());
        response.sendResult(status.code(), errorInfo);
    }

}

