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
package io.esastack.restlight.ext.filter;

import esa.commons.StringUtils;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import io.esastack.restlight.ext.filter.config.AccessLogOptions;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.httpserver.core.HttpResponse;
import io.esastack.restlight.server.handler.FilterChain;
import io.esastack.restlight.server.context.FilterContext;
import io.esastack.restlight.server.spi.Filter;
import io.esastack.restlight.server.util.DateUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @deprecated use {@link io.esastack.restlight.ext.filter.accesslog.AccessLogFilter}
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
    public CompletableFuture<Void> doFilter(FilterContext context, FilterChain<FilterContext> chain) {
        final HttpRequest request = context.request();
        final HttpResponse response = context.response();

        final long start = System.nanoTime();
        final HttpMethod method = request.method();
        final String path = fullUri ? request.uri() : request.path();
        final String httpVersion = request.httpVersion().name();
        final long contentLength = request.contentLength();
        final String remoteAddr = request.remoteAddr();
        final int remotePort = request.remotePort();
        response.onEnd(r -> {
            final String log = StringUtils.concat(
                    "\"", method.name(),
                    " ", path,
                    " ", httpVersion,
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
        return chain.doFilter(context);
    }
}
