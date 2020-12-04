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
import esa.restlight.core.util.MediaType;
import esa.restlight.test.mock.MockAsyncRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProducesPredicateTest {

    @Test
    void testMatch() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"text/plain"});
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "text/plain")
                .build();
        assertTrue(predicate.test(request));
    }

    @Test
    void testMatchNegated() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"!text/plain"});
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "text/plain")
                .build();
        assertFalse(predicate.test(request));
    }

    @Test
    void testMatchNegatedWithoutAcceptHeader() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"!text/plain"});
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        assertFalse(predicate.test(request));
    }

    @Test
    void testMatchWithoutAcceptHeader() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"text/plain"});
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .build();
        assertTrue(predicate.test(request));
    }

    @Test
    void testMatchWildcard() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"text/*"});
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "text/plain")
                .build();
        assertTrue(predicate.test(request));
    }

    @Test
    void testMultiple() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"text/plain", "application/xml"});
        AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "text/plain")
                .build();
        assertTrue(predicate.test(request));

        request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "application/xml")
                .build();
        assertTrue(predicate.test(request));
    }

    @Test
    void testMatchSingle() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"text/plain"});
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "application/xml")
                .build();
        assertFalse(predicate.test(request));
    }

    @Test
    void testMatchParseError() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"text/plain"});
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "aaa")
                .build();
        assertFalse(predicate.test(request));
    }

    @Test
    void testMatchParseErrorWithNegation() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"!text/plain"});
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "aaa")
                .build();
        assertFalse(predicate.test(request));
    }

    @Test
    void testCompatibleMediaTypes() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"*/*"});
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withUri("/")
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "*/*")
                .build();
        predicate.test(request);
        assertNull(request.getAttribute(ProducesPredicate.COMPATIBLE_MEDIA_TYPES));
    }

    @Test
    void testGetAccepts() {
        final ProducesPredicate predicate = ProducesPredicate.parseFrom(new String[]{"application/json",
                "multipart/form-data"});
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "application/json,text/plain,multipart/form-data")
                .build();
        predicate.test(request);
        assertEquals(2,
                ((List<MediaType>) request.getAttribute(ProducesPredicate.COMPATIBLE_MEDIA_TYPES)).size());
    }

    @Test
    void testMayAmbiguous() {
        assertFalse(ProducesPredicate.parseFrom(new String[]{"text/plain"}).mayAmbiguousWith(null));
        assertFalse(ProducesPredicate.parseFrom(new String[]{"text/plain"})
                .mayAmbiguousWith(new MethodPredicate(HttpMethod.GET)));

        assertTrue(ProducesPredicate.parseFrom(new String[]{"text/plain"})
                .mayAmbiguousWith(ProducesPredicate.parseFrom(new String[]{"text/plain"})));
        assertTrue(ProducesPredicate.parseFrom(new String[]{"text/plain", "a/1"})
                .mayAmbiguousWith(ProducesPredicate.parseFrom(new String[]{"text/plain"})));
        assertTrue(ProducesPredicate.parseFrom(new String[]{"text/plain"})
                .mayAmbiguousWith(ProducesPredicate.parseFrom(new String[]{"text/plain", "a/1"})));
        assertTrue(ProducesPredicate.parseFrom(new String[]{"text/plain", "a/1"})
                .mayAmbiguousWith(ProducesPredicate.parseFrom(new String[]{"text/plain", "b/1"})));
    }

}
