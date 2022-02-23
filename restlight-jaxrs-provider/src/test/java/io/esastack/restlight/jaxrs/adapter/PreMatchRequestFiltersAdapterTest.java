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
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.jaxrs.impl.JaxrsContextUtils;
import io.esastack.restlight.server.context.FilterContext;
import io.esastack.restlight.server.context.impl.FilterContextImpl;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.core.impl.FilteringRequestImpl;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.mock.MockHttpResponse;
import io.esastack.restlight.server.util.Futures;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PreMatchRequestFiltersAdapterTest {

    @Test
    void testAll() {
        assertThrows(NullPointerException.class, () -> new PreMatchRequestFiltersAdapter(null));

        final Attributes attrs = new AttributeMap();
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final ContainerRequestFilter[] filters = new ContainerRequestFilter[1];
        final Response rsp = mock(Response.class);
        filters[0] = requestContext -> requestContext.abortWith(rsp);
        final PreMatchRequestFiltersAdapter adapter = new PreMatchRequestFiltersAdapter(filters);

        final AtomicInteger count = new AtomicInteger();
        final FilterContext rCtx = new FilterContextImpl(attrs, new FilteringRequestImpl(request), response);
        adapter.doFilter(rCtx, context -> {
            count.incrementAndGet();
            return Futures.completedFuture();
        });

        assertEquals(0, count.intValue());
        assertTrue(JaxrsContextUtils.getRequestContext(rCtx).isAborted());

        filters[0] = requestContext -> { };
        final FilterContext rCtx1 = new FilterContextImpl(new AttributeMap(),
                new FilteringRequestImpl(request), response);
        adapter.doFilter(
                rCtx1,
                context -> {
                    count.incrementAndGet();
                    return Futures.completedFuture();
                });

        assertEquals(1, count.intValue());
        assertFalse(JaxrsContextUtils.getRequestContext(rCtx1).isAborted());

        assertEquals(Ordered.LOWEST_PRECEDENCE, adapter.getOrder());
    }

}

