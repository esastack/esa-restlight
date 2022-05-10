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
package io.esastack.restlight.ext.filter.cors;

import esa.commons.collection.AttributeMap;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restlight.core.filter.FilterContextImpl;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.filter.FilteringRequestImpl;
import io.esastack.restlight.core.filter.FilterChain;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.core.mock.MockHttpResponse;
import io.esastack.restlight.core.util.Futures;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsFilterTest {

    @Test
    void testNoneCors() {
        final CorsFilter filter = new CorsFilter(Collections.singletonList(CorsOptionsConfigure.defaultOpts()));
        final HttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/foo")
                .withMethod("GET")
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((context) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(new FilterContextImpl(new AttributeMap(), new FilteringRequestImpl(request), response), chain)
                .toCompletableFuture().join();
        assertTrue(endOfChain.get());
        assertFalse(response.headers().contains(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void testSimpleRequestWithDefaultOptions() {
        final CorsFilter filter = new CorsFilter(Collections.singletonList(CorsOptionsConfigure.defaultOpts()));
        final HttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/foo")
                .withMethod("GET")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8080")
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((context) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(new FilterContextImpl(new AttributeMap(), new FilteringRequestImpl(request), response), chain)
                .toCompletableFuture().join();
        assertTrue(endOfChain.get());
        assertEquals(request.headers().get(HttpHeaderNames.ORIGIN),
                response.headers().get(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals("*", response.headers().get(HttpHeaderNames.ACCESS_CONTROL_EXPOSE_HEADERS));
        assertEquals("true", response.headers().get(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        assertEquals(HttpHeaderNames.ORIGIN.toString(), response.headers().get(HttpHeaderNames.VARY));
    }

    @Test
    void testPreflighRequestWithDefaultOptions() {
        final CorsOptions opts = CorsOptionsConfigure.defaultOpts();
        final CorsFilter filter = new CorsFilter(Collections.singletonList(opts));
        final HttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/")
                .withMethod("OPTIONS")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8080")
                .withHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), "GET")
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((context) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(new FilterContextImpl(new AttributeMap(), new FilteringRequestImpl(request), response), chain)
                .toCompletableFuture().join();
        assertFalse(endOfChain.get());
        assertEquals(request.headers().get(HttpHeaderNames.ORIGIN),
                response.headers().get(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals("*", response.headers().get(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS));
        assertEquals(CorsOptions.DEFAULT_ALLOW_METHODS.stream()
                        .map(HttpMethod::name)
                        .collect(Collectors.joining(",")),
                response.headers().get(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS));
        assertEquals("true", response.headers().get(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        assertEquals(HttpHeaderNames.ORIGIN.toString(), response.headers().get(HttpHeaderNames.VARY));
        assertEquals(String.valueOf(opts.getMaxAge()), response.headers().get(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE));
    }

    @Test
    void testSimpleRequestWithSpecifiedOrigin() {
        final CorsFilter filter = new CorsFilter(Collections.singletonList(CorsOptionsConfigure.newOpts()
                .anyOrigin(false)
                .origins(Collections.singleton("http://localhost:8080"))
                .configured()));
        final HttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/foo")
                .withMethod("GET")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8080")
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((context) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(new FilterContextImpl(new AttributeMap(), new FilteringRequestImpl(request), response), chain)
                .toCompletableFuture().join();
        assertTrue(endOfChain.get());
        assertEquals(request.headers().get(HttpHeaderNames.ORIGIN),
                response.headers().get(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals("*", response.headers().get(HttpHeaderNames.ACCESS_CONTROL_EXPOSE_HEADERS));
        assertEquals("true", response.headers().get(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        assertEquals(HttpHeaderNames.ORIGIN.toString(), response.headers().get(HttpHeaderNames.VARY));
    }

    @Test
    void testSimpleRequestWithSpecifiedOriginAndCredentialsNotAllowed() {
        final CorsFilter filter = new CorsFilter(Collections.singletonList(CorsOptionsConfigure.newOpts()
                .anyOrigin(false)
                .origins(Collections.singleton("http://localhost:8080"))
                .allowCredentials(false)
                .configured()));
        final HttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/foo")
                .withMethod("GET")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8080")
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((context) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(new FilterContextImpl(new AttributeMap(), new FilteringRequestImpl(request), response), chain)
                .toCompletableFuture().join();
        assertTrue(endOfChain.get());
        assertEquals(request.headers().get(HttpHeaderNames.ORIGIN),
                response.headers().get(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals("*", response.headers().get(HttpHeaderNames.ACCESS_CONTROL_EXPOSE_HEADERS));

        final HttpRequest preflight = MockHttpRequest.aMockRequest()
                .withUri("/")
                .withMethod("OPTIONS")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8080")
                .withHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), "GET")

                .build();
        final HttpResponse response1 = MockHttpResponse.aMockResponse().build();
        endOfChain.set(false);
        filter.doFilter(new FilterContextImpl(new AttributeMap(), new FilteringRequestImpl(preflight), response1),
                chain).toCompletableFuture().join();
        assertFalse(endOfChain.get());
        assertEquals(preflight.headers().get(HttpHeaderNames.ORIGIN),
                response1.headers().get(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals(HttpHeaderNames.ORIGIN.toString(), response1.headers().get(HttpHeaderNames.VARY));

    }

    @Test
    void testSimpleRequestWithAnyOriginAndCredentialsNotAllowed() {
        final CorsFilter filter = new CorsFilter(Collections.singletonList(CorsOptionsConfigure.newOpts()
                .anyOrigin(true)
                .allowCredentials(false)
                .configured()));
        final HttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/foo")
                .withMethod("GET")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8080")
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((context) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(new FilterContextImpl(new AttributeMap(), new FilteringRequestImpl(request), response),
                chain).toCompletableFuture().join();
        assertTrue(endOfChain.get());
        assertEquals("*",
                response.headers().get(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertFalse(response.headers().contains(HttpHeaderNames.VARY));

        final HttpRequest preflight = MockHttpRequest.aMockRequest()
                .withUri("/")
                .withMethod("OPTIONS")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8080")
                .withHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), "GET")

                .build();
        final HttpResponse response1 = MockHttpResponse.aMockResponse().build();
        endOfChain.set(false);
        filter.doFilter(new FilterContextImpl(new AttributeMap(), new FilteringRequestImpl(preflight), response1),
                chain).toCompletableFuture().join();
        assertFalse(endOfChain.get());
        assertEquals("*",
                response1.headers().get(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertFalse(response1.headers().contains(HttpHeaderNames.VARY));
    }

    @Test
    void testSimpleRequestMissOrigin() {
        final CorsFilter filter = new CorsFilter(Collections.singletonList(CorsOptionsConfigure.newOpts()
                .anyOrigin(false)
                .origins(Collections.singleton("http://localhost:8080"))
                .configured()));
        final HttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/foo")
                .withMethod("GET")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8081")
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((context) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(new FilterContextImpl(new AttributeMap(), new FilteringRequestImpl(request), response), chain)
                .toCompletableFuture().join();
        assertFalse(endOfChain.get());
        assertEquals(HttpStatus.FORBIDDEN.code(), response.status());
    }

    @Test
    void testPreflightRequestMissOrigin() {
        final CorsFilter filter = new CorsFilter(Collections.singletonList(CorsOptionsConfigure.newOpts()
                .anyOrigin(false)
                .origins(Collections.singleton("http://localhost:8080"))
                .configured()));
        final HttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/")
                .withMethod("OPTIONS")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8081")
                .withHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), "GET")
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((context) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(new FilterContextImpl(new AttributeMap(), new FilteringRequestImpl(request), response), chain)
                .toCompletableFuture().join();
        assertFalse(endOfChain.get());
        assertEquals(HttpStatus.OK.code(), response.status());
        assertFalse(response.headers().contains(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void testSimpleRequestWithMultiOrigins() {
        final CorsFilter filter = new CorsFilter(Collections.singletonList(CorsOptionsConfigure.newOpts()
                .anyOrigin(false)
                .origins(new LinkedHashSet<>(Arrays.asList("http://localhost:8080", "http://localhost:8081")))
                .configured()));
        final HttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/foo")
                .withMethod("GET")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8081")
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((context) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(new FilterContextImpl(new AttributeMap(), new FilteringRequestImpl(request), response), chain)
                .toCompletableFuture().join();
        assertTrue(endOfChain.get());
        assertEquals(request.headers().get(HttpHeaderNames.ORIGIN),
                response.headers().get(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void testPreflightRequestWithMultiOrigins() {
        final CorsFilter filter = new CorsFilter(Collections.singletonList(CorsOptionsConfigure.newOpts()
                .anyOrigin(false)
                .origins(new LinkedHashSet<>(Arrays.asList("http://localhost:8080", "http://localhost:8081")))
                .configured()));
        final HttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/")
                .withMethod("OPTIONS")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8081")
                .withHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), "GET")
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((context) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(new FilterContextImpl(new AttributeMap(), new FilteringRequestImpl(request), response), chain)
                .toCompletableFuture().join();
        assertFalse(endOfChain.get());
        assertEquals(request.headers().get(HttpHeaderNames.ORIGIN),
                response.headers().get(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
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
        final HttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/foo")
                .withMethod("GET")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8081")
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((context) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(new FilterContextImpl(new AttributeMap(), new FilteringRequestImpl(request), response), chain)
                .toCompletableFuture().join();
        assertTrue(endOfChain.get());
        assertEquals(request.headers().get(HttpHeaderNames.ORIGIN),
                response.headers().get(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
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
        final HttpRequest request = MockHttpRequest.aMockRequest()
                .withUri("/")
                .withMethod("OPTIONS")
                .withHeader(HttpHeaderNames.ORIGIN.toString(), "http://localhost:8081")
                .withHeader(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), "GET")
                .build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final AtomicBoolean endOfChain = new AtomicBoolean(false);
        final FilterChain chain = ((context) -> {
            endOfChain.set(true);
            return Futures.completedFuture();
        });
        filter.doFilter(new FilterContextImpl(new AttributeMap(), new FilteringRequestImpl(request), response), chain)
                .toCompletableFuture().join();
        assertFalse(endOfChain.get());
        assertEquals(request.headers().get(HttpHeaderNames.ORIGIN),
                response.headers().get(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

}
