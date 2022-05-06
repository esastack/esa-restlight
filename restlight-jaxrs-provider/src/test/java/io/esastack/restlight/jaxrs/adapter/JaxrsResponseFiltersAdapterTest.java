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

import esa.commons.collection.AttributeKey;
import esa.commons.collection.AttributeMap;
import esa.commons.collection.Attributes;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.jaxrs.resolver.ResponseEntityStreamChannelImpl;
import io.esastack.restlight.core.resolver.ResponseContent;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.RouteContext;
import io.esastack.restlight.server.context.impl.FilterContextImpl;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.esastack.restlight.server.context.impl.RouteContextImpl;
import io.esastack.restlight.server.core.FilteringRequest;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.core.RoutedRequest;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.mock.MockHttpResponse;
import io.esastack.restlight.server.util.Futures;
import io.netty.buffer.ByteBufAllocator;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JaxrsResponseFiltersAdapterTest {

    @Test
    void testBasic() {
        assertThrows(NullPointerException.class, () -> new JaxrsResponseFiltersAdapter(null));

        final JaxrsResponseFiltersAdapter adapter = new JaxrsResponseFiltersAdapter(new ContainerResponseFilter[0]);
        assertEquals(Ordered.LOWEST_PRECEDENCE, adapter.getOrder());

        final CompletableFuture<Void> future = adapter.doFilter(new FilterContextImpl(new AttributeMap(),
                mock(FilteringRequest.class),
                mock(HttpResponse.class)), context -> Futures.completedFuture()).toCompletableFuture();
        assertTrue(future.isDone());
    }

    @Test
    void testGetBoundFilters() {
        final ContainerResponseFilter[] filters = new ContainerResponseFilter[0];
        final JaxrsResponseFiltersAdapter adapter = new JaxrsResponseFiltersAdapter(filters);
        final Attributes attrs = new AttributeMap();
        final RequestContext context = new RequestContextImpl(attrs, mock(HttpRequest.class), mock(HttpResponse.class));
        assertSame(filters, adapter.getBoundFilters(context));

        final ContainerResponseFilter[] filters1  = new ContainerResponseFilter[1];
        attrs.attr(AttributeKey.valueOf("$bound_filters")).set(filters1);
        assertFalse(attrs.isEmpty());
        assertSame(filters1, adapter.getBoundFilters(context));
        assertTrue(attrs.isEmpty());
    }

    @Test
    void testApplyResponseFilters() {
        final AtomicInteger count = new AtomicInteger();
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final Attributes attrs = new AttributeMap();
        final RequestContext context = new RequestContextImpl(attrs, request, response);

        MultivaluedMap<String, Object> previous = new MultivaluedHashMap<>();
        final ContainerResponseFilter[] filters = new ContainerResponseFilter[1];
        filters[0] = (reqCtx, rspCtx) -> {
            previous.putAll(rspCtx.getHeaders());
            rspCtx.getHeaders().add("name", "value");
            rspCtx.setEntity("DEF");
            count.incrementAndGet();
        };
        context.response().status(400);
        JaxrsResponseFiltersAdapter.applyResponseFilters(context, filters);
        assertEquals(0, count.intValue());

        context.response().status(200);
        JaxrsResponseFiltersAdapter.applyResponseFilters(context, null);
        assertEquals(0, count.intValue());

        JaxrsResponseFiltersAdapter.applyResponseFilters(context, new ContainerResponseFilter[0]);
        assertEquals(0, count.intValue());

        final ResponseContent content = mock(ResponseContent.class);
        context.attrs().attr(RequestContextImpl.RESPONSE_CONTENT).set(content);
        when(content.alloc()).thenReturn(ByteBufAllocator.DEFAULT);
        ResponseEntityStreamChannelImpl.get(context);
        response.headers().add("name0", "value0");
        JaxrsResponseFiltersAdapter.applyResponseFilters(context, filters);
        assertEquals(1, count.intValue());
        assertEquals(1, previous.size());
        assertEquals("value0", previous.getFirst("name0"));
        assertEquals(2, response.headers().size());
        assertEquals("value0", response.headers().get("name0"));
        assertEquals("value", response.headers().get("name"));
        assertEquals("DEF", response.entity());

        count.set(0);
        previous.clear();
        response.headers().clear();

        response.headers().add("name0", "value0");
        final ContainerResponseFilter[] filters1 = new ContainerResponseFilter[2];
        filters1[0] = filters[0];
        filters1[1] = (reqCtx, rspCtx) -> {
            rspCtx.getHeaders().add("name1", "value1");
            throw new IllegalStateException();
        };
        JaxrsResponseFiltersAdapter.applyResponseFilters(context, filters1);
        assertEquals(1, count.intValue());
        assertEquals(1, previous.size());
        assertEquals("value0", previous.getFirst("name0"));
        assertEquals(3, response.headers().size());
        assertEquals("value0", response.headers().get("name0"));
        assertEquals("value", response.headers().get("name"));
        assertEquals("value1", response.headers().get("name1"));
        assertEquals("DEF", response.entity());
    }

    @Test
    void testIsSuccess() {
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final Attributes attrs = new AttributeMap();
        final RequestContext context = new RequestContextImpl(attrs, request, response);
        assertTrue(JaxrsResponseFiltersAdapter.isSuccess(context));
        response.status(400);
        assertFalse(JaxrsResponseFiltersAdapter.isSuccess(context));
        response.status(500);
        assertFalse(JaxrsResponseFiltersAdapter.isSuccess(context));
    }

    @Test
    void testGetResponse() {
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final Attributes attrs = new AttributeMap();
        final RequestContext context = new RequestContextImpl(attrs, request, response);

        final Response rsp = Response.ok().build();
        context.response().entity(rsp);
        assertSame(rsp, JaxrsResponseFiltersAdapter.getResponse(context));

        context.response().entity(null);
        assertNotNull(JaxrsResponseFiltersAdapter.getResponse(context));
    }

    @Test
    void testBinder() {
        final ContainerResponseFilter[] filters = new ContainerResponseFilter[0];
        final JaxrsResponseFiltersAdapter.ContainerResponseFilterBinder binder =
                new JaxrsResponseFiltersAdapter.ContainerResponseFilterBinder(filters);
        assertEquals(Ordered.HIGHEST_PRECEDENCE, binder.getOrder());

        final Attributes attrs = new AttributeMap();
        final RouteContext context = new RouteContextImpl(attrs, mock(RoutedRequest.class), mock(HttpResponse.class));
        binder.routed(mock(HandlerMapping.class), context, (mapping, ctx) -> null);
        assertEquals(1, attrs.size());
        assertSame(filters, attrs.attr(AttributeKey.valueOf("$bound_filters")).get());
    }

}

