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
package io.esastack.restlight.jaxrs.impl.core;

import esa.commons.collection.AttributeKey;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpHeaderValues;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.impl.HandlerMappingImpl;
import io.esastack.restlight.core.handler.method.RouteMethodInfoImpl;
import io.esastack.restlight.core.handler.method.RouteHandlerMethodImpl;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.core.mock.MockHttpResponse;
import io.esastack.restlight.core.route.Mapping;
import io.esastack.restlight.core.route.predicate.PatternsPredicate;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class UriInfoImplTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new UriInfoImpl(null, mock(RequestContext.class)));
        assertThrows(NullPointerException.class, () -> new UriInfoImpl(URI.create("/abc"), null));
        assertDoesNotThrow(() -> new UriInfoImpl(URI.create("/abc"), mock(RequestContext.class)));
    }

    @Test
    void testGetPath() {
        final HttpRequest request = MockHttpRequest.aMockRequest().withUri("/xyz/abc/def").build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext context = new RequestContextImpl(request, response);
        final URI baseUri = URI.create("http://127.0.0.1:9999/xyz");
        final UriInfo uriInfo = new UriInfoImpl(baseUri, context);
        assertEquals("/xyz/abc/def", uriInfo.getPath(true));
    }

    @Test
    void testGetPathSegments() {
        final URI baseUri = URI.create("http://127.0.0.1:9999/xyz");
        final HttpRequest request1 = MockHttpRequest.aMockRequest().withUri("").build();
        final HttpResponse response1 = MockHttpResponse.aMockResponse().build();
        final RequestContext context1 = new RequestContextImpl(request1, response1);
        final UriInfo uriInfo1 = new UriInfoImpl(baseUri, context1);
        assertTrue(uriInfo1.getPathSegments(true).isEmpty());

        final HttpRequest request2 = MockHttpRequest.aMockRequest()
                .withUri("/abc;a=v1,b1,c1;x=y1/def;m=n1,n2;p=q1").build();
        final HttpResponse response2 = MockHttpResponse.aMockResponse().build();
        final RequestContext context2 = new RequestContextImpl(request2, response2);
        final UriInfo uriInfo2 = new UriInfoImpl(baseUri, context2);
        List<PathSegment> segments = uriInfo2.getPathSegments(true);
        assertEquals(2, segments.size());

        assertEquals("abc", segments.get(0).getPath());
        final MultivaluedMap<String, String> matrixParams1 = segments.get(0).getMatrixParameters();
        assertEquals(2, matrixParams1.size());
        assertEquals(3, matrixParams1.get("a").size());
        assertEquals("v1", matrixParams1.get("a").get(0));
        assertEquals("b1", matrixParams1.get("a").get(1));
        assertEquals("c1", matrixParams1.get("a").get(2));
        assertEquals(1, matrixParams1.get("x").size());
        assertEquals("y1", matrixParams1.get("x").get(0));

        assertEquals("def", segments.get(1).getPath());
        final MultivaluedMap<String, String> matrixParams2 = segments.get(1).getMatrixParameters();
        assertEquals(2, matrixParams2.size());
        assertEquals(2, matrixParams2.get("m").size());
        assertEquals("n1", matrixParams2.get("m").get(0));
        assertEquals("n2", matrixParams2.get("m").get(1));
        assertEquals(1, matrixParams2.get("p").size());
        assertEquals("q1", matrixParams2.get("p").get(0));
    }

    @Test
    void testGetRequestUri() {
        final URI baseUri = URI.create("http://127.0.0.1:9999/xyz");
        final HttpRequest request = MockHttpRequest.aMockRequest().withUri("/def/mn/xyz").build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext context = new RequestContextImpl(request, response);
        final UriInfo uriInfo = new UriInfoImpl(baseUri, context);
        URI requestUri = uriInfo.getRequestUri();
        assertEquals("/def/mn/xyz", requestUri.toString());
    }

    @Test
    void testGetRequestUriBuilder() {
        final URI baseUri = URI.create("http://127.0.0.1:9999/xyz");
        final HttpRequest request = MockHttpRequest.aMockRequest().withUri("/def/mn/xyz").build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext context = new RequestContextImpl(request, response);
        final UriInfo uriInfo = new UriInfoImpl(baseUri, context);
        URI requestUri = uriInfo.getRequestUriBuilder().build();
        assertEquals("/def/mn/xyz", requestUri.toString());
    }

    @Test
    void testGetAbsolutePath() {
        final URI baseUri1 = URI.create("http://127.0.0.1:8080/abc/def");
        final HttpRequest request1 = MockHttpRequest.aMockRequest().withUri("mn/xyz/def?a=b&c=d").build();
        final HttpResponse response1 = MockHttpResponse.aMockResponse().build();
        final RequestContext context1 = new RequestContextImpl(request1, response1);
        final UriInfo uriInfo1 = new UriInfoImpl(baseUri1, context1);
        final URI uri1 = uriInfo1.getAbsolutePath();
        assertEquals("http://127.0.0.1:8080/abc/mn/xyz/def", uri1.toString());

        final URI baseUri2 = URI.create("http://127.0.0.1:8080/abc/def/");
        final HttpRequest request2 = MockHttpRequest.aMockRequest().withUri("mn/xyz/def?a=b&c=d").build();
        final HttpResponse response2 = MockHttpResponse.aMockResponse().build();
        final RequestContext context2 = new RequestContextImpl(request2, response2);
        final UriInfo uriInfo2 = new UriInfoImpl(baseUri2, context2);
        final URI uri2 = uriInfo2.getAbsolutePath();
        assertEquals("http://127.0.0.1:8080/abc/def/mn/xyz/def", uri2.toString());

        final URI baseUri3 = URI.create("http://127.0.0.1:8080/abc/def/");
        final HttpRequest request3 = MockHttpRequest.aMockRequest().withUri("/mn/xyz/def?a=b&c=d").build();
        final HttpResponse response3 = MockHttpResponse.aMockResponse().build();
        final RequestContext context3 = new RequestContextImpl(request3, response3);
        final UriInfo uriInfo3 = new UriInfoImpl(baseUri3, context3);
        final URI uri3 = uriInfo3.getAbsolutePath();
        assertEquals("http://127.0.0.1:8080/mn/xyz/def", uri3.toString());
    }

    @Test
    void testGetAbsolutePathBuilder() {
        final URI baseUri1 = URI.create("http://127.0.0.1:8080/abc/def");
        final HttpRequest request1 = MockHttpRequest.aMockRequest().withUri("mn/xyz/def?a=b&c=d").build();
        final HttpResponse response1 = MockHttpResponse.aMockResponse().build();
        final RequestContext context1 = new RequestContextImpl(request1, response1);
        final UriInfo uriInfo1 = new UriInfoImpl(baseUri1, context1);
        final URI uri1 = uriInfo1.getAbsolutePathBuilder().build();
        assertEquals("http://127.0.0.1:8080/abc/mn/xyz/def", uri1.toString());

        final URI baseUri2 = URI.create("http://127.0.0.1:8080/abc/def/");
        final HttpRequest request2 = MockHttpRequest.aMockRequest().withUri("mn/xyz/def?a=b&c=d").build();
        final HttpResponse response2 = MockHttpResponse.aMockResponse().build();
        final RequestContext context2 = new RequestContextImpl(request2, response2);
        final UriInfo uriInfo2 = new UriInfoImpl(baseUri2, context2);
        final URI uri2 = uriInfo2.getAbsolutePathBuilder().build();
        assertEquals("http://127.0.0.1:8080/abc/def/mn/xyz/def", uri2.toString());

        final URI baseUri3 = URI.create("http://127.0.0.1:8080/abc/def/");
        final HttpRequest request3 = MockHttpRequest.aMockRequest().withUri("/mn/xyz/def?a=b&c=d").build();
        final HttpResponse response3 = MockHttpResponse.aMockResponse().build();
        final RequestContext context3 = new RequestContextImpl(request3, response3);
        final UriInfo uriInfo3 = new UriInfoImpl(baseUri3, context3);
        final URI uri3 = uriInfo3.getAbsolutePathBuilder().build();
        assertEquals("http://127.0.0.1:8080/mn/xyz/def", uri3.toString());
    }

    @Test
    void testGetBaseUri() {
        final URI baseUri = URI.create("http://127.0.0.1:8080/abc/def");
        final HttpRequest request = MockHttpRequest.aMockRequest().withUri("mn/xyz/def?a=b&c=d").build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext context = new RequestContextImpl(request, response);
        final UriInfo uriInfo = new UriInfoImpl(baseUri, context);
        assertSame(baseUri, uriInfo.getBaseUri());
    }

    @Test
    void testGetBaseUriBuilder() {
        final URI baseUri = URI.create("http://127.0.0.1:8080/abc/def");
        final HttpRequest request = MockHttpRequest.aMockRequest().withUri("mn/xyz/def?a=b&c=d").build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext context = new RequestContextImpl(request, response);
        final UriInfo uriInfo = new UriInfoImpl(baseUri, context);
        assertEquals(baseUri, uriInfo.getBaseUriBuilder().build());
    }

    @Test
    void testGetPathParameters() {
        final URI baseUri1 = URI.create("http://127.0.0.1:8080/abc/def");
        final HttpRequest request1 = MockHttpRequest.aMockRequest().withUri("mn/xyz/def?a=b&c=d").build();
        final HttpResponse response1 = MockHttpResponse.aMockResponse().build();
        final RequestContext context1 = new RequestContextImpl(request1, response1);
        final UriInfo uriInfo1 = new UriInfoImpl(baseUri1, context1);
        assertTrue(uriInfo1.getPathParameters(true).isEmpty());

        final Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("name", "LiMing");
        pathVariables.put("age", "20");
        final URI baseUri2 = URI.create("http://127.0.0.1:8080/abc/def");
        final HttpRequest request2 = MockHttpRequest.aMockRequest().withUri("mn/xyz/def?a=b&c=d").build();
        final HttpResponse response2 = MockHttpResponse.aMockResponse().build();
        final RequestContext context2 = new RequestContextImpl(request2, response2);
        context2.attrs().attr(PatternsPredicate.TEMPLATE_VARIABLES).set(pathVariables);
        final UriInfo uriInfo2 = new UriInfoImpl(baseUri2, context2);
        MultivaluedMap<String, String> params = uriInfo2.getPathParameters(false);
        assertEquals(2, params.size());
        assertEquals(1, params.get("name").size());
        assertEquals("LiMing", params.get("name").get(0));
        assertEquals(1, params.get("age").size());
        assertEquals("20", params.get("age").get(0));
    }

    @Test
    void testGetQueryParameters() {
        final URI baseUri1 = URI.create("http://127.0.0.1:8080/abc/def");
        final HttpRequest request1 = MockHttpRequest.aMockRequest().withUri("mn/xyz/def").build();
        final HttpResponse response1 = MockHttpResponse.aMockResponse().build();
        final RequestContext context1 = new RequestContextImpl(request1, response1);
        final UriInfo uriInfo1 = new UriInfoImpl(baseUri1, context1);
        assertTrue(uriInfo1.getQueryParameters(true).isEmpty());

        final URI baseUri2 = URI.create("http://127.0.0.1:8080");
        final HttpRequest request2 = MockHttpRequest.aMockRequest()
                .withMethod(HttpMethod.POST)
                .withUri("/abc/def?a=b&c=d")
                .withHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED)
                .withBody("a=b1&c=d1".getBytes(StandardCharsets.UTF_8))
                .build();

        final HttpResponse response2 = MockHttpResponse.aMockResponse().build();
        final RequestContext context2 = new RequestContextImpl(request2, response2);
        final UriInfo uriInfo2 = new UriInfoImpl(baseUri2, context2);
        MultivaluedMap<String, String> params = uriInfo2.getQueryParameters(false);
        assertEquals(2, params.size());
        assertEquals(1, params.get("a").size());
        assertEquals("b", params.get("a").get(0));
        assertEquals(1, params.get("c").size());
        assertEquals("d", params.get("c").get(0));
    }

    @Test
    void testGetMatchedURIs() throws Throwable {
        final URI baseUri1 = URI.create("http://127.0.0.1:8080/abc/def");
        final HttpRequest request1 = MockHttpRequest.aMockRequest().withUri("mn/xyz/def").build();
        final HttpResponse response1 = MockHttpResponse.aMockResponse().build();
        final RequestContext context1 = new RequestContextImpl(request1, response1);
        final UriInfo uriInfo1 = new UriInfoImpl(baseUri1, context1);
        assertTrue(uriInfo1.getMatchedURIs(true).isEmpty());

        final URI baseUri2 = URI.create("http://127.0.0.1:8080");
        final HttpRequest request2 = MockHttpRequest.aMockRequest().withUri("/abc/def?a=b&c=d").build();
        final HttpResponse response2 = MockHttpResponse.aMockResponse().build();
        final RequestContext context2 = new RequestContextImpl(request2, response2);

        final List<HandlerMapping> mappings = new LinkedList<>();
        final Class<?> userType = Object.class;
        final Method method1 = Object.class.getMethod("equals", Object.class);
        final Object obj1 = new Object();
        mappings.add(new HandlerMappingImpl(Mapping.mapping("/abc"),
                new RouteMethodInfoImpl(RouteHandlerMethodImpl.of(userType,
                        method1, false, null),
                        false, null), obj1, null));

        final Method method2 = Object.class.getMethod("toString");
        mappings.add(new HandlerMappingImpl(Mapping.mapping("abc/def"),
                new RouteMethodInfoImpl(RouteHandlerMethodImpl.of(userType,
                        method2, false, null),
                        false, null), null, null));

        AttributeKey<List<HandlerMapping>> routeTrackingKey = AttributeKey.valueOf("internal.route.tracking");
        context2.attrs().attr(routeTrackingKey).set(mappings);
        final UriInfo uriInfo2 = new UriInfoImpl(baseUri2, context2);
        List<String> uris = uriInfo2.getMatchedURIs();
        assertEquals(2, uris.size());
        assertEquals("path=[/abc/def]", uris.get(0));
        assertEquals("path=[/abc]", uris.get(1));
    }

    @Test
    void testGetMatchedResources() throws Throwable {
        final URI baseUri1 = URI.create("http://127.0.0.1:8080/abc/def");
        final HttpRequest request1 = MockHttpRequest.aMockRequest().withUri("mn/xyz/def").build();
        final HttpResponse response1 = MockHttpResponse.aMockResponse().build();
        final RequestContext context1 = new RequestContextImpl(request1, response1);
        final UriInfo uriInfo1 = new UriInfoImpl(baseUri1, context1);
        assertTrue(uriInfo1.getMatchedResources().isEmpty());

        final URI baseUri2 = URI.create("http://127.0.0.1:8080");
        final HttpRequest request2 = MockHttpRequest.aMockRequest().withUri("/abc/def?a=b&c=d").build();
        final HttpResponse response2 = MockHttpResponse.aMockResponse().build();
        final RequestContext context2 = new RequestContextImpl(request2, response2);

        final List<HandlerMapping> mappings = new LinkedList<>();
        final Class<?> userType = Object.class;
        final Method method1 = Object.class.getMethod("equals", Object.class);
        final Object obj1 = new Object();
        mappings.add(new HandlerMappingImpl(Mapping.mapping("/abc"),
                new RouteMethodInfoImpl(RouteHandlerMethodImpl.of(userType,
                        method1, false, null),
                        false, null), obj1, null));

        final Method method2 = Object.class.getMethod("toString");
        mappings.add(new HandlerMappingImpl(Mapping.mapping("abc/def"),
                new RouteMethodInfoImpl(RouteHandlerMethodImpl.of(userType,
                        method2, false, null),
                        false, null), null, null));

        AttributeKey<List<HandlerMapping>> routeTrackingKey = AttributeKey.valueOf("internal.route.tracking");
        context2.attrs().attr(routeTrackingKey).set(mappings);
        final UriInfo uriInfo2 = new UriInfoImpl(baseUri2, context2);
        List<Object> matchedResources = uriInfo2.getMatchedResources();
        assertEquals(2, matchedResources.size());
        assertSame(method2, ((MatchedResource) matchedResources.get(0)).method().handlerMethod().method());
        assertFalse(((MatchedResource) matchedResources.get(0)).bean().isPresent());

        assertSame(method1, ((MatchedResource) matchedResources.get(1)).method().handlerMethod().method());
        assertSame(obj1, ((MatchedResource) matchedResources.get(1)).bean().get());
    }

    @Test
    void testResolve() {
        final URI baseUri1 = URI.create("http://127.0.0.1:8080/abc/def");
        final HttpRequest request1 = MockHttpRequest
                .aMockRequest()
                .build();
        final HttpResponse response1 = MockHttpResponse.aMockResponse().build();
        final RequestContext context1 = new RequestContextImpl(request1, response1);
        final UriInfo uriInfo1 = new UriInfoImpl(baseUri1, context1);
        assertEquals(URI.create("http://localhost:9090/mn/xyz/def?a=b&c=d"),
                uriInfo1.resolve(URI.create("http://localhost:9090/mn/xyz/def?a=b&c=d")));

        final URI baseUri2 = URI.create("http://127.0.0.1:8080/abc/def");
        final HttpRequest request2 = MockHttpRequest
                .aMockRequest()
                .build();
        final HttpResponse response2 = MockHttpResponse.aMockResponse().build();
        final RequestContext context2 = new RequestContextImpl(request2, response2);
        final UriInfo uriInfo2 = new UriInfoImpl(baseUri2, context2);
        assertEquals(URI.create("http://127.0.0.1:8080/mn/xyz/def?a=b&c=d"),
                uriInfo2.resolve(URI.create("/mn/xyz/def?a=b&c=d")));
    }

    @Test
    void testRelativize() {
        final URI baseUri1 = URI.create("http://example.com:8080/app/root/");
        final HttpRequest request1 = MockHttpRequest
                .aMockRequest()
                .withUri("http://example.com:8080/app/root/a/b/c/resource.html")
                .build();
        final HttpResponse response1 = MockHttpResponse
                .aMockResponse()
                .build();
        final RequestContext context1 = new RequestContextImpl(request1, response1);
        final UriInfo uriInfo1 = new UriInfoImpl(baseUri1, context1);
        assertEquals(URI.create("d/file.txt"),
                uriInfo1.relativize(URI.create("a/b/c/d/file.txt")));

        final URI baseUri2 = URI.create("http://example.com:8080/app/root/");
        final HttpRequest request2 = MockHttpRequest
                .aMockRequest()
                .withUri("http://example.com:8080/app/root/a/b/c/resource.html")
                .build();
        final HttpResponse response2 = MockHttpResponse.aMockResponse().build();
        final RequestContext context2 = new RequestContextImpl(request2, response2);
        final UriInfo uriInfo2 = new UriInfoImpl(baseUri2, context2);
        assertEquals(URI.create("http://example2.com:9090/app2/root2/a/d/file.txt"),
                uriInfo2.relativize(URI.create("http://example2.com:9090/app2/root2/a/d/file.txt")));
    }

    @Test
    void testBaseUri() {
        final URI baseUri = URI.create("http://127.0.0.1:8080/abc/def");
        final HttpRequest request = MockHttpRequest.aMockRequest().withUri("mn/xyz/def?a=b&c=d").build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext context = new RequestContextImpl(request, response);
        final UriInfoImpl uriInfo = new UriInfoImpl(baseUri, context);
        final URI updated = URI.create("/abc/def");
        uriInfo.baseUri(updated);
        assertSame(updated, uriInfo.getBaseUri());
    }

}

