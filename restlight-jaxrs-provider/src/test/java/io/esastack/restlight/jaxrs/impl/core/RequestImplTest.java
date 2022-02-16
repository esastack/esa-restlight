/*
 * Copyright 2022 OPPO ESA Stack Project
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

import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.mock.MockHttpResponse;
import io.esastack.restlight.server.util.DateUtils;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Variant;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static io.esastack.commons.net.http.HttpHeaderNames.IF_MATCH;
import static io.esastack.commons.net.http.HttpHeaderNames.IF_MODIFIED_SINCE;
import static io.esastack.commons.net.http.HttpHeaderNames.IF_NONE_MATCH;
import static io.esastack.commons.net.http.HttpHeaderNames.IF_UNMODIFIED_SINCE;
import static io.esastack.commons.net.http.HttpHeaderNames.VARY;
import static jakarta.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class RequestImplTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new RequestImpl(null));
        assertDoesNotThrow(() -> new RequestImpl(mock(RequestContext.class)));
    }

    @Test
    void testGetMethod() {
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext context = new RequestContextImpl(request, response);
        final Request req = new RequestImpl(context);
        assertEquals("GET", req.getMethod());
    }

    @Test
    void testSelectVariant() {
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext context = new RequestContextImpl(request, response);
        final Request req = new RequestImpl(context);

        assertThrows(IllegalArgumentException.class, () -> req.selectVariant(null));
        assertThrows(IllegalArgumentException.class, () -> req.selectVariant(Collections.emptyList()));

        final List<Variant> variants = new LinkedList<>();
        Variant variant1 = new Variant(MediaType.WILDCARD_TYPE, "zh-TW;q=0.7", "gzip;q=1.0");
        variants.add(variant1);
        Variant variant2 = new Variant(MediaType.APPLICATION_JSON_TYPE, "zh-TW;q=0.7",
                "gzip;q=1.0");
        variants.add(variant2);
        assertSame(variant2, req.selectVariant(variants));
        assertEquals("accept,accept-language,accept-encoding", response.headers().get(VARY));

        request.headers().clear();
        response.headers().clear();
        // case1: language doesn't match
        request.headers().add(HttpHeaderNames.ACCEPT_LANGUAGE, "en-US");
        assertNull(req.selectVariant(variants));
        assertEquals("accept,accept-language,accept-encoding", response.headers().get(VARY));

        request.headers().clear();
        response.headers().clear();
        // case2: encoding doesn't match
        request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, "deflate");
        assertNull(req.selectVariant(variants));
        assertEquals("accept,accept-language,accept-encoding", response.headers().get(VARY));

        // case3: mediaType doesn't match
        request.headers().clear();
        response.headers().clear();
        request.headers().add(HttpHeaderNames.ACCEPT, "text/html");
        assertSame(variant1, req.selectVariant(variants));

        request.headers().clear();
        response.headers().clear();
        request.headers().add(HttpHeaderNames.ACCEPT, "text/html");
        Variant variant3 = new Variant(MediaType.TEXT_HTML_TYPE, "zh-TW;q=0.7", "gzip;q=1.0");
        variants.add(variant3);
        assertSame(variant3, req.selectVariant(variants));
    }

    @Test
    void testValuatePreconditionsOfEntityTag() {
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext context = new RequestContextImpl(request, response);
        final Request req = new RequestImpl(context);

        assertThrows(IllegalArgumentException.class, () -> req.evaluatePreconditions((EntityTag) null));

        // case1: 'if-match' matches
        request.headers().add(IF_MATCH, "\"*\"");
        Response.ResponseBuilder builder = req.evaluatePreconditions(new EntityTag("abc", true));
        assertNull(builder);

        // case2: 'if-match' doesn't match
        request.headers().clear();
        request.headers().add(IF_MATCH, "\"abc\"");
        builder = req.evaluatePreconditions(new EntityTag("def", true));
        assertNotNull(builder);
        Response response2 = builder.build();
        assertEquals(PRECONDITION_FAILED.getStatusCode(), response2.getStatus());
        assertEquals("W/\"def\"", response2.getHeaderString(HttpHeaders.ETAG));

        // case3: 'if-none-match' matches
        request.headers().clear();
        request.headers().add(IF_NONE_MATCH, "\"*\"");
        builder = req.evaluatePreconditions(new EntityTag("abc", true));
        assertNotNull(builder);
        Response response3 = builder.build();
        assertEquals(Response.Status.NOT_MODIFIED.getStatusCode(), response3.getStatus());
        assertEquals("W/\"abc\"", response3.getHeaderString(HttpHeaders.ETAG));

        // case4: 'if-none-match' doesn't match
        request.headers().clear();
        request.headers().add(IF_NONE_MATCH, "\"abc\"");
        builder = req.evaluatePreconditions(new EntityTag("def", true));
        assertNull(builder);

        // case5: 'if-match' and 'if-none-match' are both absent
        request.headers().clear();
        builder = req.evaluatePreconditions(new EntityTag("def", true));
        assertNull(builder);
    }

    @Test
    void testValuatePreconditionsOfDate() {
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext context = new RequestContextImpl(request, response);
        final Request req = new RequestImpl(context);

        assertThrows(IllegalArgumentException.class, () -> req.evaluatePreconditions((Date) null));

        // case1: 'if-modified-since' matches
        final Date date = new Date();
        final Date afterDate = new Date(date.getTime() + 1000);
        final Date beforeDate = new Date(date.getTime() - 1000);

        request.headers().add(IF_MODIFIED_SINCE, DateUtils.formatByCache(date.getTime()));
        Response.ResponseBuilder builder = req.evaluatePreconditions(afterDate);
        assertNull(builder);

        // case2: 'if-modified-since' doesn't match
        builder = req.evaluatePreconditions(beforeDate);
        assertNotNull(builder);
        final Response response2 = builder.build();
        assertEquals(Response.Status.NOT_MODIFIED.getStatusCode(), response2.getStatus());

        request.headers().clear();
        // case3: 'if-unmodified-since' matches
        request.headers().add(IF_UNMODIFIED_SINCE, DateUtils.formatByCache(date.getTime()));
        builder = req.evaluatePreconditions(afterDate);
        assertNotNull(builder);
        final Response response3 = builder.build();
        assertEquals(PRECONDITION_FAILED.getStatusCode(), response3.getStatus());

        // case4: 'if-unmodified-since' doesn't match
        builder = req.evaluatePreconditions(beforeDate);
        assertNull(builder);

        // case5: 'if-modified-since' and 'if-unmodified-since' are both absent
        request.headers().clear();
        builder = req.evaluatePreconditions(date);
        assertNull(builder);
    }

    @Test
    void testValuatePreconditionsOfDateAndEntityTag() {
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext context = new RequestContextImpl(request, response);
        final Request req = new RequestImpl(context);

        assertThrows(IllegalArgumentException.class, () -> req.evaluatePreconditions(null,
                new EntityTag("abc")));
        assertThrows(IllegalArgumentException.class, () -> req.evaluatePreconditions(new Date(),
                null));

        final Date date = new Date();
        final Date afterDate = new Date(date.getTime() + 1000);
        final Date beforeDate = new Date(date.getTime() - 1000);

        // case1: only 'eTage' matches
        request.headers().add(IF_MODIFIED_SINCE, DateUtils.formatByCache(date.getTime()));
        request.headers().add(IF_MATCH, "\"abc\"");
        Response.ResponseBuilder builder = req.evaluatePreconditions(afterDate,
                new EntityTag("def", true));
        assertNotNull(builder);
        final Response response1 = builder.build();
        assertEquals(PRECONDITION_FAILED.getStatusCode(), response1.getStatus());
        assertEquals("W/\"def\"", response1.getHeaderString(HttpHeaders.ETAG));

        // case2: only 'lastModified' matches
        request.headers().clear();
        request.headers().add(IF_MATCH, "\"*\"");
        request.headers().add(IF_MODIFIED_SINCE, DateUtils.formatByCache(date.getTime()));
        builder = req.evaluatePreconditions(beforeDate, new EntityTag("abc", true));
        assertNotNull(builder);
        final Response response2 = builder.build();
        assertEquals(Response.Status.NOT_MODIFIED.getStatusCode(), response2.getStatus());

        request.headers().clear();
        // case3: 'eTage' and 'lastModified' are both mismatched.
        request.headers().add(IF_MATCH, "\"*\"");
        request.headers().add(IF_MODIFIED_SINCE, DateUtils.formatByCache(date.getTime()));
        builder = req.evaluatePreconditions(afterDate, new EntityTag("abc", true));
        assertNull(builder);

        // case4: 'eTage' and 'lastModified' are both matched.
        request.headers().clear();
        request.headers().add(IF_MATCH, "\"abc\"");
        request.headers().add(IF_MODIFIED_SINCE, DateUtils.formatByCache(date.getTime()));
        builder = req.evaluatePreconditions(afterDate, new EntityTag("def", true));
        assertNotNull(builder);
        assertEquals("W/\"def\"", builder.build().getHeaderString(HttpHeaders.ETAG));
    }

    @Test
    void testValuatePreconditions() {
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext context = new RequestContextImpl(request, response);
        final Request req = new RequestImpl(context);

        assertNull(req.evaluatePreconditions());
        request.headers().set(IF_MATCH, "");
        assertNotNull(req.evaluatePreconditions());

        request.headers().clear();
        request.headers().set(IF_MATCH, "\"abc\"");
        Response.ResponseBuilder builder = req.evaluatePreconditions();
        assertNotNull(builder);
        assertEquals(PRECONDITION_FAILED.getStatusCode(), builder.build().getStatus());
    }
}

