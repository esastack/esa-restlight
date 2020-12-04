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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MethodPredicateTest {

    @Test
    void testSinglePredicate() {
        final MethodPredicate predicate = new MethodPredicate(HttpMethod.GET);
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withMethod(HttpMethod.GET.name())
                .build();
        assertTrue(predicate.test(request));
        final AsyncRequest unMatchRequest = MockAsyncRequest
                .aMockRequest()
                .withMethod(HttpMethod.POST.name())
                .build();
        assertFalse(predicate.test(unMatchRequest));
    }

    @Test
    void testMultiMethodPredicate() {
        final MethodPredicate predicate = new MethodPredicate(HttpMethod.POST, HttpMethod.PUT);
        final AsyncRequest putRequest = MockAsyncRequest
                .aMockRequest()
                .withMethod(HttpMethod.PUT.name())
                .build();
        final AsyncRequest getRequest = MockAsyncRequest
                .aMockRequest()
                .withMethod(HttpMethod.PUT.name())
                .build();
        final AsyncRequest otherRequest = MockAsyncRequest
                .aMockRequest()
                .withMethod(HttpMethod.GET.name())
                .build();
        assertTrue(predicate.test(putRequest));
        assertTrue(predicate.test(getRequest));
        assertFalse(predicate.test(otherRequest));
    }

    @Test
    void testHeadRequestMappedToGet() {
        final MethodPredicate predicate = new MethodPredicate(HttpMethod.GET);
        final AsyncRequest request = MockAsyncRequest
                .aMockRequest()
                .withMethod(HttpMethod.HEAD.name())
                .build();
        assertTrue(predicate.test(request));
    }

    @Test
    void testNullConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new MethodPredicate(null));
    }

    @Test
    void testMayAmbiguous() {
        assertFalse(new MethodPredicate(HttpMethod.GET).mayAmbiguousWith(null));
        assertFalse(new MethodPredicate(HttpMethod.GET)
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"a"})));

        assertTrue(new MethodPredicate(HttpMethod.GET)
                .mayAmbiguousWith(new MethodPredicate(HttpMethod.GET)));
        assertTrue(new MethodPredicate(HttpMethod.GET, HttpMethod.POST)
                .mayAmbiguousWith(new MethodPredicate(HttpMethod.POST)));
        assertTrue(new MethodPredicate(HttpMethod.GET, HttpMethod.POST)
                .mayAmbiguousWith(new MethodPredicate(HttpMethod.POST, HttpMethod.PUT)));

        assertFalse(new MethodPredicate(HttpMethod.GET)
                .mayAmbiguousWith(new MethodPredicate(HttpMethod.PUT)));
        assertFalse(new MethodPredicate(HttpMethod.GET, HttpMethod.POST)
                .mayAmbiguousWith(new MethodPredicate(HttpMethod.DELETE, HttpMethod.PUT)));

    }

}
