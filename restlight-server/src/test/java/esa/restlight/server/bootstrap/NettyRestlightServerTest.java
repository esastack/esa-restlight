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
package esa.restlight.server.bootstrap;

import esa.commons.NetworkUtils;
import esa.restlight.core.util.RestlightVer;
import esa.restlight.server.config.ServerOptions;
import esa.restlight.server.config.ServerOptionsConfigure;
import esa.restlight.server.handler.RestlightHandler;
import esa.restlight.server.schedule.ExecutorScheduler;
import esa.restlight.server.schedule.Schedulers;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NettyRestlightServerTest {

    @Test
    void testBootstrapServer() {
        final ServerOptions options = ServerOptionsConfigure.newOpts()
                .ioThreads(1)
                .coreBizThreads(0)
                .maxBizThreads(1)
                .configured();


        final SocketAddress address = new InetSocketAddress(NetworkUtils.selectRandomPort());
        final RestlightHandler handler = mock(RestlightHandler.class);

        final RestlightServer server = RestlightServerBootstrap.from(handler, options)
                .daemon(true)
                .withAddress(address)
                .forServer();

        assertEquals(address, server.address());
        server.await();
        assertFalse(server.isStarted());
        assertEquals(RestlightVer.version(), server.version());
        assertNull(server.ioExecutor());
        when(handler.schedulers()).thenReturn(Collections.singletonList(Schedulers.biz()));
        assertEquals(((ExecutorScheduler) Schedulers.biz()).executor(), server.bizExecutor());

        try {
            server.start();
            assertTrue(server.isStarted());
            assertNotNull(server.ioExecutor());
            server.shutdown();
        } catch (Throwable ignored) {
        }
    }

}
