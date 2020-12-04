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
package esa.restlight.starter;

import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.config.RestlightOptionsConfigure;
import esa.restlight.server.handler.Filter;
import esa.restlight.server.util.Futures;
import esa.restlight.spring.Deployments4Spring;
import esa.restlight.spring.Restlight4Spring;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.unix.DomainSocketAddress;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

class ConfigurableRestlightTest {

    @Test
    void testOptions() {
        final Restlight4Spring restlight = mock(Restlight4Spring.class);
        final RestlightOptions options = RestlightOptionsConfigure.defaultOpts();
        final ConfigurableRestlight conf = new ConfigurableRestlight(restlight, options);

        assertSame(options, conf.options());

        conf.enableServerAware(true);
        verify(restlight).enableServerAware(true);

        conf.enableIoExecutorAware(true);
        verify(restlight).enableIoExecutorAware(true);

        conf.enableBizExecutorAware(true);
        verify(restlight).enableBizExecutorAware(true);

        conf.address(8080);
        assertEquals(new InetSocketAddress(8080), conf.address);
        conf.address(new InetSocketAddress(8080));
        assertEquals(new InetSocketAddress(8080), conf.address);

        conf.address("127.0.0.1", 8080);
        assertEquals(new InetSocketAddress("127.0.0.1", 8080), conf.address);

        conf.domainSocketAddress("/tmp");
        assertEquals(new DomainSocketAddress("/tmp"), conf.address);

        final Filter f = (request, response, chain) -> Futures.completedFuture();
        conf.addFilter(f);
        verify(restlight).addFilter(same(f));

        conf.addFilters(Collections.singleton(f));
        verify(restlight).addFilters(argThat(c -> c != null && c.contains(f)));

        conf.daemon(false);
        verify(restlight).daemon(false);

        final Map<ChannelOption<?>, Object> cOptions = Collections.emptyMap();
        conf.options(cOptions);
        verify(restlight).options(same(cOptions));

        conf.option(ChannelOption.SO_RCVBUF, 1);
        verify(restlight).option(same(ChannelOption.SO_RCVBUF), eq(1));


        conf.childOptions(cOptions);
        verify(restlight).childOptions(same(cOptions));

        conf.childOption(ChannelOption.SO_RCVBUF, 1);
        verify(restlight).childOption(same(ChannelOption.SO_RCVBUF), eq(1));

        final ChannelHandler handler = mock(ChannelHandler.class);
        conf.channelHandler(handler);
        verify(restlight).channelHandler(same(handler));

        final ChannelHandler handler1 = mock(ChannelHandler.class);
        conf.channelHandlers(handler, handler1);
        verify(restlight)
                .channelHandlers(same(handler), same(handler1));
        conf.channelHandlers(Collections.singleton(handler));
        verify(restlight)
                .channelHandlers(argThat((ArgumentMatcher<Collection<? extends ChannelHandler>>) t ->
                        t != null && !t.isEmpty() && t.iterator().next() == handler));

        final Deployments4Spring.Impl deployments4Spring = mock(Deployments4Spring.Impl.class);
        when(restlight.deployments()).thenReturn(deployments4Spring);
        assertSame(deployments4Spring, conf.deployments());
    }

}
