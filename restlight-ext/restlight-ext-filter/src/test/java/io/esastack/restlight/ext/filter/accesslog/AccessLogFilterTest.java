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
package io.esastack.restlight.ext.filter.accesslog;

import esa.commons.collection.AttributeMap;
import esa.commons.logging.InternalLogger;
import io.esastack.restlight.server.context.impl.FilterContextImpl;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.impl.FilteringRequestImpl;
import io.esastack.restlight.server.handler.FilterChain;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.mock.MockHttpResponse;
import io.esastack.restlight.server.util.Futures;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccessLogFilterTest {

    @Test
    void testBuildLogger() {
        final AccessLogOptions opts =
                AccessLogOptionsConfigure.newOpts()
                        .directory("foo")
                        .fileName("test.log")
                        .rolling(true)
                        .charset(StandardCharsets.UTF_8.name())
                        .fullUri(false)
                        .datePattern("yyyy-MM-dd")
                        .maxHistory(4)
                        .configured();

        final AccessLogOptions mock = mock(AccessLogOptions.class);
        when(mock.getCharset()).thenReturn(opts.getCharset());
        when(mock.isRolling()).thenReturn(opts.isRolling());
        when(mock.getDatePattern()).thenReturn(opts.getDatePattern());
        when(mock.getDirectory()).thenReturn(opts.getDirectory());
        when(mock.getFileName()).thenReturn(opts.getFileName());
        when(mock.getMaxHistory()).thenReturn(opts.getMaxHistory());
        when(mock.isFullUri()).thenReturn(opts.isFullUri());
        assertNotNull(AccessLogFilter.forLogger(mock));
        verify(mock, atLeastOnce()).getCharset();
        verify(mock, atLeastOnce()).getDatePattern();
        verify(mock, atLeastOnce()).getDirectory();
        verify(mock, atLeastOnce()).getFileName();
        verify(mock, atLeastOnce()).getMaxHistory();
        verify(mock, never()).isFullUri();
    }

    @Test
    void testDoFilter() {
        final InternalLogger mock = mock(InternalLogger.class);
        final AccessLogFilter filter = new AccessLogFilter(mock, true);
        final HttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/foo/bar")
                .withParameter("p", "1")
                .withMethod("GET")
                .withRemoteAddr("127.0.0.1")
                .withRemotePort(8081)
                .withBody("hello".getBytes(StandardCharsets.UTF_8))
                .build();
        final MockHttpResponse response = MockHttpResponse.aMockResponse().build();
        final FilterChain chain = ((ctx) -> {
            ctx.response().status(200);
            return Futures.completedFuture();
        });
        filter.doFilter(new FilterContextImpl(new AttributeMap(), new FilteringRequestImpl(request), response),
                chain).join();
        assertEquals(200, response.status());
        response.callEndListener();
        verify(mock).info(argThat(s -> s.contains(request.rawMethod())
                && s.contains("contentLength=" + request.contentLength())
                && s.contains("remoteAddr=" + request.remoteAddr())
                && s.contains("remotePort=" + request.remotePort())
                && s.contains("code=" + response.status())));
    }

}
