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
package io.esastack.restlight.ext.filter;

import io.esastack.httpserver.core.HttpRequest;
import io.esastack.httpserver.core.HttpResponse;
import io.esastack.restlight.ext.filter.config.AccessLogOptionsConfigure;
import io.esastack.restlight.server.context.FilterContext;
import io.esastack.restlight.server.context.impl.FilterContextImpl;
import io.esastack.restlight.server.context.impl.FilteringRequestImpl;
import io.esastack.restlight.server.handler.FilterChain;
import io.esastack.restlight.server.util.Futures;
import io.esastack.restlight.test.mock.MockHttpRequest;
import io.esastack.restlight.test.mock.MockHttpResponse;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccessLogFilterTest {

    @Test
    void testDoFilter() {
        final AccessLogFilter filter = new AccessLogFilter(AccessLogOptionsConfigure
                .newOpts().fullUri(false).configured());
        final HttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/foo/bar")
                .withParameter("p", "1")
                .withMethod("GET")
                .withRemoteAddr("127.0.0.1")
                .withRemotePort(8081)
                .withBody("hello".getBytes(StandardCharsets.UTF_8))
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final FilterChain<FilterContext> chain = ((context) -> {
            context.response().sendResult(200);
            return Futures.completedFuture();
        });
        filter.doFilter(new FilterContextImpl(new FilteringRequestImpl(request), response), chain).join();
        assertEquals(200, response.status());
    }

}

