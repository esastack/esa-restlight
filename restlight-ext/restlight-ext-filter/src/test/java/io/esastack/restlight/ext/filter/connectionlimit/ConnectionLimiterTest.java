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
import io.esastack.restlight.server.handler.ChannelConnection;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConnectionLimiterTest {

    @Test
    void testLimited() throws Exception {
        final ConnectionLimitOptions ops = ConnectionLimitOptionsConfigure.newOpts()
                .maxPerSecond(1).configured();
        final RateLimiter limiter0 = mock(RateLimiter.class);
        final ConnectionLimiter limiter = new ConnectionLimiter(ops, limiter0);
        final ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        final EmbeddedChannel channel = new EmbeddedChannel();
        final ChannelConnection connection = new ChannelConnection(channel);
        when(ctx.channel()).thenReturn(channel);
        when(limiter0.tryAcquire(1)).thenReturn(true);
        limiter.onConnectionInit(connection);
        assertTrue(ctx.channel().isActive());
        assertTrue(ctx.channel().isOpen());
        assertTrue(ctx.channel().isWritable());

        when(limiter0.tryAcquire(1)).thenReturn(false);
        limiter.onConnectionInit(connection);

        // wait the closure of this channel.
        final CountDownLatch latch = new CountDownLatch(1);
        channel.closeFuture().addListener(future -> {
            latch.countDown();
            assertFalse(ctx.channel().isActive());
            assertFalse(ctx.channel().isOpen());
            assertFalse(ctx.channel().isWritable());
        });
        latch.await();
    }

}
