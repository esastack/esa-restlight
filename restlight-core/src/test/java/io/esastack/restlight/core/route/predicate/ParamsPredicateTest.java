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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ParamsPredicateTest {

    private static HttpRequest request;

    @BeforeAll
    static void setUpRequest() {
        request = MockHttpRequest
                .aMockRequest()
                .withParameter("foo", "a")
                .withParameter("bar", "b")
                .withParameter("baz", "c")
                .withParameter("qux", "d")
                .build();
    }

    @Test
    void testSimpleExpression() {
        final ParamsPredicate predicate = new ParamsPredicate("foo=a");
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
    }

    @Test
    void testNegativeExpression() {
        final ParamsPredicate predicate = new ParamsPredicate("foo!=a");
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testMultiExpression() {
        final ParamsPredicate predicate = new ParamsPredicate("foo=a", "bar=b", "baz=c", "qux=d");
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
    }

    @Test
    void testComplexExpression() {
        final ParamsPredicate predicate = new ParamsPredicate("foo=a", "bar!=b");
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testBlankOperatorExpression() {
        final ParamsPredicate predicate = new ParamsPredicate("foo = a");
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testWrongExpression() {
        final ParamsPredicate predicate = new ParamsPredicate("foo?a");
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testMayAmbiguous() {
        assertFalse(new ParamsPredicate("a=1").mayAmbiguousWith(null));
        assertFalse(new ParamsPredicate("a=1").mayAmbiguousWith(new MethodPredicate(HttpMethod.GET)));

        assertTrue(new ParamsPredicate("a=1")
                .mayAmbiguousWith(new ParamsPredicate("a=1")));
        assertTrue(new ParamsPredicate("a")
                .mayAmbiguousWith(new ParamsPredicate("a")));
        assertTrue(new ParamsPredicate("!a")
                .mayAmbiguousWith(new ParamsPredicate("!a")));
        assertTrue(new ParamsPredicate("a!=1")
                .mayAmbiguousWith(new ParamsPredicate("a!=1")));
        assertTrue(new ParamsPredicate("a=1", "b=2")
                .mayAmbiguousWith(new ParamsPredicate("a=1")));
        assertTrue(new ParamsPredicate("a=1", "b=2")
                .mayAmbiguousWith(new ParamsPredicate("a=1", "c=3")));
        assertFalse(new ParamsPredicate("a=1")
                .mayAmbiguousWith(new ParamsPredicate("a!=1")));
        assertFalse(new ParamsPredicate("a=1")
                .mayAmbiguousWith(new ParamsPredicate("!a")));
        assertFalse(new ParamsPredicate("a=1", "b=1")
                .mayAmbiguousWith(new ParamsPredicate("!a")));
        assertFalse(new ParamsPredicate("a=1", "b=1")
                .mayAmbiguousWith(new ParamsPredicate("!a", "b!=1")));
    }

}
