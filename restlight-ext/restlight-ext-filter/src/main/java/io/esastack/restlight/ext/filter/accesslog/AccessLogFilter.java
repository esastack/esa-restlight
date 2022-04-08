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
package io.esastack.restlight.ext.filter.accesslog;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.logging.InternalLogger;
import esa.commons.logging.InternalLoggers;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import io.esastack.restlight.server.context.FilterContext;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.handler.Filter;
import io.esastack.restlight.server.handler.FilterChain;
import io.esastack.restlight.server.util.DateUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class AccessLogFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(AccessLogFilter.class);
    private final InternalLogger logger;
    private final boolean fullUri;

    public AccessLogFilter(AccessLogOptions options) {
        this(forLogger(options).build(), options.isFullUri());
    }

    AccessLogFilter(InternalLogger logger, boolean fullUri) {
        Checks.checkNotNull(logger);
        this.logger = logger;
        this.fullUri = fullUri;
    }

    static InternalLoggers.Builder forLogger(AccessLogOptions options) {
        InternalLoggers.Builder builder = InternalLoggers.logger("io.esastack.restlight.accesslog",
                new File(options.getDirectory(), options.getFileName()))
                .pattern("%msg%n");
        if (options.isRolling() && options.getMaxHistory() > 0) {
            builder.useTimeBasedRolling(options.getDatePattern(), options.getMaxHistory());
        }
        if (StringUtils.isNotEmpty(options.getCharset())) {
            builder.charset(Charset.forName(options.getCharset()));
        }
        if (log.isDebugEnabled()) {
            log.debug("Start access logger and append to '{}'",
                    new File(options.getDirectory(), options.getFileName()).getAbsolutePath());
        }
        return builder;
    }

    @Override
    public CompletionStage<Void> doFilter(FilterContext context, FilterChain chain) {
        final HttpRequest request = context.request();

        final io.esastack.commons.net.http.HttpMethod method = request.method();
        final String path = fullUri ? request.uri() : request.path();
        final long contentLength = request.contentLength();
        final String scheme = request.scheme();
        final String remoteAddr = request.remoteAddr();
        final int remotePort = request.remotePort();
        final long start = System.nanoTime();
        context.onEnd(ctx -> {
            final String log = StringUtils.concat(
                    DateUtils.now(),
                    " [", method.name(),
                    " ", path,
                    " ", scheme,
                    "] contentLength=", String.valueOf(contentLength),
                    ", remoteAddr=", remoteAddr,
                    ", remotePort=", String.valueOf(remotePort),
                    ", code=", String.valueOf(ctx.response().status()),
                    ", duration=",
                    String.valueOf(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)), " mills");

            logger.info(log);
        });
        return chain.doFilter(context);
    }
}
