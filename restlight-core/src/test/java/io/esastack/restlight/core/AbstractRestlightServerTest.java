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
package io.esastack.restlight.core;

import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.config.RestlightOptionsConfigure;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpServerCodec;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractRestlightServerTest {

    @Test
    void testBaseOperations() {
        final Server server = new Server(RestlightOptionsConfigure.defaultOpts());
        server.name("Server");
        assertEquals("Server", server.name());

        server.address(InetSocketAddress.createUnresolved("127.0.0.1", 8989));
        server.start();
        assertEquals(InetSocketAddress.createUnresolved("127.0.0.1", 8989), server.address());

        server.address(9999);
        server.start();
        assertEquals(9999, ((InetSocketAddress) server.address()).getPort());

        server.address("127.0.0.2", 6666);
        server.start();
        assertEquals("127.0.0.2", ((InetSocketAddress) server.address()).getHostString());
        assertEquals(6666, ((InetSocketAddress) server.address()).getPort());

        server.domainSocketAddress("/abc");
        server.start();
        assertTrue(server.address() instanceof DomainSocketAddress);
        assertEquals("/abc", ((DomainSocketAddress) server.address()).path());

        server.options(Collections.singletonMap(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT));
        server.option(ChannelOption.AUTO_CLOSE, true);
        server.childOption(ChannelOption.AUTO_READ, true);
        server.childOptions(Collections.singletonMap(ChannelOption.AUTO_READ, false));
        server.channelHandler(new HttpClientCodec());
        server.channelHandlers(new HttpServerCodec(), new HttpRequestDecoder());
        server.channelHandlers(Collections.singleton(new HttpResponseDecoder()));
    }

    private static class Server extends AbstractRestlight {
        private Server(RestlightOptions options) {
            super(options);
        }

        @Override
        protected DeploymentsImpl createDeployments() {
            return new DeploymentsImpl(this, options);
        }

        @Override
        public synchronized void start() {
            setServer(doBuildServer(deployments().applyDeployments()));
        }
    }

    private static class DeploymentsImpl extends Deployments {
        private DeploymentsImpl(Server server, RestlightOptions options) {
            super(server, options);
        }
    }
}

