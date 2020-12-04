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
package esa.restlight.ext.filter.cors;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.method.HttpMethod;
import esa.restlight.server.handler.FilterChain;
import esa.restlight.server.util.Futures;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CorsFilterTest {

    @Test
    void testNoneCors() {
        final CorsFilter filter = new CorsFilter(Collections.singletonList(CorsOptionsConfigure.defaultOpts()));
        final AsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/foo")
                .withMethod("GET")
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((req, res) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(request, response, chain).join();
        assertTrue(endOfChain.get());
        assertFalse(response.containsHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void testSimpleRequestWithDefaultOptions() {
        final CorsFilter filter = new CorsFilter(Collections.singletonList(CorsOptionsConfigure.defaultOpts()));
        final AsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/foo")
                .withMethod("GET")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8080")
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((req, res) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(request, response, chain).join();
        assertTrue(endOfChain.get());
        assertEquals(request.getHeader(HttpHeaderNames.ORIGIN),
                response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals("*", response.getHeader(HttpHeaderNames.ACCESS_CONTROL_EXPOSE_HEADERS));
        assertEquals("true", response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        assertEquals(HttpHeaderNames.ORIGIN.toString(), response.getHeader(HttpHeaderNames.VARY));
    }

    @Test
    void testPreflighRequestWithDefaultOptions() {
        final CorsOptions opts = CorsOptionsConfigure.defaultOpts();
        final CorsFilter filter = new CorsFilter(Collections.singletonList(opts));
        final AsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/")
                .withMethod("OPTIONS")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8080")
                .withHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), "GET")
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((req, res) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(request, response, chain).join();
        assertFalse(endOfChain.get());
        assertEquals(request.getHeader(HttpHeaderNames.ORIGIN),
                response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals("*", response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS));
        assertEquals(CorsOptions.DEFAULT_ALLOW_METHODS.stream()
                        .map(HttpMethod::name)
                        .collect(Collectors.joining(",")),
                response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS));
        assertEquals("true", response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        assertEquals(HttpHeaderNames.ORIGIN.toString(), response.getHeader(HttpHeaderNames.VARY));
        assertEquals(String.valueOf(opts.getMaxAge()), response.getHeader(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE));
    }

    @Test
    void testSimpleRequestWithSpecifiedOrigin() {
        final CorsFilter filter = new CorsFilter(Collections.singletonList(CorsOptionsConfigure.newOpts()
                .anyOrigin(false)
                .origins(Collections.singleton("http://localhost:8080"))
                .configured()));
        final AsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/foo")
                .withMethod("GET")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8080")
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((req, res) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(request, response, chain).join();
        assertTrue(endOfChain.get());
        assertEquals(request.getHeader(HttpHeaderNames.ORIGIN),
                response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals("*", response.getHeader(HttpHeaderNames.ACCESS_CONTROL_EXPOSE_HEADERS));
        assertEquals("true", response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        assertEquals(HttpHeaderNames.ORIGIN.toString(), response.getHeader(HttpHeaderNames.VARY));
    }

    @Test
    void testSimpleRequestWithSpecifiedOriginAndCredentialsNotAllowed() {
        final CorsFilter filter = new CorsFilter(Collections.singletonList(CorsOptionsConfigure.newOpts()
                .anyOrigin(false)
                .origins(Collections.singleton("http://localhost:8080"))
                .allowCredentials(false)
                .configured()));
        final AsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/foo")
                .withMethod("GET")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8080")
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((req, res) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(request, response, chain).join();
        assertTrue(endOfChain.get());
        assertEquals(request.getHeader(HttpHeaderNames.ORIGIN),
                response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals("*", response.getHeader(HttpHeaderNames.ACCESS_CONTROL_EXPOSE_HEADERS));

        final AsyncRequest preflight = MockAsyncRequest.aMockRequest()
                .withUri("/")
                .withMethod("OPTIONS")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8080")
                .withHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), "GET")

                .build();
        final AsyncResponse response1 = MockAsyncResponse.aMockResponse().build();
        endOfChain.set(false);
        filter.doFilter(preflight, response1, chain).join();
        assertFalse(endOfChain.get());
        assertEquals(preflight.getHeader(HttpHeaderNames.ORIGIN),
                response1.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals(HttpHeaderNames.ORIGIN.toString(), response1.getHeader(HttpHeaderNames.VARY));

    }

    @Test
    void testSimpleRequestWithAnyOriginAndCredentialsNotAllowed() {
        final CorsFilter filter = new CorsFilter(Collections.singletonList(CorsOptionsConfigure.newOpts()
                .anyOrigin(true)
                .allowCredentials(false)
                .configured()));
        final AsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/foo")
                .withMethod("GET")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8080")
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((req, res) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(request, response, chain).join();
        assertTrue(endOfChain.get());
        assertEquals("*",
                response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertFalse(response.containsHeader(HttpHeaderNames.VARY));

        final AsyncRequest preflight = MockAsyncRequest.aMockRequest()
                .withUri("/")
                .withMethod("OPTIONS")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8080")
                .withHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), "GET")

                .build();
        final AsyncResponse response1 = MockAsyncResponse.aMockResponse().build();
        endOfChain.set(false);
        filter.doFilter(preflight, response1, chain).join();
        assertFalse(endOfChain.get());
        assertEquals("*",
                response1.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertFalse(response1.containsHeader(HttpHeaderNames.VARY));
    }

    @Test
    void testSimpleRequestMissOrigin() {
        final CorsFilter filter = new CorsFilter(Collections.singletonList(CorsOptionsConfigure.newOpts()
                .anyOrigin(false)
                .origins(Collections.singleton("http://localhost:8080"))
                .configured()));
        final AsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/foo")
                .withMethod("GET")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8081")
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((req, res) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(request, response, chain).join();
        assertFalse(endOfChain.get());
        assertEquals(HttpResponseStatus.FORBIDDEN.code(), response.status());
    }

    @Test
    void testPreflightRequestMissOrigin() {
        final CorsFilter filter = new CorsFilter(Collections.singletonList(CorsOptionsConfigure.newOpts()
                .anyOrigin(false)
                .origins(Collections.singleton("http://localhost:8080"))
                .configured()));
        final AsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/")
                .withMethod("OPTIONS")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8081")
                .withHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), "GET")
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((req, res) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(request, response, chain).join();
        assertFalse(endOfChain.get());
        assertEquals(HttpResponseStatus.OK.code(), response.status());
        assertFalse(response.containsHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void testSimpleRequestWithMultiOrigins() {
        final CorsFilter filter = new CorsFilter(Collections.singletonList(CorsOptionsConfigure.newOpts()
                .anyOrigin(false)
                .origins(new LinkedHashSet<>(Arrays.asList("http://localhost:8080", "http://localhost:8081")))
                .configured()));
        final AsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/foo")
                .withMethod("GET")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8081")
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((req, res) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(request, response, chain).join();
        assertTrue(endOfChain.get());
        assertEquals(request.getHeader(HttpHeaderNames.ORIGIN),
                response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void testPreflightRequestWithMultiOrigins() {
        final CorsFilter filter = new CorsFilter(Collections.singletonList(CorsOptionsConfigure.newOpts()
                .anyOrigin(false)
                .origins(new LinkedHashSet<>(Arrays.asList("http://localhost:8080", "http://localhost:8081")))
                .configured()));
        final AsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/")
                .withMethod("OPTIONS")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8081")
                .withHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), "GET")
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((req, res) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(request, response, chain).join();
        assertFalse(endOfChain.get());
        assertEquals(request.getHeader(HttpHeaderNames.ORIGIN),
                response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void testSimpleRequestWithMultiOptions() {
        final CorsFilter filter = new CorsFilter(Arrays.asList(CorsOptionsConfigure.newOpts()
                        .anyOrigin(false)
                        .origins(Collections.singleton("http://localhost:8080"))
                        .configured(),
                CorsOptionsConfigure.newOpts()
                        .anyOrigin(false)
                        .origins(Collections.singleton("http://localhost:8081"))
                        .configured()));
        final AsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/foo")
                .withMethod("GET")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8081")
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((req, res) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(request, response, chain).join();
        assertTrue(endOfChain.get());
        assertEquals(request.getHeader(HttpHeaderNames.ORIGIN),
                response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void testPreflightRequestWithMultiOptions() {
        final CorsFilter filter = new CorsFilter(Arrays.asList(CorsOptionsConfigure.newOpts()
                        .anyOrigin(false)
                        .origins(Collections.singleton("http://localhost:8080"))
                        .configured(),
                CorsOptionsConfigure.newOpts()
                        .anyOrigin(false)
                        .origins(Collections.singleton("http://localhost:8081"))
                        .configured()));
        final AsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/")
                .withMethod("OPTIONS")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8081")
                .withHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), "GET")
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((req, res) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(request, response, chain).join();
        assertFalse(endOfChain.get());
        assertEquals(request.getHeader(HttpHeaderNames.ORIGIN),
                response.getHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

}
