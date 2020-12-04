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
package esa.restlight.ext.filter.accesslog;

import esa.commons.logging.InternalLogger;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.server.handler.FilterChain;
import esa.restlight.server.util.Futures;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

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
        final AsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/foo/bar")
                .withParameter("p", "1")
                .withMethod("GET")
                .withRemoteAddr("127.0.0.1")
                .withRemotePort(8081)
                .withProtocol(HttpVersion.HTTP_1_1)
                .withBody("hello".getBytes(StandardCharsets.UTF_8))
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final FilterChain chain = ((req, res) -> {
            res.sendResult(200);
            return Futures.completedFuture();
        });
        filter.doFilter(request, response, chain).join();
        assertEquals(200, response.status());
        verify(mock).info(argThat(s -> s.contains(request.rawMethod())
                && s.contains(request.protocol())
                && s.contains("contentLength=" + request.contentLength())
                && s.contains("remoteAddr=" + request.remoteAddr())
                && s.contains("remotePort=" + request.remotePort())
                && s.contains("code=" + response.status())));
    }

}
