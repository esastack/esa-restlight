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
import esa.commons.reflect.AnnotationUtils;
import esa.commons.reflect.ReflectionUtils;
import io.esastack.restlight.core.method.MethodParam;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.jaxrs.impl.container.AbstractContainerRequestContext;
import io.esastack.restlight.jaxrs.impl.container.PostMatchContainerRequestContext;
import io.esastack.restlight.jaxrs.impl.container.PreMatchContainerRequestContext;
import io.esastack.restlight.jaxrs.impl.core.HttpRequestHeaders;
import io.esastack.restlight.jaxrs.impl.core.RequestImpl;
import io.esastack.restlight.jaxrs.impl.core.ResponseImpl;
import io.esastack.restlight.jaxrs.impl.core.UriInfoImpl;
import io.esastack.restlight.server.context.FilterContext;
import io.esastack.restlight.server.context.RequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public final class JaxrsContextUtils {

    private static final AttributeKey<Request> REQUEST_KEY = AttributeKey.valueOf("$jakarta.request");
    private static final AttributeKey<Response> RESPONSE_KEY = AttributeKey.valueOf("$jakarta.response");
    private static final AttributeKey<UriInfoImpl> URI_KEY = AttributeKey.valueOf("$jakarta.uriInfo");
    private static final AttributeKey<HttpHeaders> REQUEST_HEADERS_KEY = AttributeKey
            .valueOf("$jakarta.request.headers");
    private static final AttributeKey<SecurityContext> SECURITY_CONTEXT_KEY = AttributeKey
            .valueOf("$jakarta.security.context");
    private static final AttributeKey<AbstractContainerRequestContext> REQUEST_CONTEXT_KEY = AttributeKey
            .valueOf("$jakarta.request.context");
    private static final AttributeKey<CompletableFuture<Object>> ASYNC_RESPONSE_KEY = AttributeKey
            .valueOf("$jakarta.async.response");

    public static boolean hasContextAnnotation(Param param) {
        if (param == null) {
            return false;
        }
        return param.hasAnnotation(Context.class) || isSetterParam(param);
    }

    public static Request getRequest(RequestContext context) {
        Request request = context.attrs().attr(REQUEST_KEY).get();
        if (request == null) {
            request = new RequestImpl(context.request(), context.response());
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

    public static ResponseImpl getResponse(RequestContext context) {
        Response response = context.attrs().attr(RESPONSE_KEY).get();
        ResponseImpl rsp;
        if (response == null) {
            rsp = (ResponseImpl) RuntimeDelegate.getInstance().createResponseBuilder().build();
        } else {
            rsp = (ResponseImpl) Response.fromResponse(response).build();
        }
        return rsp;
    }

    public static void setResponse(RequestContext context, Response response) {
        context.attrs().attr(RESPONSE_KEY).set(response);
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
        String sb = context.request().scheme() +
                "://" +
                context.request().localAddr() +
                ":" +
                context.request().localPort();
        return URI.create(sb);
    }

    private static boolean isSetterParam(Param param) {
        if (!param.isMethodParam()) {
            return false;
        }
        MethodParam mParam = param.methodParam();
        if (!ReflectionUtils.isSetter(mParam.method())) {
            return false;
        }
        if (mParam.method().getParameterCount() != 1) {
            return false;
        }
        return AnnotationUtils.hasAnnotation(mParam.method(), Context.class);
    }

    private JaxrsContextUtils() {
    }
}


