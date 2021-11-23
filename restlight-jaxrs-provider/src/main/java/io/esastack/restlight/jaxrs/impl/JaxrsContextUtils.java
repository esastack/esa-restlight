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

import io.esastack.httpserver.core.RequestContext;
import io.esastack.restlight.jaxrs.impl.container.AbstractContainerRequestContext;
import io.esastack.restlight.jaxrs.impl.container.PostMatchContainerRequestContext;
import io.esastack.restlight.jaxrs.impl.container.PreMatchContainerRequestContext;
import io.esastack.restlight.jaxrs.impl.core.HttpRequestHeaders;
import io.esastack.restlight.jaxrs.impl.core.RequestImpl;
import io.esastack.restlight.jaxrs.impl.core.ResponseImpl;
import io.esastack.restlight.jaxrs.impl.core.UriInfoImpl;
import io.esastack.restlight.server.context.FilterContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public final class JaxrsContextUtils {

    private static final String REQUEST_KEY = "$jakarta.request";
    private static final String RESPONSE_KEY = "$jakarta.response";
    private static final String URI_KEY = "$jakarta.uriInfo";
    private static final String REQUEST_HEADERS_KEY = "$jakarta.request.headers";
    private static final String SECURITY_CONTEXT_KEY = "$jakarta.security.context";
    private static final String REQUEST_CONTEXT_KEY = "$jakarta.request.context";
    private static final String ASYNC_RESPONSE_KEY = "$jakarta.async.response";

    public static Request getRequest(RequestContext context) {
        Request request = context.getUncheckedAttribute(REQUEST_KEY);
        if (request == null) {
            request = new RequestImpl(context.request(), context.response());
            context.setAttribute(REQUEST_KEY, request);
        }
        return request;
    }

    public static UriInfoImpl getUriInfo(RequestContext context) {
        UriInfoImpl uriInfo = context.getUncheckedAttribute(URI_KEY);
        if (uriInfo == null) {
            uriInfo = new UriInfoImpl(extractURI(context), context);
            context.setAttribute(URI_KEY, uriInfo);
        }
        return uriInfo;
    }

    public static HttpHeaders getHeaders(RequestContext context) {
        HttpHeaders headers = context.getUncheckedAttribute(REQUEST_HEADERS_KEY);
        if (headers == null) {
            headers = new HttpRequestHeaders(context.request());
            context.setAttribute(REQUEST_HEADERS_KEY, headers);
        }
        return headers;
    }

    public static AbstractContainerRequestContext getRequestContext(RequestContext context) {
        AbstractContainerRequestContext ctx = context.getUncheckedAttribute(REQUEST_CONTEXT_KEY);
        if (ctx == null) {
            if (context instanceof FilterContext) {
                ctx = new PreMatchContainerRequestContext((FilterContext) context);
            } else {
                ctx = new PostMatchContainerRequestContext(context);
            }
            context.setAttribute(REQUEST_CONTEXT_KEY, ctx);
        }
        return ctx;
    }

    public static ResponseImpl getResponse(RequestContext context) {
        Response response = context.getUncheckedAttribute(RESPONSE_KEY);
        ResponseImpl rsp;
        if (response == null) {
            rsp = (ResponseImpl) RuntimeDelegate.getInstance().createResponseBuilder().build();
        } else {
            rsp = (ResponseImpl) Response.fromResponse(response).build();
        }
        return rsp;
    }

    public static void setResponse(RequestContext context, Response response) {
        context.setAttribute(RESPONSE_KEY, response);
    }

    public static SecurityContext getSecurityContext(RequestContext context) {
        return context.getUncheckedAttribute(SECURITY_CONTEXT_KEY);
    }

    public static void setSecurityContext(RequestContext context, SecurityContext sCtx) {
        context.setAttribute(SECURITY_CONTEXT_KEY, sCtx);
    }

    public static void setAsyncResponse(RequestContext context, CompletableFuture<Object> response) {
        context.setAttribute(ASYNC_RESPONSE_KEY, response);
    }

    public static CompletableFuture<Object> getAsyncResponse(RequestContext context) {
        return context.getUncheckedAttribute(ASYNC_RESPONSE_KEY);
    }

    public static URI extractURI(RequestContext context) {
        String sb = context.request().scheme() +
                "://" +
                context.request().localAddr() +
                ":" +
                context.request().localPort();
        return URI.create(sb);
    }

    private JaxrsContextUtils() {
    }
}


