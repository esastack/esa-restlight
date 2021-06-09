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
package esa.restlight.starter.autoconfigure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AutoRestlightServerOptionsTest {

    @Test
    void testSetterAndGetter() {
        final AutoRestlightServerOptions options = new AutoRestlightServerOptions();
        options.setHost("127.0.1.1");
        assertEquals("127.0.1.1", options.getHost());

        options.setPort(9999);
        assertEquals(9999, options.getPort());

        options.setUnixDomainSocketFile("/abc");
        assertEquals("/abc", options.getUnixDomainSocketFile());

        options.setPrintBanner(false);
        assertFalse(options.isPrintBanner());

        final WarmUpOptions warmUp = new WarmUpOptions();
        warmUp.setEnable(true);
        warmUp.setDelay(99L);
        options.setWarmUp(warmUp);
        assertEquals(warmUp, options.getWarmUp());
    }

    @Test
    void testToString() {
        assertEquals("Options{host=null, port=8080, unixDomainSocketFile=null," +
                " http2Enable=false, useNativeTransports=false, connectorThreads=1," +
                " ioThreads=16, bizThreads=BizThreadsOptions{core=64, max=128," +
                " blockingQueueLength=512, keepAliveTimeSeconds=180}," +
                " bizTerminationTimeoutSeconds=60, validationMessageFile='null'," +
                " contextPath='null', serialize=SerializesOptions{request=" +
                "SerializeOptions{negotiation=false, negotiationParam='format'}," +
                " response=SerializeOptions{negotiation=false, negotiationParam='format'}}," +
                " compress=false, decompress=false, maxContentLength=4194304," +
                " maxInitialLineLength=4096, maxHeaderSize=8192, soBacklog=128," +
                " writeBufferHighWaterMark=-1, writeBufferLowWaterMark=-1," +
                " idleTimeSeconds=60, keepAliveEnable=true," +
                " scheduling=SchedulingOptions{defaultScheduler='BIZ', timeout={}}," +
                " route=RouteOptions{useCachedRouting=true, cacheRatio=10, computeRate=1}," +
                " ssl=SslOptions{enable=false, clientAuth=null, ciphers=[], enabledProtocols=[]," +
                " certChainPath='null', keyPath='null', keyPassword='null', trustCertsPath='null'," +
                " sessionTimeout=0, sessionCacheSize=0, handshakeTimeoutMillis=0}," +
                " warmUp=WarmUpOptions{enable=false, delay=0}, printBanner=true, ext={}}",
                new AutoRestlightServerOptions().toString());
    }

}

