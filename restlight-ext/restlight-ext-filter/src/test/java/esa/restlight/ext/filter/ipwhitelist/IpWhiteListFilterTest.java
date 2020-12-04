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
package esa.restlight.ext.filter.ipwhitelist;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.util.MediaType;
import esa.restlight.server.handler.FilterChain;
import esa.restlight.server.util.Futures;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IpWhiteListFilterTest {

    @Test
    void testDoFilter() {
        final List<String> ips = new ArrayList<>(2);
        ips.add("127.0.0.1");
        ips.add("regex:10.\\d{2}.1[0-3]{1}.*");
        final IpWhiteListOptions options = IpWhiteListOptionsConfigure.newOpts()
                .ips(ips)
                .cacheSize(0)
                .configured();
        final IpWhiteListFilter nonCached = new IpWhiteListFilter(options);
        final IpWhiteListOptions options1 = IpWhiteListOptionsConfigure.newOpts()
                .ips(ips)
                .cacheSize(512)
                .configured();
        final IpWhiteListFilter cached = new IpWhiteListFilter(options1);

        assertAllowed(MockAsyncRequest.aMockRequest().withRemoteAddr("127.0.0.1").build(), nonCached);
        assertAllowed(MockAsyncRequest.aMockRequest().withRemoteAddr("127.0.0.1").build(), cached);
        assertNotAllowed(MockAsyncRequest.aMockRequest().withRemoteAddr("127.0.0.2").build(), nonCached);
        assertNotAllowed(MockAsyncRequest.aMockRequest().withRemoteAddr("127.0.0.2").build(), cached);
        // test again for hitting cache
        assertNotAllowed(MockAsyncRequest.aMockRequest().withRemoteAddr("127.0.0.2").build(), cached);

        assertAllowed(MockAsyncRequest.aMockRequest().withRemoteAddr("10.11.11.123").build(), nonCached);
        // test again for hitting cache
        assertAllowed(MockAsyncRequest.aMockRequest().withRemoteAddr("10.11.11.123").build(), cached);
        assertAllowed(MockAsyncRequest.aMockRequest().withRemoteAddr("10.11.11.123").build(), cached);

        assertNotAllowed(MockAsyncRequest.aMockRequest().withRemoteAddr("127.0.0.2").build(), nonCached);
        assertNotAllowed(MockAsyncRequest.aMockRequest().withRemoteAddr("127.0.0.2").build(), cached);
        // test again for hitting cache
        assertNotAllowed(MockAsyncRequest.aMockRequest().withRemoteAddr("127.0.0.2").build(), cached);
    }

    private static void assertAllowed(AsyncRequest request, IpWhiteListFilter filter) {
        final FilterChain chain = ((req, res) -> {
            res.sendResult(200);
            return Futures.completedFuture();
        });
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        filter.doFilter(request, response, chain);
        assertEquals(200, response.status());
    }

    private static void assertNotAllowed(AsyncRequest request, IpWhiteListFilter filter) {
        final FilterChain chain = ((req, res) -> {
            res.sendResult(200);
            return Futures.completedFuture();
        });
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        filter.doFilter(request, response, chain);
        assertEquals(401, response.status());
        assertEquals(MediaType.TEXT_PLAIN.value(), response.getHeader(HttpHeaderNames.CONTENT_TYPE));
    }
}
