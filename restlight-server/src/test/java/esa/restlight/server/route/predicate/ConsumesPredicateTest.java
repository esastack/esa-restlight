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
package esa.restlight.server.route.predicate;

import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.method.HttpMethod;
import esa.restlight.test.mock.MockAsyncRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsumesPredicateTest {

    @Test
    void testConsumesMatch() {
        final ConsumesPredicate predicate = ConsumesPredicate.parseFrom(new String[]{"text/plain"});
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                .build();
        assertTrue(predicate.test(request));
    }

    @Test
    void testNegatedConsumesMatch() {
        final ConsumesPredicate predicate = ConsumesPredicate.parseFrom(new String[]{"!text/plain"});
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                .build();
        assertFalse(predicate.test(request));
    }

    @Test
    void testConsumesWildcardMatch() {
        final ConsumesPredicate predicate = ConsumesPredicate.parseFrom(new String[]{"text/*"});
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                .build();
        assertTrue(predicate.test(request));
    }

    @Test
    void testConsumesMultipleMatch() {
        final ConsumesPredicate predicate = ConsumesPredicate.parseFrom(new String[]{"text/plain", "application/xml"});
        AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                .build();
        assertTrue(predicate.test(request));

        request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/xml")
                .build();
        assertTrue(predicate.test(request));
    }

    @Test
    void testConsumesSingleNoMatch() {
        final ConsumesPredicate predicate = ConsumesPredicate.parseFrom(new String[]{"text/plain"});
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/xml")
                .build();
        assertFalse(predicate.test(request));
    }

    @Test
    void testConsumesParseError() {
        final ConsumesPredicate predicate = ConsumesPredicate.parseFrom(new String[]{"text/plain"});
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "aaa")
                .build();
        assertFalse(predicate.test(request));
    }

    @Test
    void testConsumesErrorWithNegation() {
        final ConsumesPredicate predicate = ConsumesPredicate.parseFrom(new String[]{"!text/plain"});
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "aaa")
                .build();
        assertFalse(predicate.test(request));
    }

    @Test
    void testNoContentType() {
        final ConsumesPredicate predicate = ConsumesPredicate.parseFrom(new String[]{"text/plain"});
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        assertFalse(predicate.test(request));
    }

    @Test
    void testMayAmbiguous() {
        assertFalse(ConsumesPredicate.parseFrom(new String[]{"text/plain"}).mayAmbiguousWith(null));
        assertFalse(ConsumesPredicate.parseFrom(new String[]{"text/plain"})
                .mayAmbiguousWith(new MethodPredicate(HttpMethod.GET)));

        assertTrue(ConsumesPredicate.parseFrom(new String[]{"text/plain"})
                .mayAmbiguousWith(ConsumesPredicate.parseFrom(new String[]{"text/plain"})));
        assertTrue(ConsumesPredicate.parseFrom(new String[]{"text/plain", "a/1"})
                .mayAmbiguousWith(ConsumesPredicate.parseFrom(new String[]{"text/plain"})));
        assertTrue(ConsumesPredicate.parseFrom(new String[]{"text/plain"})
                .mayAmbiguousWith(ConsumesPredicate.parseFrom(new String[]{"text/plain", "a/1"})));
        assertTrue(ConsumesPredicate.parseFrom(new String[]{"text/plain", "a/1"})
                .mayAmbiguousWith(ConsumesPredicate.parseFrom(new String[]{"text/plain", "b/1"})));
    }

}
