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

import esa.commons.collection.AttributeKey;
import io.esastack.restlight.jaxrs.impl.container.AbstractContainerRequestContext;
import io.esastack.restlight.jaxrs.impl.container.PostMatchContainerRequestContext;
import io.esastack.restlight.jaxrs.impl.container.PreMatchContainerRequestContext;
import io.esastack.restlight.jaxrs.impl.core.HttpRequestHeaders;
import io.esastack.restlight.jaxrs.impl.core.RequestImpl;
import io.esastack.restlight.jaxrs.impl.core.UriInfoImpl;
import io.esastack.restlight.server.context.FilterContext;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.HttpRequest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.SecurityContext;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public final class JaxrsContextUtils {

    private static final AttributeKey<Request> REQUEST_KEY = AttributeKey.valueOf("$jakarta.request");
    private static final AttributeKey<UriInfoImpl> URI_KEY = AttributeKey.valueOf("$jakarta.uriInfo");
    private static final AttributeKey<HttpHeaders> REQUEST_HEADERS_KEY = AttributeKey
            .valueOf("$jakarta.request.headers");
    private static final AttributeKey<SecurityContext> SECURITY_CONTEXT_KEY = AttributeKey
            .valueOf("$jakarta.security.context");
    private static final AttributeKey<AbstractContainerRequestContext> REQUEST_CONTEXT_KEY = AttributeKey
            .valueOf("$jakarta.request.context");
    private static final AttributeKey<CompletableFuture<Object>> ASYNC_RESPONSE_KEY = AttributeKey
            .valueOf("$jakarta.async.response");

    public static Request getRequest(RequestContext context) {
        Request request = context.attrs().attr(REQUEST_KEY).get();
        if (request == null) {
            request = new RequestImpl(context);
            context.attrs().attr(REQUEST_KEY).set(request);
        }
        return request;
    }

    public static UriInfoImpl getUriInfo(RequestContext context) {
        UriInfoImpl uriInfo = context.attrs().attr(URI_KEY).get();
        if (uriInfo == null) {
            uriInfo = new UriInfoImpl(extractURI(context), context);
            context.attrs().attr(URI_KEY).set(uriInfo);
        }
        return uriInfo;
    }

    public static HttpHeaders getHeaders(RequestContext context) {
        HttpHeaders headers = context.attrs().attr(REQUEST_HEADERS_KEY).get();
        if (headers == null) {
            headers = new HttpRequestHeaders(context.request());
            context.attrs().attr(REQUEST_HEADERS_KEY).set(headers);
        }
        return headers;
    }

    public static AbstractContainerRequestContext getRequestContext(RequestContext context) {
        AbstractContainerRequestContext ctx = context.attrs().attr(REQUEST_CONTEXT_KEY).get();
        if (ctx == null) {
            if (context instanceof FilterContext) {
                ctx = new PreMatchContainerRequestContext((FilterContext) context);
            } else {
                ctx = new PostMatchContainerRequestContext(context);
            }
            context.attrs().attr(REQUEST_CONTEXT_KEY).set(ctx);
        }
        return ctx;
    }

    public static SecurityContext getSecurityContext(RequestContext context) {
        return context.attrs().attr(SECURITY_CONTEXT_KEY).get();
    }

    public static void setSecurityContext(RequestContext context, SecurityContext sCtx) {
        context.attrs().attr(SECURITY_CONTEXT_KEY).set(sCtx);
    }

    public static void setAsyncResponse(RequestContext context, CompletableFuture<Object> response) {
        context.attrs().attr(ASYNC_RESPONSE_KEY).set(response);
    }

    public static CompletableFuture<Object> getAsyncResponse(RequestContext context) {
        return context.attrs().attr(ASYNC_RESPONSE_KEY).get();
    }

    public static URI extractURI(RequestContext context) {
        HttpRequest request = context.request();
        StringBuilder sb = new StringBuilder();
        sb.append(request.scheme().toLowerCase())
                .append("://")
                .append(request.localAddr())
                .append(":")
                .append(request.localPort());
        return URI.create(sb.toString());
    }

    private JaxrsContextUtils() {
    }
}


