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
package io.esastack.restlight.jaxrs.adapter;

import esa.commons.collection.AttributeMap;
import esa.commons.collection.Attributes;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.jaxrs.impl.JaxrsContextUtils;
import io.esastack.restlight.core.filter.RouteContext;
import io.esastack.restlight.core.filter.RouteContextImpl;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.filter.RoutedRequestImpl;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.core.mock.MockHttpResponse;
import io.esastack.restlight.core.util.Futures;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PostMatchRequestFiltersAdapterTest {

    @Test
    void testAll() {
        assertThrows(NullPointerException.class, () -> new PostMatchRequestFiltersAdapter(null));

        final Attributes attrs = new AttributeMap();
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final ContainerRequestFilter[] filters = new ContainerRequestFilter[1];
        final Response rsp = mock(Response.class);
        filters[0] = requestContext -> requestContext.abortWith(rsp);
        final PostMatchRequestFiltersAdapter adapter = new PostMatchRequestFiltersAdapter(filters);

        final AtomicInteger count = new AtomicInteger();
        final RouteContext rCtx = new RouteContextImpl(attrs, new RoutedRequestImpl(request), response);
        adapter.routed(mock(HandlerMapping.class),
                rCtx,
                (mapping, context) -> {
                    count.incrementAndGet();
                    return Futures.completedFuture();
                });
        assertEquals(0, count.intValue());
        assertTrue(JaxrsContextUtils.getRequestContext(rCtx).isAborted());

        filters[0] = requestContext -> { };
        final RouteContext rCtx1 = new RouteContextImpl(new AttributeMap(), new RoutedRequestImpl(request), response);
        adapter.routed(mock(HandlerMapping.class),
                rCtx1,
                (mapping, context) -> {
                    count.incrementAndGet();
                    return Futures.completedFuture();
                });
        assertEquals(1, count.intValue());
        assertFalse(JaxrsContextUtils.getRequestContext(rCtx1).isAborted());

        assertEquals(Ordered.LOWEST_PRECEDENCE, adapter.getOrder());
    }

}

