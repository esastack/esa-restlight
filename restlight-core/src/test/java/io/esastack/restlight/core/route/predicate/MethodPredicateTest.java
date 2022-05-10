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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class MethodPredicateTest {

    @Test
    void testSinglePredicate() {
        final MethodPredicate predicate = new MethodPredicate(HttpMethod.GET);
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withMethod(HttpMethod.GET.name())
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
        final HttpRequest unMatchRequest = MockHttpRequest
                .aMockRequest()
                .withMethod(HttpMethod.POST.name())
                .build();
        context = new RequestContextImpl(unMatchRequest, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testMultiMethodPredicate() {
        final MethodPredicate predicate = new MethodPredicate(HttpMethod.POST, HttpMethod.PUT);
        final HttpRequest putRequest = MockHttpRequest
                .aMockRequest()
                .withMethod(HttpMethod.PUT.name())
                .build();
        final HttpRequest getRequest = MockHttpRequest
                .aMockRequest()
                .withMethod(HttpMethod.PUT.name())
                .build();
        final HttpRequest otherRequest = MockHttpRequest
                .aMockRequest()
                .withMethod(HttpMethod.GET.name())
                .build();
        RequestContext context = new RequestContextImpl(putRequest, mock(HttpResponse.class));
        assertTrue(predicate.test(context));

        context = new RequestContextImpl(getRequest, mock(HttpResponse.class));
        assertTrue(predicate.test(context));

        context = new RequestContextImpl(otherRequest, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testHeadRequestMappedToGet() {
        final MethodPredicate predicate = new MethodPredicate(HttpMethod.GET);
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withMethod(HttpMethod.HEAD.name())
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
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
