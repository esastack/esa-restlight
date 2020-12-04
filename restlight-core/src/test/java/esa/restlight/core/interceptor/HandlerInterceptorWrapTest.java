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
package esa.restlight.core.interceptor;

import esa.restlight.core.mock.MockContext;
import esa.restlight.core.util.Affinity;
import esa.restlight.server.route.Mapping;
import esa.restlight.server.route.Route;
import esa.restlight.test.mock.MockAsyncRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class HandlerInterceptorWrapTest {

    @Test
    void testDetached() {
        final HandlerInterceptor mock = mock(HandlerInterceptor.class);
        when(mock.includes()).thenReturn(new String[]{"/a"});
        when(mock.excludes()).thenReturn(new String[]{"/foo"});
        final Route route = Route.route(Mapping.get("foo"));
        final HandlerInterceptorWrap wrap = new HandlerInterceptorWrap(mock, MockContext.mock(), route);
        assertEquals(Affinity.DETACHED, wrap.affinity());
        assertEquals(InterceptorPredicate.NEVER, wrap.predicate());
    }

    @Test
    void testAlways() {
        final HandlerInterceptor mock = mock(HandlerInterceptor.class);
        when(mock.includes()).thenReturn(new String[]{"/foo"});
        when(mock.excludes()).thenReturn(null);
        final Route route = Route.route(Mapping.get("foo"));
        final HandlerInterceptorWrap wrap = new HandlerInterceptorWrap(mock, MockContext.mock(), route);
        assertEquals(Affinity.ATTACHED, wrap.affinity());
        assertEquals(InterceptorPredicate.ALWAYS, wrap.predicate());
    }

    @Test
    void testDetachedWithEmptyPath() {
        final HandlerInterceptor mock = mock(HandlerInterceptor.class);
        when(mock.includes()).thenReturn(null);
        when(mock.excludes()).thenReturn(null);
        final Route route = Route.route(Mapping.get());
        final HandlerInterceptorWrap wrap = new HandlerInterceptorWrap(mock, MockContext.mock(), route);
        assertEquals(Affinity.DETACHED, wrap.affinity());
        assertEquals(InterceptorPredicate.NEVER, wrap.predicate());
    }

    @Test
    void testMatchAll() {
        final HandlerInterceptor mock = mock(HandlerInterceptor.class);
        when(mock.includes()).thenReturn(null);
        when(mock.excludes()).thenReturn(null);
        final Route route = Route.route(Mapping.get("foo"));
        final HandlerInterceptorWrap wrap = new HandlerInterceptorWrap(mock, MockContext.mock(), route);
        assertEquals(Affinity.ATTACHED, wrap.affinity());
        assertEquals(InterceptorPredicate.ALWAYS, wrap.predicate());
    }

    @Test
    void testMatchEmpty() {
        final HandlerInterceptor mock = mock(HandlerInterceptor.class);
        when(mock.includes()).thenReturn(new String[]{});
        when(mock.excludes()).thenReturn(null);
        final Route route = Route.route(Mapping.get("foo"));
        final HandlerInterceptorWrap wrap = new HandlerInterceptorWrap(mock, MockContext.mock(), route);
        assertEquals(Affinity.DETACHED, wrap.affinity());
        assertEquals(InterceptorPredicate.NEVER, wrap.predicate());

        reset(mock);
        when(mock.includes()).thenReturn(null);
        when(mock.excludes()).thenReturn(new String[]{"/**"});
        final HandlerInterceptorWrap wrap1 = new HandlerInterceptorWrap(mock, MockContext.mock(), route);
        assertEquals(Affinity.DETACHED, wrap1.affinity());
        assertEquals(InterceptorPredicate.NEVER, wrap1.predicate());
    }

    @Test
    void testCertainlyIncludesPath() {
        final HandlerInterceptor mock = mock(HandlerInterceptor.class);
        when(mock.includes()).thenReturn(new String[]{"/foo/b*"});
        when(mock.excludes()).thenReturn(null);
        final Route route = Route.route(Mapping.get("/foo/bar"));
        final HandlerInterceptorWrap wrap = new HandlerInterceptorWrap(mock, MockContext.mock(), route);
        assertEquals(Affinity.ATTACHED, wrap.affinity());
        assertEquals(InterceptorPredicate.ALWAYS, wrap.predicate());
    }

    @Test
    void testCertainlyExcludesPath() {
        final HandlerInterceptor mock = mock(HandlerInterceptor.class);
        when(mock.includes()).thenReturn(null);
        when(mock.excludes()).thenReturn(new String[]{"/foo/b*"});
        final Route route = Route.route(Mapping.get("/foo/bar"));
        final HandlerInterceptorWrap wrap = new HandlerInterceptorWrap(mock, MockContext.mock(), route);
        assertEquals(Affinity.DETACHED, wrap.affinity());
        assertEquals(InterceptorPredicate.NEVER, wrap.predicate());
    }

    @Test
    void testExcludesNeverInsectWithPath() {
        final HandlerInterceptor mock = mock(HandlerInterceptor.class);
        when(mock.includes()).thenReturn(new String[]{"/foo/b*"});
        when(mock.excludes()).thenReturn(new String[]{"/baz/qu*"});
        final Route route = Route.route(Mapping.get("/foo/bar"));
        final HandlerInterceptorWrap wrap = new HandlerInterceptorWrap(mock, MockContext.mock(), route);
        assertEquals(Affinity.ATTACHED, wrap.affinity());
        assertEquals(InterceptorPredicate.ALWAYS, wrap.predicate());
    }

    @Test
    void testIncludesNeverInsectWithPath() {
        final HandlerInterceptor mock = mock(HandlerInterceptor.class);
        when(mock.includes()).thenReturn(new String[]{"/bar/q*x"});
        when(mock.excludes()).thenReturn(null);
        final Route route = Route.route(Mapping.get("/foo/bar"));
        final HandlerInterceptorWrap wrap = new HandlerInterceptorWrap(mock, MockContext.mock(), route);
        assertEquals(Affinity.DETACHED, wrap.affinity());
        assertEquals(InterceptorPredicate.NEVER, wrap.predicate());
    }

    @Test
    void testOther() {
        final HandlerInterceptor mock = mock(HandlerInterceptor.class);
        when(mock.includes()).thenReturn(new String[]{"/*o*/b*"});
        when(mock.excludes()).thenReturn(null);
        final Route route = Route.route(Mapping.get("/?o*/b?r"));
        final HandlerInterceptorWrap wrap = new HandlerInterceptorWrap(mock, MockContext.mock(), route);
        assertTrue(wrap.affinity() > 0);
        assertTrue(wrap.predicate().test(MockAsyncRequest.aMockRequest()
                .withUri("/foo1/bar")
                .build()));
    }
}
