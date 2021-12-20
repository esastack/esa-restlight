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
package io.esastack.restlight.ext.filter.ipwhitelist;

import esa.commons.collection.AttributeMap;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.context.impl.FilterContextImpl;
import io.esastack.restlight.server.core.impl.FilteringRequestImpl;
import io.esastack.restlight.server.handler.FilterChain;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.mock.MockHttpResponse;
import io.esastack.restlight.server.util.Futures;
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

        assertAllowed(MockHttpRequest.aMockRequest().withRemoteAddr("127.0.0.1").build(), nonCached);
        assertAllowed(MockHttpRequest.aMockRequest().withRemoteAddr("127.0.0.1").build(), cached);
        assertNotAllowed(MockHttpRequest.aMockRequest().withRemoteAddr("127.0.0.2").build(), nonCached);
        assertNotAllowed(MockHttpRequest.aMockRequest().withRemoteAddr("127.0.0.2").build(), cached);
        // test again for hitting cache
        assertNotAllowed(MockHttpRequest.aMockRequest().withRemoteAddr("127.0.0.2").build(), cached);

        assertAllowed(MockHttpRequest.aMockRequest().withRemoteAddr("10.11.11.123").build(), nonCached);
        // test again for hitting cache
        assertAllowed(MockHttpRequest.aMockRequest().withRemoteAddr("10.11.11.123").build(), cached);
        assertAllowed(MockHttpRequest.aMockRequest().withRemoteAddr("10.11.11.123").build(), cached);

        assertNotAllowed(MockHttpRequest.aMockRequest().withRemoteAddr("127.0.0.2").build(), nonCached);
        assertNotAllowed(MockHttpRequest.aMockRequest().withRemoteAddr("127.0.0.2").build(), cached);
        // test again for hitting cache
        assertNotAllowed(MockHttpRequest.aMockRequest().withRemoteAddr("127.0.0.2").build(), cached);
    }

    private static void assertAllowed(HttpRequest request, IpWhiteListFilter filter) {
        final FilterChain chain = ((context) -> {
            context.response().status(200);
            return Futures.completedFuture();
        });
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        filter.doFilter(new FilterContextImpl(new AttributeMap(), new FilteringRequestImpl(request), response), chain);
        assertEquals(200, response.status());
    }

    private static void assertNotAllowed(HttpRequest request, IpWhiteListFilter filter) {
        final FilterChain chain = ((context) -> {
            context.response().status(200);
            return Futures.completedFuture();
        });
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        filter.doFilter(new FilterContextImpl(new AttributeMap(), new FilteringRequestImpl(request), response), chain);
        assertEquals(401, response.status());
        assertEquals(MediaType.TEXT_PLAIN.value(), response.headers().get(HttpHeaderNames.CONTENT_TYPE));
    }
}
