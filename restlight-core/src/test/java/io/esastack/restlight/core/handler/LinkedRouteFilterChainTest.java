/*
 * Copyright 2022 OPPO ESA Stack Project
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
package io.esastack.restlight.core.handler;

import esa.commons.collection.AttributeKey;
import esa.commons.collection.AttributeMap;
import io.esastack.restlight.core.filter.RouteContext;
import io.esastack.restlight.core.filter.RouteContextImpl;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.filter.RoutedRequest;
import io.esastack.restlight.core.filter.LinkedRouteFilterChain;
import io.esastack.restlight.core.filter.RouteFilter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class LinkedRouteFilterChainTest {

    @Test
    void testDoNext() {
        final List<RouteFilter> filters = new ArrayList<>();
        filters.add((mapping, context, next) -> {
            context.attrs().attr(AttributeKey.stringKey("1")).set("1");
            return next.doNext(mapping, context);
        });
        filters.add((mapping, context, next) -> {
            if ("1".equals(context.attrs().attr(AttributeKey.stringKey("1")).get())) {
                context.attrs().attr(AttributeKey.stringKey("2")).set("2");
                return next.doNext(mapping, context);
            }
            throw new IllegalStateException();
        });

        filters.add((mapping, context, next) -> {
            if ("1".equals(context.attrs().attr(AttributeKey.stringKey("1")).get()) &&
                    "2".equals(context.attrs().attr(AttributeKey.stringKey("2")).get())) {
                context.attrs().attr(AttributeKey.stringKey("3")).set("3");
                return next.doNext(mapping, context);
            }
            throw new IllegalStateException();
        });

        final LinkedRouteFilterChain chain = LinkedRouteFilterChain.immutable(filters.toArray(new RouteFilter[0]),
                (context) -> CompletableFuture.completedFuture(null));

        final RoutedRequest request = mock(RoutedRequest.class);
        final HttpResponse response = mock(HttpResponse.class);
        final RouteContext context = new RouteContextImpl(new AttributeMap(), request, response);
        chain.doNext(mock(HandlerMapping.class), context);
        assertEquals("1", context.attrs().attr(AttributeKey.stringKey("1")).get());
        assertEquals("2", context.attrs().attr(AttributeKey.stringKey("2")).get());
        assertEquals("3", context.attrs().attr(AttributeKey.stringKey("3")).get());
    }

}
