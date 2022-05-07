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
package io.esastack.restlight.core.route.predicate;

import io.esastack.commons.net.http.HttpMethod;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ProducesPredicateTest {

    @Test
    void testMatch() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"text/plain"}, null);
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "text/plain")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
    }

    @Test
    void testMatchNegated() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"!text/plain"}, null);
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "text/plain")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testMatchNegatedWithoutAcceptHeader() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"!text/plain"}, null);
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testMatchWithoutAcceptHeader() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"text/plain"}, null);
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
    }

    @Test
    void testMatchWildcard() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"text/*"}, null);
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "text/plain")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
    }

    @Test
    void testMultiple() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"text/plain", "application/xml"},
                null);
        HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "text/plain")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "application/xml")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
    }

    @Test
    void testMatchSingle() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"text/plain"}, null);
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "application/xml")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testMatchParseError() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"text/plain"}, null);
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "aaa")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testMatchParseErrorWithNegation() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"!text/plain"}, null);
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "aaa")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testCompatibleMediaTypes() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"*/*"}, null);
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/")
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "*/*")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
        assertNull(context.attrs().attr(ProducesPredicate.COMPATIBLE_MEDIA_TYPES).get());
    }

    @Test
    void testGetAccepts() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"application/json",
                "multipart/form-data"}, null);
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "application/json,text/plain,multipart/form-data")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
        assertEquals(2,
                (context.attrs().attr(ProducesPredicate.COMPATIBLE_MEDIA_TYPES).get()).size());
    }

    @Test
    void testMayAmbiguous() {
        assertFalse(ProducesPredicate.parseFrom(new String[]{"text/plain"}, null)
                .mayAmbiguousWith(null));
        assertFalse(ProducesPredicate.parseFrom(new String[]{"text/plain"}, null)
                .mayAmbiguousWith(new MethodPredicate(HttpMethod.GET)));

        assertTrue(ProducesPredicate.parseFrom(new String[]{"text/plain"}, null)
                .mayAmbiguousWith(ProducesPredicate.parseFrom(new String[]{"text/plain"}, null)));
        assertTrue(ProducesPredicate.parseFrom(new String[]{"text/plain", "a/1"}, null)
                .mayAmbiguousWith(ProducesPredicate.parseFrom(new String[]{"text/plain"}, null)));
        assertTrue(ProducesPredicate.parseFrom(new String[]{"text/plain"}, null)
                .mayAmbiguousWith(ProducesPredicate.parseFrom(new String[]{"text/plain", "a/1"}, null)));
        assertTrue(ProducesPredicate.parseFrom(new String[]{"text/plain", "a/1"}, null)
                .mayAmbiguousWith(ProducesPredicate.parseFrom(new String[]{"text/plain", "b/1"}, null)));
    }

}
