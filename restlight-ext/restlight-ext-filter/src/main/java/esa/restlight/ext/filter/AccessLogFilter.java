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
package esa.restlight.ext.filter;

import esa.commons.StringUtils;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.ext.filter.config.AccessLogOptions;
import esa.restlight.server.handler.Filter;
import esa.restlight.server.handler.FilterChain;
import esa.restlight.server.util.DateUtils;
import io.netty.handler.codec.http.HttpMethod;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @deprecated use {@link esa.restlight.ext.filter.accesslog.AccessLogFilter}
 */
@Deprecated
public class AccessLogFilter implements Filter {

    private static final Logger logger =
            LoggerFactory.getLogger("ACCESS_LOG");

    private final boolean fullUri;

    public AccessLogFilter(AccessLogOptions options) {
        this.fullUri = options.isFullUri();
    }

    @Override
    public CompletableFuture<Void> doFilter(AsyncRequest request, AsyncResponse response, FilterChain chain) {
        final long start = System.nanoTime();
        final HttpMethod method = request.method();
        final String path = fullUri ? request.uri() : request.path();
        final String protocol = request.protocol();
        final int contentLength = request.contentLength();
        final String remoteAddr = request.remoteAddr();
        final int remotePort = request.remotePort();
        response.onEnd(r -> {
            final String log = StringUtils.concat(
                    "\"", method.name(),
                    " ", path,
                    " ", protocol,
                    "\" contentLength=", String.valueOf(contentLength),
                    ", remoteAddr=", remoteAddr,
                    ", remotePort=", String.valueOf(remotePort),
                    ", time=", DateUtils.now(),
                    ", code=", String.valueOf(r.status()),
                    ", duration=",
                    String.valueOf(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)), " mills");
            if (logger.isDebugEnabled()) {
                logger.debug(log);
            } else {
                logger.info(log);
            }
        });
        return chain.doFilter(request, response);
    }
}
