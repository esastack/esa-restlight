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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class HeadersPredicateTest {

    private static HttpRequest request;

    @BeforeAll
    static void setUpRequest() {
        request = MockHttpRequest
                .aMockRequest()
                .withHeader("foo", "a")
                .withHeader("bar", "b")
                .withHeader("baz", "c")
                .withHeader("qux", "d")
                .build();
    }

    @Test
    void testSimpleExpression() {
        final HeadersPredicate predicate = HeadersPredicate.parseFrom("foo=a");
        assertNotNull(predicate);
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
    }

    @Test
    void testNegativeExpression() {
        final HeadersPredicate predicate = HeadersPredicate.parseFrom("foo!=a");
        assertNotNull(predicate);
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testMultiExpression() {
        final HeadersPredicate predicate = HeadersPredicate.parseFrom("foo=a", "bar=b", "baz=c", "qux=d");
        assertNotNull(predicate);
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
    }

    @Test
    void testComplexExpression() {
        final HeadersPredicate predicate = HeadersPredicate.parseFrom("foo=a", "bar!=b");
        assertNotNull(predicate);
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testBlankOperatorExpression() {
        final HeadersPredicate predicate = HeadersPredicate.parseFrom("foo = a");
        assertNotNull(predicate);
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testWrongExpression() {
        final HeadersPredicate predicate = HeadersPredicate.parseFrom("foo?a");
        assertNotNull(predicate);
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testMayAmbiguous() {
        assertFalse(HeadersPredicate.parseFrom("a=1").mayAmbiguousWith(null));
        assertFalse(HeadersPredicate.parseFrom("a=1").mayAmbiguousWith(new MethodPredicate(HttpMethod.GET)));

        assertTrue(HeadersPredicate.parseFrom("a=1")
                .mayAmbiguousWith(HeadersPredicate.parseFrom("a=1")));
        assertTrue(HeadersPredicate.parseFrom("a")
                .mayAmbiguousWith(HeadersPredicate.parseFrom("a")));
        assertTrue(HeadersPredicate.parseFrom("!a")
                .mayAmbiguousWith(HeadersPredicate.parseFrom("!a")));
        assertTrue(HeadersPredicate.parseFrom("a!=1")
                .mayAmbiguousWith(HeadersPredicate.parseFrom("a!=1")));
        assertTrue(HeadersPredicate.parseFrom("a=1", "content-type=text/plain")
                .mayAmbiguousWith(HeadersPredicate.parseFrom("a=1")));
        assertTrue(HeadersPredicate.parseFrom("a=1", "accept=text/plain")
                .mayAmbiguousWith(HeadersPredicate.parseFrom("a=1")));
        assertTrue(HeadersPredicate.parseFrom("a=1", "accept=text/plain", "content-type=application/json")
                .mayAmbiguousWith(HeadersPredicate.parseFrom("a=1", "accept=a/a", "content-type=b/1")));
        assertTrue(HeadersPredicate.parseFrom("a=1", "b=2")
                .mayAmbiguousWith(HeadersPredicate.parseFrom("a=1")));
        assertTrue(HeadersPredicate.parseFrom("a=1", "b=2")
                .mayAmbiguousWith(HeadersPredicate.parseFrom("a=1", "c=3")));
        assertFalse(HeadersPredicate.parseFrom("a=1")
                .mayAmbiguousWith(HeadersPredicate.parseFrom("a!=1")));
        assertFalse(HeadersPredicate.parseFrom("a=1")
                .mayAmbiguousWith(HeadersPredicate.parseFrom("!a")));
        assertFalse(HeadersPredicate.parseFrom("a=1", "b=1")
                .mayAmbiguousWith(HeadersPredicate.parseFrom("!a")));
        assertFalse(HeadersPredicate.parseFrom("a=1", "b=1")
                .mayAmbiguousWith(HeadersPredicate.parseFrom("!a", "b!=1")));
        assertFalse(HeadersPredicate.parseFrom("a=1", "content-type=a/1", "accept=b1")
                .mayAmbiguousWith(HeadersPredicate.parseFrom("!a", "b!=1", "content-type=a/1", "accept=b1")));
    }

}
