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
package esa.restlight.server.config;

import io.netty.handler.logging.LogLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerOptionsTest {

    @Test
    void testConfigure() {
        final BizThreadsOptions bizThreads = BizThreadsOptionsConfigure.newOpts()
                .core(4)
                .max(5)
                .blockingQueueLength(6)
                .keepAliveTimeSeconds(7)
                .configured();

        final ServerOptions options = ServerOptionsConfigure.newOpts()
                .http2Enable(true)
                .useNativeTransports(true)
                .connectorThreads(2)
                .ioThreads(3)
                .bizTerminationTimeoutSeconds(8)
                .compress(true)
                .decompress(true)
                .maxContentLength(9)
                .maxInitialLineLength(10)
                .maxHeaderSize(11)
                .soBacklog(12)
                .writeBufferHighWaterMark(13)
                .writeBufferLowWaterMark(14)
                .idleTimeSeconds(15)
                .keepAliveEnable(false)
                .logging(LogLevel.DEBUG)
                .scheduling(null)
                .route(null)
                .ssl(null)
                .https(null)
                .bizThreads(bizThreads)
                .configured();

        assertTrue(options.isHttp2Enable());
        assertTrue(options.isUseNativeTransports());
        assertEquals(2, options.getConnectorThreads());
        assertEquals(3, options.getIoThreads());
        assertEquals(4, options.getBizThreads().getCore());
        assertEquals(5, options.getBizThreads().getMax());
        assertEquals(6, options.getBizThreads().getBlockingQueueLength());
        assertEquals(7, options.getBizThreads().getKeepAliveTimeSeconds());
        assertEquals(8, options.getBizTerminationTimeoutSeconds());
        assertTrue(options.isCompress());
        assertTrue(options.isDecompress());

        assertEquals(9, options.getMaxContentLength());
        assertEquals(10, options.getMaxInitialLineLength());
        assertEquals(11, options.getMaxHeaderSize());
        assertEquals(12, options.getSoBacklog());
        assertEquals(13, options.getWriteBufferHighWaterMark());
        assertEquals(14, options.getWriteBufferLowWaterMark());
        assertEquals(15, options.getIdleTimeSeconds());
        assertFalse(options.isKeepAliveEnable());
        assertEquals(LogLevel.DEBUG, options.getLogging());
        assertNull(options.getScheduling());
        assertNull(options.getRoute());
        assertNull(options.getSsl());
    }

    @Test
    void testDefaultOpts() {
        final ServerOptions options = ServerOptionsConfigure.defaultOpts();
        final ServerOptions def = new ServerOptions();

        assertEquals(def.isHttp2Enable(), options.isHttp2Enable());
        assertEquals(def.isUseNativeTransports(), options.isUseNativeTransports());
        assertEquals(def.getConnectorThreads(), options.getConnectorThreads());
        assertEquals(def.getIoThreads(), options.getIoThreads());
        assertEquals(def.getBizTerminationTimeoutSeconds(), options.getBizTerminationTimeoutSeconds());
        assertEquals(def.isCompress(), options.isCompress());
        assertEquals(def.isDecompress(), options.isDecompress());
        assertEquals(def.getMaxContentLength(), options.getMaxContentLength());
        assertEquals(def.getMaxInitialLineLength(), options.getMaxInitialLineLength());
        assertEquals(def.getMaxHeaderSize(), options.getMaxHeaderSize());
        assertEquals(def.getSoBacklog(), options.getSoBacklog());
        assertEquals(def.getWriteBufferHighWaterMark(), options.getWriteBufferHighWaterMark());
        assertEquals(def.getWriteBufferLowWaterMark(), options.getWriteBufferLowWaterMark());
        assertEquals(def.getIdleTimeSeconds(), options.getIdleTimeSeconds());
        assertEquals(def.isKeepAliveEnable(), options.isKeepAliveEnable());
        assertEquals(def.getLogging(), options.getLogging());
        assertNotNull(def.getScheduling());
        assertNotNull(def.getRoute());
        assertNotNull(def.getSsl());
        assertNotNull(def.getBizThreads());
    }

}
