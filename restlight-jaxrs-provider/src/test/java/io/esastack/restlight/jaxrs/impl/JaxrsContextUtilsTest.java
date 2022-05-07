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
package io.esastack.restlight.jaxrs.impl;

import esa.commons.collection.AttributeMap;
import esa.commons.collection.Attributes;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.restlight.jaxrs.impl.container.AbstractContainerRequestContext;
import io.esastack.restlight.jaxrs.impl.container.PostMatchContainerRequestContext;
import io.esastack.restlight.jaxrs.impl.container.PreMatchContainerRequestContext;
import io.esastack.restlight.core.filter.FilterContext;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.filter.FilteringRequest;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.core.mock.MockHttpResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JaxrsContextUtilsTest {

    @Test
    void testGetRequest() {
        final Attributes attrs = new AttributeMap();
        final RequestContext context = mock(RequestContext.class);
        when(context.attrs()).thenReturn(attrs);
        when(context.request()).thenReturn(mock(HttpRequest.class));
        when(context.response()).thenReturn(mock(HttpResponse.class));

        Request request = JaxrsContextUtils.getRequest(context);
        assertNotNull(request);
        assertSame(request, JaxrsContextUtils.getRequest(context));
    }

    @Test
    void testGetUriInfo() {
        final Attributes attrs = new AttributeMap();
        final RequestContext context = mock(RequestContext.class);
        when(context.attrs()).thenReturn(attrs);
        when(context.request()).thenReturn(MockHttpRequest.aMockRequest().build());
        when(context.response()).thenReturn(MockHttpResponse.aMockResponse().build());

        UriInfo uriInfo = JaxrsContextUtils.getUriInfo(context);
        assertNotNull(uriInfo);
        assertSame(uriInfo, JaxrsContextUtils.getUriInfo(context));
    }

    @Test
    void testGetHeaders() {
        final Attributes attrs = new AttributeMap();
        final HttpRequest request = mock(HttpRequest.class);
        final RequestContext context = mock(RequestContext.class);
        when(context.attrs()).thenReturn(attrs);
        when(context.request()).thenReturn(request);
        when(context.response()).thenReturn(mock(HttpResponse.class));
        when(request.headers()).thenReturn(new Http1HeadersImpl());

        HttpHeaders headers = JaxrsContextUtils.getHeaders(context);
        assertNotNull(headers);
        assertSame(headers, JaxrsContextUtils.getHeaders(context));
    }

    @Test
    void testGetRequestContext() {
        final Attributes attrs1 = new AttributeMap();
        final HttpRequest request1 = mock(HttpRequest.class);
        final RequestContext context1 = mock(RequestContext.class);
        when(context1.attrs()).thenReturn(attrs1);
        when(request1.headers()).thenReturn(new Http1HeadersImpl());
        when(context1.request()).thenReturn(request1);
        when(context1.response()).thenReturn(mock(HttpResponse.class));
        when(request1.scheme()).thenReturn("http");

        AbstractContainerRequestContext reqCtx1 = JaxrsContextUtils.getRequestContext(context1);
        assertNotNull(reqCtx1);
        assertTrue(reqCtx1 instanceof PostMatchContainerRequestContext);
        assertSame(reqCtx1, JaxrsContextUtils.getRequestContext(context1));


        final Attributes attrs2 = new AttributeMap();
        final FilteringRequest request2 = mock(FilteringRequest.class);
        final RequestContext context2 = mock(FilterContext.class);
        when(context2.attrs()).thenReturn(attrs2);
        when(request2.headers()).thenReturn(new Http1HeadersImpl());
        when(context2.request()).thenReturn(request2);
        when(context2.response()).thenReturn(mock(HttpResponse.class));

        when(request2.scheme()).thenReturn("https");
        AbstractContainerRequestContext reqCtx2 = JaxrsContextUtils.getRequestContext(context2);
        assertNotNull(reqCtx2);
        assertTrue(reqCtx2 instanceof PreMatchContainerRequestContext);
        assertSame(reqCtx2, JaxrsContextUtils.getRequestContext(context2));
    }

    @Test
    void testOperateSecurityContext() {
        final Attributes attrs = new AttributeMap();
        final RequestContext context = mock(RequestContext.class);
        when(context.attrs()).thenReturn(attrs);
        final SecurityContext sCtx = mock(SecurityContext.class);
        JaxrsContextUtils.setSecurityContext(context, sCtx);
        assertSame(sCtx, JaxrsContextUtils.getSecurityContext(context));
    }

    @Test
    void testOperateAsyncResponse() {
        final Attributes attrs = new AttributeMap();
        final RequestContext context = mock(RequestContext.class);
        when(context.attrs()).thenReturn(attrs);
        final CompletableFuture<Object> asyncResponse = new CompletableFuture<>();
        JaxrsContextUtils.setAsyncResponse(context, asyncResponse);
        assertSame(asyncResponse, JaxrsContextUtils.getAsyncResponse(context));
    }

    @Test
    void testExtractURI() {
        final HttpRequest request = mock(HttpRequest.class);
        final RequestContext context = mock(RequestContext.class);
        when(context.request()).thenReturn(request);
        when(request.scheme()).thenReturn("https");
        when(request.localAddr()).thenReturn("127.0.0.1");
        when(request.localPort()).thenReturn(8080);

        URI uri = JaxrsContextUtils.extractURI(context);
        assertEquals("https://127.0.0.1:8080", uri.toString());
    }

}

