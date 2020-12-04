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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParamsPredicateTest {

    private static AsyncRequest request;

    @BeforeAll
    static void setUpRequest() {
        request = MockAsyncRequest
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
        assertTrue(predicate.test(request));
    }

    @Test
    void testNegativeExpression() {
        final ParamsPredicate predicate = new ParamsPredicate("foo!=a");
        assertFalse(predicate.test(request));
    }

    @Test
    void testMultiExpression() {
        final ParamsPredicate predicate = new ParamsPredicate("foo=a", "bar=b", "baz=c", "qux=d");
        assertTrue(predicate.test(request));
    }

    @Test
    void testComplexExpression() {
        final ParamsPredicate predicate = new ParamsPredicate("foo=a", "bar!=b");
        assertFalse(predicate.test(request));
    }

    @Test
    void testBlankOperatorExpression() {
        final ParamsPredicate predicate = new ParamsPredicate("foo = a");
        assertFalse(predicate.test(request));
    }

    @Test
    void testWrongExpression() {
        final ParamsPredicate predicate = new ParamsPredicate("foo?a");
        assertFalse(predicate.test(request));
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
