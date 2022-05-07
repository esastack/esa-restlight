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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ConsumesPredicateTest {

    @Test
    void testConsumesMatch() {
        final ConsumesPredicate predicate = ConsumesPredicate.parseFrom(new String[]{"text/plain"}, null);
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
    }

    @Test
    void testNegatedConsumesMatch() {
        final ConsumesPredicate predicate = ConsumesPredicate.parseFrom(new String[]{"!text/plain"}, null);
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testConsumesWildcardMatch() {
        final ConsumesPredicate predicate = ConsumesPredicate.parseFrom(new String[]{"text/*"}, null);
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
    }

    @Test
    void testConsumesMultipleMatch() {
        final ConsumesPredicate predicate = ConsumesPredicate.parseFrom(new String[]{"text/plain", "application/xml"},
                null);
        HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/xml")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
    }

    @Test
    void testConsumesSingleNoMatch() {
        final ConsumesPredicate predicate = ConsumesPredicate.parseFrom(new String[]{"text/plain"}, null);
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/xml")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testConsumesParseError() {
        final ConsumesPredicate predicate = ConsumesPredicate.parseFrom(new String[]{"text/plain"}, null);
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "aaa")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testConsumesErrorWithNegation() {
        final ConsumesPredicate predicate = ConsumesPredicate.parseFrom(new String[]{"!text/plain"}, null);
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "aaa")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testNoContentType() {
        final ConsumesPredicate predicate = ConsumesPredicate.parseFrom(new String[]{"text/plain"}, null);
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testMayAmbiguous() {
        assertFalse(ConsumesPredicate.parseFrom(new String[]{"text/plain"}, null)
                .mayAmbiguousWith(null));
        assertFalse(ConsumesPredicate.parseFrom(new String[]{"text/plain"}, null)
                .mayAmbiguousWith(new MethodPredicate(HttpMethod.GET)));

        assertTrue(ConsumesPredicate.parseFrom(new String[]{"text/plain"}, null)
                .mayAmbiguousWith(ConsumesPredicate.parseFrom(new String[]{"text/plain"}, null)));
        assertTrue(ConsumesPredicate.parseFrom(new String[]{"text/plain", "a/1"}, null)
                .mayAmbiguousWith(ConsumesPredicate.parseFrom(new String[]{"text/plain"}, null)));
        assertTrue(ConsumesPredicate.parseFrom(new String[]{"text/plain"}, null)
                .mayAmbiguousWith(ConsumesPredicate.parseFrom(new String[]{"text/plain", "a/1"}, null)));
        assertTrue(ConsumesPredicate.parseFrom(new String[]{"text/plain", "a/1"}, null)
                .mayAmbiguousWith(ConsumesPredicate.parseFrom(new String[]{"text/plain", "b/1"}, null)));
    }

}
