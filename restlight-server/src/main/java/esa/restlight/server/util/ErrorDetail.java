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
package esa.restlight.server.util;

import esa.commons.StringUtils;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.util.MediaType;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.StandardCharsets;

public class ErrorDetail<T> {

    private final String path;
    private final T message;
    private final long time;
    private final String error;
    private final int status;

    private ErrorDetail(String path, T message, String error, int status) {
        this.path = path;
        this.message = message;
        this.error = error;
        this.status = status;
        this.time = System.currentTimeMillis();
    }

    @SuppressWarnings("unchecked")
    public static byte[] buildErrorMsg(String path, String message, String error, int status) {
        return new ErrorDetail(path, message, error, status).toBytes();
    }

    public static <T> byte[] buildError(String path, T message, String error, int status) {
        return new ErrorDetail<>(path, message, error, status).toBytes();
    }

    public String getPath() {
        return path;
    }

    public T getMessage() {
        return message;
    }

    public long getTime() {
        return time;
    }

    public String getError() {
        return error;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"path\":\"").append(path).append("\",");
        sb.append("\"message\":\"").append(message).append("\",");
        sb.append("\"time\":\"").append(DateUtils.formatByCache(time)).append("\",");
        sb.append("\"error\":\"").append(error).append("\",");
        sb.append("\"status\":").append(status);
        sb.append('}');
        return sb.toString();
    }

    public byte[] toBytes() {
        return this.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static void sendErrorResult(AsyncRequest request,
                                       AsyncResponse response,
                                       Throwable ex,
                                       HttpResponseStatus status) {
        final String msg = StringUtils.isNotEmpty(ex.getMessage()) ? ex.getMessage() : status.reasonPhrase();
        sendErrorResult(request, response, msg, status);
    }

    public static void sendErrorResult(AsyncRequest request,
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
