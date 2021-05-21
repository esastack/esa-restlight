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
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.util.MediaType;
import esa.restlight.server.util.ErrorDetail;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.StandardCharsets;

public class DefaultDispatcherExceptionHandler implements DispatcherExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(DispatcherExceptionHandler.class);

    @Override
    public ExceptionHandleResult handleException(AsyncRequest request,
                                                 AsyncResponse response,
                                                 Throwable throwable) {
        //clean up response.
        if (!response.isCommitted()) {
            if (throwable != null) {
                logger.error("Error occurred when doing request(url={}, method={})",
                        request.path(), request.method(), throwable);
                HttpResponseStatus status = toCustomStatus(request, response, throwable);
                if (status == null) {
                    sendError(request, response, throwable);
                } else {
                    response.setHeader(HttpHeaderNames.CONTENT_TYPE, MediaType.TEXT_PLAIN.value());
                    response.sendResult(status.code(), status.reasonPhrase().getBytes(StandardCharsets.UTF_8));
                    return ExceptionHandleResult.handled();
                }
            } else {
                response.sendResult();
            }
        } else if (throwable != null) {
            logger.error("Error occurred when doing request(url={}, method={})",
                    request.path(), request.method(), throwable);
        }

        return ExceptionHandleResult.remained(throwable);
    }

    protected HttpResponseStatus toCustomStatus(AsyncRequest request,
                                                AsyncResponse response,
                                                Throwable throwable) {
        return null;
    }

    protected void sendError(AsyncRequest request,
                             AsyncResponse response,
                             Throwable ex) {
        final HttpResponseStatus status;
        if (ex instanceof WebServerException) {
            //400 bad request
            status = ((WebServerException) ex).status();

        } else {
            //default to 500
            status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
        }

        sendErrorResult(request, response, ex, status);
    }

    protected static void sendErrorResult(AsyncRequest request,
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

