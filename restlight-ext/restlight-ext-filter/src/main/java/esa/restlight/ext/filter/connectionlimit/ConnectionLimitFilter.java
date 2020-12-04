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
package esa.restlight.ext.filter.connectionlimit;

import com.google.common.util.concurrent.RateLimiter;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.server.handler.Filter;
import esa.restlight.server.handler.FilterChain;
import esa.restlight.server.util.LoggerUtils;
import io.netty.channel.ChannelHandlerContext;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * ConnectionLimitFilter To limit the qps of the new connections. We will refuse the new connections if current
 * qps is reaching the limitation.
 */
@SuppressWarnings("UnstableApiUsage")
public class ConnectionLimitFilter implements Filter {

    private final RateLimiter connects;
    private final int permitsPerSecond;

    public ConnectionLimitFilter(ConnectionLimitOptions options) {
        Objects.requireNonNull(options, "ConnectionLimitOptions must not be null!");
        if (options.getMaxPerSecond() <= 0) {
            throw new IllegalArgumentException("PermitsPerSecond must over than 0!");
        }
        this.permitsPerSecond = options.getMaxPerSecond();
        this.connects = RateLimiter.create((double) permitsPerSecond);
    }

    @Override
    public boolean onConnected(ChannelHandlerContext ctx) {
        if (connects.tryAcquire(1)) {
            return true;
        }
        final String conn = ctx.channel().toString();
        ctx.channel().close();
        LoggerUtils.logErrorPeriodically(
                "Connection({}) refused because the number of new connection is over than {} per-second",
                conn, permitsPerSecond);
        return false;
    }

    @Override
    public CompletableFuture<Void> doFilter(AsyncRequest request,
                                            AsyncResponse response,
                                            FilterChain chain) {
        return chain.doFilter(request, response);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 200;
    }
}
