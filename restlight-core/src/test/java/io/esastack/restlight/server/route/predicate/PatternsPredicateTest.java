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
package io.esastack.restlight.server.route.predicate;

import io.esastack.commons.net.http.HttpMethod;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.mock.MockHttpRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PatternsPredicateTest {

    @Test
    void testExactlyMatch() {
        final String path = "/foo";
        final PatternsPredicate predicate = new PatternsPredicate(new String[]{path});
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri(path)
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
    }

    @Test
    void testMultiPatterns() {
        final String path1 = "/foo/{f}";
        final String path2 = "/foo2/{f}";
        final PatternsPredicate predicate = new PatternsPredicate(new String[]{path1, path2});
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/foo2/bar")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
        final Map<String, String> uriVariables = context.attrs().attr(PatternsPredicate.TEMPLATE_VARIABLES).get();
        assertEquals(1, uriVariables.size());
        assertEquals("bar", uriVariables.get("f"));
    }

    @Test
    void testUnMatch() {
        final String path = "/foo";
        final String anotherPath = "/bar";
        final PatternsPredicate predicate = new PatternsPredicate(new String[]{path});
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri(anotherPath)
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testTwoAsteriskWildcardsMatch() {
        final String path = "/foo/**";
        final String anotherPath = "/foo/bar";
        final PatternsPredicate predicate = new PatternsPredicate(new String[]{path});
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri(anotherPath)
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
        final HttpRequest request1 = MockHttpRequest
                .aMockRequest()
                .withUri(anotherPath + "/baz")
                .build();
        context = new RequestContextImpl(request1, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
    }

    @Test
    void testTwoAsteriskWildcardsUnMatch() {
        final String path = "/foo/**";
        final String anotherPath = "/foo1/bar";
        final PatternsPredicate predicate = new PatternsPredicate(new String[]{path});
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri(anotherPath)
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testOneAsteriskWildcardsMatch() {
        final String path = "/foo/*/1";
        final String anotherPath = "/foo/bar/1";
        final PatternsPredicate predicate = new PatternsPredicate(new String[]{path});
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri(anotherPath)
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
    }

    @Test
    void testOneAsteriskWildcardsUnMatch() {
        final String path = "/foo/*/1";
        final String anotherPath = "/foo/1";
        final PatternsPredicate predicate = new PatternsPredicate(new String[]{path});
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri(anotherPath)
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testTemplateVariableWildcards() {
        final String path = "/foo/{bar}";
        final String anotherPath = "/foo/bar";
        final PatternsPredicate predicate = new PatternsPredicate(new String[]{path});
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri(anotherPath)
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
    }

    @Test
    void testQuestionMarkWildcardsMatch() {
        final String path = "/foo/b?r";
        final String anotherPath = "/foo/bar";
        final PatternsPredicate predicate = new PatternsPredicate(new String[]{path});
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri(anotherPath)
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
    }

    @Test
    void testQuestionMarkWildcardsUnMatch() {
        final String path = "/foo/b?r";
        final String anotherPath = "/foo/baz";
        final PatternsPredicate predicate = new PatternsPredicate(new String[]{path});
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri(anotherPath)
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testComplexWildcards() {
        final String path = "/foo/{bar}/b?z/*.qux";
        final String anotherPath = "/foo/bar/baz/test.qux";
        final PatternsPredicate predicate = new PatternsPredicate(new String[]{path});
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri(anotherPath)
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));
    }

    @Test
    void testMayAmbiguous() {

        final PatternsPredicate predicate = new PatternsPredicate(new String[]{"/abc/def/gh"});
        assertFalse(predicate.mayAmbiguousWith(null));
        assertFalse(predicate.mayAmbiguousWith(new MethodPredicate(HttpMethod.GET)));
        assertTrue(predicate.mayAmbiguousWith(predicate));

        // non-pattern
        assertTrue(new PatternsPredicate(new String[]{"abc"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"abc"})));
        assertTrue(new PatternsPredicate(new String[]{"abc/abc"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"abc/abc"})));
        assertTrue(new PatternsPredicate(new String[]{"/abc/abc"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/abc/abc"})));
        assertTrue(new PatternsPredicate(new String[]{"//abc//abc"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/abc/abc"})));
        assertTrue(new PatternsPredicate(new String[]{"/abc/abc"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"//abc//abc"})));
        assertTrue(new PatternsPredicate(new String[]{"/abc/abc/def"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"//abc//abc/def"})));
        assertFalse(new PatternsPredicate(new String[]{"/abc/def"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"abc/de"})));
        assertFalse(new PatternsPredicate(new String[]{"abc"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"def"})));
        assertFalse(new PatternsPredicate(new String[]{"/ab/def"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/abc/def"})));
        assertFalse(new PatternsPredicate(new String[]{"/abc/def/g"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/abc/def"})));

        // first parameter is pattern
        assertTrue((new PatternsPredicate(new String[]{"ab?"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"abc"}))));

        assertTrue((new PatternsPredicate(new String[]{"ab*"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"abc"}))));
        assertTrue((new PatternsPredicate(new String[]{"ab*"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"ab"}))));
        assertTrue((new PatternsPredicate(new String[]{"*"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"ab"}))));
        assertTrue((new PatternsPredicate(new String[]{"**"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"ab"}))));
        assertTrue((new PatternsPredicate(new String[]{"{name}"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"ab"}))));
        assertTrue((new PatternsPredicate(new String[]{"/ab?"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/abc"}))));
        assertTrue((new PatternsPredicate(new String[]{"/ab*"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/abc"}))));
        assertTrue((new PatternsPredicate(new String[]{"/ab*"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab"}))));
        assertTrue((new PatternsPredicate(new String[]{"/*"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab"}))));
        assertTrue((new PatternsPredicate(new String[]{"/**"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab"}))));
        assertTrue((new PatternsPredicate(new String[]{"/{name}"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab"}))));
        assertTrue((new PatternsPredicate(new String[]{"/a?c/def"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/abc/def"}))));
        assertTrue((new PatternsPredicate(new String[]{"/a??/def"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/abc/def"}))));
        assertTrue((new PatternsPredicate(new String[]{"/a??/de?"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/abc/def"}))));
        assertTrue((new PatternsPredicate(new String[]{"/a*/def*"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/abc/def"}))));
        assertTrue((new PatternsPredicate(new String[]{"/a*/*"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/abc/def"}))));
        assertTrue((new PatternsPredicate(new String[]{"/a*/d*"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/abc/def"}))));
        assertTrue((new PatternsPredicate(new String[]{"/**"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/abc/def"}))));
        assertTrue((new PatternsPredicate(new String[]{"/**/def"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/a/b/c/def"}))));
        assertTrue(new PatternsPredicate(new String[]{"/ababca/de"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab**a/de"})));
        assertFalse((new PatternsPredicate(new String[]{"/ab?"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab"}))));
        assertFalse((new PatternsPredicate(new String[]{"/ab*/de?"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/abcd/de"}))));
        assertFalse((new PatternsPredicate(new String[]{"/ab??/def"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/abcd/de"}))));
        assertFalse((new PatternsPredicate(new String[]{"/ab??/d"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/abcd/de"}))));
        assertFalse((new PatternsPredicate(new String[]{"/a**d/de"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab/ad/de"}))));

        // second parameter is pattern
        assertTrue(new PatternsPredicate(new String[]{"abc"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"ab?"})));
        assertTrue(new PatternsPredicate(new String[]{"abc"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"ab*"})));
        assertTrue(new PatternsPredicate(new String[]{"ab"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"ab*"})));
        assertTrue(new PatternsPredicate(new String[]{"ab"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"*"})));
        assertTrue(new PatternsPredicate(new String[]{"ab"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"**"})));
        assertTrue(new PatternsPredicate(new String[]{"ab"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"{name}"})));
        assertTrue(new PatternsPredicate(new String[]{"/abc"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab?"})));
        assertTrue(new PatternsPredicate(new String[]{"/abc"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab*"})));
        assertTrue(new PatternsPredicate(new String[]{"/ab"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab*"})));
        assertTrue(new PatternsPredicate(new String[]{"/ab"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/*"})));
        assertTrue(new PatternsPredicate(new String[]{"/ab"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/**"})));
        assertTrue(new PatternsPredicate(new String[]{"/ab"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/{name}"})));
        assertTrue(new PatternsPredicate(new String[]{"/abc/def"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/a?c/def"})));
        assertTrue(new PatternsPredicate(new String[]{"/abc/def"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/a??/def"})));
        assertTrue(new PatternsPredicate(new String[]{"/abc/def"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/a??/de?"})));
        assertTrue(new PatternsPredicate(new String[]{"/abc/def"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/a*/def"})));
        assertTrue(new PatternsPredicate(new String[]{"/abc/def"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/a*/def*"})));
        assertTrue(new PatternsPredicate(new String[]{"/abc/def"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/a*/*"})));
        assertTrue(new PatternsPredicate(new String[]{"/abc/def"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/a*/d*"})));
        assertTrue(new PatternsPredicate(new String[]{"/abc/def"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/**"})));
        assertTrue(new PatternsPredicate(new String[]{"/a/b/c/def"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/**/def"})));
        assertTrue(new PatternsPredicate(new String[]{"/ab**a/de"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ababca/de"})));

        assertFalse(new PatternsPredicate(new String[]{"/ab"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab?"})));
        assertFalse(new PatternsPredicate(new String[]{"/a"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab*"})));
        assertFalse(new PatternsPredicate(new String[]{"/abcd/de"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab*/de?"})));
        assertFalse(new PatternsPredicate(new String[]{"/abcd/de"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab??/def"})));
        assertFalse(new PatternsPredicate(new String[]{"/abcd/de"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab??/d"})));
        assertFalse(new PatternsPredicate(new String[]{"/ab/ad/de"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/a**d/de"})));

        // both are pattern
        assertTrue(new PatternsPredicate(new String[]{"**"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"**"})));
        assertTrue(new PatternsPredicate(new String[]{"*"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"*"})));
        assertTrue(new PatternsPredicate(new String[]{"?"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"?"})));
        assertTrue(new PatternsPredicate(new String[]{"*"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"??"})));
        assertTrue(new PatternsPredicate(new String[]{"?*"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"?? "})));
        assertTrue(new PatternsPredicate(new String[]{"/**"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/**"})));
        assertTrue(new PatternsPredicate(new String[]{"/ab?"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab?"})));
        assertTrue(new PatternsPredicate(new String[]{"/abc?"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab?d"})));
        assertTrue(new PatternsPredicate(new String[]{"/ab??"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab?"})));
        assertTrue(new PatternsPredicate(new String[]{"/ab*"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab*"})));
        assertTrue(new PatternsPredicate(new String[]{"/ab*"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab?"})));
        assertTrue(new PatternsPredicate(new String[]{"/ab*"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab"})));
        assertTrue(new PatternsPredicate(new String[]{"/ab*"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab*?"})));
        assertTrue(new PatternsPredicate(new String[]{"/ab*/d?f"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab?/de?"})));
        assertTrue(new PatternsPredicate(new String[]{"/ab*/**"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab?/de?"})));
        assertTrue(new PatternsPredicate(new String[]{"/ab*/**"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab?/de?/ss"})));
        assertTrue(new PatternsPredicate(new String[]{"/ab?/de?/ss"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab*/**"})));
        assertTrue(new PatternsPredicate(new String[]{"/ab*/**"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab?/de/ss"})));
        assertTrue(new PatternsPredicate(new String[]{"/ab?/de/ss"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab*/**"})));
        assertTrue(new PatternsPredicate(new String[]{"/**/ss"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab?/de/ss"})));
        assertTrue(new PatternsPredicate(new String[]{"/ab?/de/ss"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/**/ss"})));
        assertTrue(new PatternsPredicate(new String[]{"/**/ss"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab?/de/ssa"})));
        assertTrue(new PatternsPredicate(new String[]{"/*/ss"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab?/ss"})));
        assertTrue(new PatternsPredicate(new String[]{"/ab*d"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/a*d"})));
        assertTrue(new PatternsPredicate(new String[]{"/{name}"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/a*d"})));
        assertTrue(new PatternsPredicate(new String[]{"/??sdf"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/{name}"})));
        assertTrue(new PatternsPredicate(new String[]{"/abc/de?"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/abc/{name}"})));
        assertTrue(new PatternsPredicate(new String[]{"/abc/??"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/abc/{name}"})));
        assertTrue(new PatternsPredicate(new String[]{"/abc/**"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/abc/{name}"})));

        assertFalse(new PatternsPredicate(new String[]{"/ab?d"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/tb?d"})));
        assertFalse(new PatternsPredicate(new String[]{"/ab?d"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab?e"})));
        assertFalse(new PatternsPredicate(new String[]{"/ab**d"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab?e"})));
        assertFalse(new PatternsPredicate(new String[]{"/ab*cd"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab?e"})));
        assertFalse(new PatternsPredicate(new String[]{"/ab??f"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab??d"})));
        assertFalse(new PatternsPredicate(new String[]{"/ab??/ef"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab??"})));
        assertFalse(new PatternsPredicate(new String[]{"/ab??"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/ab??/ef"})));
        assertFalse(new PatternsPredicate(new String[]{"/*/abc"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/*/ef"})));
        assertFalse(new PatternsPredicate(new String[]{"/a*/abc"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/*/ef"})));
        assertFalse(new PatternsPredicate(new String[]{"/a*/abc"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/a*/ef"})));
        assertFalse(new PatternsPredicate(new String[]{"/*a/abc"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/a*a/ef"})));
        assertFalse(new PatternsPredicate(new String[]{"/{name}/abc"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/a*a/ef"})));
        assertFalse(new PatternsPredicate(new String[]{"/{name}/abc"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/a*a"})));
        assertFalse(new PatternsPredicate(new String[]{"/{name}/abc"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/a*a/**/def"})));

        // multi patterns
        assertTrue(new PatternsPredicate(new String[]{"/{name}/abc"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/aaa/abc", "/aaa"})));
        assertTrue(new PatternsPredicate(new String[]{"/{name}/abc", "/bcd"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/aaa/abc"})));
        assertTrue(new PatternsPredicate(new String[]{"/{name}/abc", "/bcd"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/aaa/abc", "/aaa"})));

        assertFalse(new PatternsPredicate(new String[]{"/abc", "/bcd"})
                .mayAmbiguousWith(new PatternsPredicate(new String[]{"/aaa", "/bbb"})));

    }

}
