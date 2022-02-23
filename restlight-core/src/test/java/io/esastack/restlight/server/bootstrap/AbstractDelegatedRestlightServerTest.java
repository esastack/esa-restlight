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
package io.esastack.restlight.server.bootstrap;

import io.esastack.restlight.core.util.RestlightVer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbstractDelegatedRestlightServerTest {

    @Test
    void testAll() {
        final AbstractDelegatedRestlightServer delegate = new AbstractDelegatedRestlightServer() {
        };
        assertFalse(delegate.isStarted());
        assertThrows(IllegalStateException.class, delegate::start);
        assertThrows(IllegalStateException.class, delegate::shutdown);
        assertThrows(IllegalStateException.class, delegate::await);
        assertThrows(IllegalStateException.class, delegate::ioExecutor);
        assertThrows(IllegalStateException.class, delegate::bizExecutor);
        assertThrows(IllegalStateException.class, delegate::address);
        assertEquals(RestlightVer.version(), delegate.version());
        assertEquals("Restlight", delegate.serverName());
        assertNull(delegate.unWrap());

        final RestlightServer server = mock(RestlightServer.class);
        delegate.setServer(server);

        delegate.start();
        verify(server, times(1)).start();
        when(server.isStarted()).thenReturn(true);
        assertTrue(delegate.isStarted());
        delegate.shutdown();
        delegate.await();
        verify(server, times(1)).shutdown();
        verify(server, times(1)).await();

        final Executor e = r -> {
        };
        when(server.ioExecutor()).thenReturn(e);
        when(server.bizExecutor()).thenReturn(e);
        assertSame(e, delegate.ioExecutor());
        assertSame(e, delegate.bizExecutor());

        final SocketAddress address = new InetSocketAddress(8080);
        when(server.address()).thenReturn(address);
        assertSame(address, delegate.address());

        when(server.version()).thenReturn("foo");
        assertEquals("foo", delegate.version());
    }

}
