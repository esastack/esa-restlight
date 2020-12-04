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
package esa.restlight.ext.filter.cpuload;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CpuLoadProtectionFilterTest {

    @Test
    void testNormal() {
        assertNormal(CpuLoadProtectionFilter.newFilter(100D,
                100D,
                100D));
    }

    @Test
    void testOverhead() {
        assertOverhead(CpuLoadProtectionFilter.newFilter(0.00001D,
                100D,
                100D));
    }

    private void assertNormal(CpuLoadProtectionFilter filter) {
        final ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        final EmbeddedChannel channel = new EmbeddedChannel();
        when(ctx.channel()).thenReturn(channel);
        assertTrue(filter.onConnected(ctx));
        assertTrue(ctx.channel().isActive());
        assertTrue(ctx.channel().isOpen());
        assertTrue(ctx.channel().isWritable());
    }

    private void assertOverhead(CpuLoadProtectionFilter filter) {
        final ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        final EmbeddedChannel channel = new EmbeddedChannel();
        when(ctx.channel()).thenReturn(channel);
        filter.currentCpuLoad = 10;
        assertFalse(filter.onConnected(ctx));
        assertFalse(ctx.channel().isActive());
        assertFalse(ctx.channel().isOpen());
        assertFalse(ctx.channel().isWritable());
    }

}
