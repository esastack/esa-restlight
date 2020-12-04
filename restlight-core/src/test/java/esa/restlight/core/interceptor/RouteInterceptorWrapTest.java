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

import esa.restlight.core.util.Affinity;
import esa.restlight.server.route.Route;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RouteInterceptorWrapTest {

    @Test
    void testPredicateAlwaysTrue() {
        final RouteInterceptor mock = mock(RouteInterceptor.class);
        final RouteInterceptorWrap wrap = new RouteInterceptorWrap(mock, null, null);
        assertSame(InterceptorPredicate.ALWAYS, wrap.predicate());
    }

    @Test
    void testAffinity() {
        final RouteInterceptor mock = mock(RouteInterceptor.class);
        final Route r1 = Route.route();
        final Route r2 = Route.route();
        when(mock.match(any(), same(r1))).thenReturn(true);
        when(mock.match(any(), same(r2))).thenReturn(false);
        assertEquals(Affinity.ATTACHED, new RouteInterceptorWrap(mock, null, r1).affinity());
        assertEquals(Affinity.DETACHED, new RouteInterceptorWrap(mock, null, r2).affinity());
    }

}
