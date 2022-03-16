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
package io.esastack.restlight.server.config;

import io.netty.handler.ssl.ClientAuth;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class SslOptionsTest {

    @Test
    void testConfigure() {
        final SslOptions options = SslOptionsConfigure.newOpts()
                .enable(true)
                .clientAuth(ClientAuth.NONE)
                .ciphers(Arrays.asList("a", "b"))
                .enabledProtocols(Arrays.asList("v1", "v2"))
                .certChainPath("foo")
                .keyPath("bar")
                .keyPassword("psd")
                .trustCertsPath("baz")
                .sessionTimeout(1)
                .sessionCacheSize(2)
                .handshakeTimeoutMillis(3)
                .configured();

        assertTrue(options.isEnable());
        assertEquals(ClientAuth.NONE, options.getClientAuth());
        assertArrayEquals(new String[]{"a", "b"}, options.getCiphers().toArray());
        assertArrayEquals(new String[]{"v1", "v2"}, options.getEnabledProtocols().toArray());
        assertEquals("foo", options.getCertChainPath());
        assertEquals("bar", options.getKeyPath());
        assertEquals("psd", options.getKeyPassword());
        assertEquals("baz", options.getTrustCertsPath());
        assertEquals(1, options.getSessionTimeout());
        assertEquals(2, options.getSessionCacheSize());
        assertEquals(3, options.getHandshakeTimeoutMillis());
    }

    @Test
    void testDefaultOpts() {
        final SslOptions options = SslOptionsConfigure.defaultOpts();
        final SslOptions def = new SslOptions();

        assertEquals(def.isEnable(), options.isEnable());
        assertEquals(def.getClientAuth(), options.getClientAuth());
        assertEquals(def.getCiphers(), options.getCiphers());
        assertEquals(def.getEnabledProtocols(), options.getEnabledProtocols());
        assertEquals(def.getCertChainPath(), options.getCertChainPath());
        assertEquals(def.getKeyPath(), options.getKeyPath());
        assertEquals(def.getKeyPassword(), options.getKeyPassword());
        assertEquals(def.getTrustCertsPath(), options.getTrustCertsPath());
        assertEquals(def.getSessionTimeout(), options.getSessionTimeout());
        assertEquals(def.getSessionCacheSize(), options.getSessionCacheSize());
        assertEquals(def.getHandshakeTimeoutMillis(), options.getHandshakeTimeoutMillis());
    }

}
