/*
 * Copyright 2021 OPPO ESA Stack Project
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
import io.esastack.restlight.core.filter.FilterContext;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.filter.FilterContextImpl;
import io.esastack.restlight.core.filter.FilteringRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.filter.Filter;
import io.esastack.restlight.core.server.processor.FilteredHandler;
import io.esastack.restlight.core.server.processor.RestlightHandler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FilteredHandlerTest {

    @Test
    void testFilter() {
        final RestlightHandler handler = mock(RestlightHandler.class);
        final List<Filter> filters = new ArrayList<>();
        filters.add((context, chain) -> {
            context.attrs().attr(AttributeKey.stringKey("1")).set("1");
            return chain.doFilter(context);
        });

        filters.add((context, chain) -> {
            if ("1".equals(context.attrs().attr(AttributeKey.stringKey("1")).get())) {
                context.attrs().attr(AttributeKey.stringKey("2")).set("2");
                return chain.doFilter(context);
            }
            throw new IllegalStateException();
        });

        filters.add((context, chain) -> {
            if ("1".equals(context.attrs().attr(AttributeKey.stringKey("1")).get()) &&
                    "2".equals(context.attrs().attr(AttributeKey.stringKey("2")).get())) {
                context.attrs().attr(AttributeKey.stringKey("3")).set("3");
                return chain.doFilter(context);
            }
            throw new IllegalStateException();
        });

        when(handler.process(any(RequestContext.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        final FilteredHandler filteredHandler = new FilteredHandler(handler, filters);
        final FilteringRequest request = mock(FilteringRequest.class);
        final HttpResponse response = mock(HttpResponse.class);
        final FilterContext context = new FilterContextImpl(new AttributeMap(), request, response);
        filteredHandler.process(context);
        assertEquals("1", context.attrs().attr(AttributeKey.stringKey("1")).get());
        assertEquals("2", context.attrs().attr(AttributeKey.stringKey("2")).get());
        assertEquals("3", context.attrs().attr(AttributeKey.stringKey("3")).get());
    }
}
