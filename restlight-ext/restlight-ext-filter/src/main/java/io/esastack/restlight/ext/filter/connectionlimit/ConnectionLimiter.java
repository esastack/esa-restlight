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
package io.esastack.restlight.ext.filter.connectionlimit;

import com.google.common.util.concurrent.RateLimiter;
import esa.commons.Checks;
import io.esastack.restlight.server.handler.ConnectionHandler;
import io.esastack.restlight.server.util.LoggerUtils;
import io.netty.channel.Channel;

/**
 * ConnectionLimitFilter To limit the qps of the new connections. We will refuse the new connections if current
 * qps is reaching the limitation.
 */
@SuppressWarnings("UnstableApiUsage")
public class ConnectionLimiter implements ConnectionHandler {

    private final RateLimiter connects;
    private final int permitsPerSecond;

    public ConnectionLimiter(ConnectionLimitOptions options) {
        Checks.checkNotNull(options, "options");
        Checks.checkArg(options.getMaxPerSecond() > 0, "PermitsPerSecond must be over than 0!");
        this.permitsPerSecond = options.getMaxPerSecond();
        this.connects = RateLimiter.create(permitsPerSecond);
    }

    ConnectionLimiter(ConnectionLimitOptions options, RateLimiter limiter) {
        Checks.checkNotNull(options, "options");
        Checks.checkArg(options.getMaxPerSecond() > 0, "PermitsPerSecond must be over than 0!");
        this.permitsPerSecond = options.getMaxPerSecond();
        this.connects = limiter;
    }

    @Override
    public void onConnect(Channel channel) {
        if (connects.tryAcquire(1)) {
            return;
        }
        final String conn = channel.toString();
        channel.close();
        LoggerUtils.logErrorPeriodically(
                "Connection({}) refused because the number of new connection is over than {} per-second",
                conn, permitsPerSecond);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 200;
    }

}
