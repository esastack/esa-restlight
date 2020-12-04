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
package esa.restlight.core.handler.impl;

import esa.commons.Checks;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.handler.Handler;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.method.MethodParam;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.core.resolver.HandlerResolverFactory;
import esa.restlight.core.resolver.ReturnValueResolver;
import esa.restlight.core.util.RouteUtils;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * An adapter of {@link Handler} which maintains the handler information such as parameters, resolvers and so on...
 */
public class HandlerAdapter<H extends Handler> implements Handler {

    protected final H handler;

    private final ResolvableParam[] params;
    private final ReturnValueResolver returnValueResolver;
    private final boolean isConcurrent;

    public HandlerAdapter(H handler,
                          HandlerResolverFactory resolverFactory) {
        this.handler = handler;
        this.isConcurrent = RouteUtils.isConcurrent(handler.handler());
        this.params = getArgumentResolvers(handler.handler(), resolverFactory);
        this.returnValueResolver = resolverFactory.getReturnValueResolver(handler.handler());
    }

    private static ResolvableParam[] getArgumentResolvers(InvocableMethod handler, HandlerResolverFactory factory) {
        // bind MethodParam and corresponding ArgumentResolver as ResolvableParam
        ResolvableParam[] params = new ResolvableParam[handler.parameters().length];
        for (int i = 0; i < params.length; i++) {
            MethodParam parameter = handler.parameters()[i];
            ArgumentResolver resolver = factory.getArgumentResolver(parameter);
            params[i] = new ResolvableParam(parameter, resolver);
        }
        return params;
    }

    @Override
    public InvocableMethod handler() {
        return handler.handler();
    }

    @Override
    public HttpResponseStatus customResponse() {
        return handler.customResponse();
    }

    @Override
    public Object invoke(AsyncRequest request, AsyncResponse response, Object[] args) throws Throwable {
        return handler.invoke(request, response, args);
    }

    ResolvableParam[] params() {
        return params;
    }

    ReturnValueResolver returnValueResolver() {
        return returnValueResolver;
    }

    boolean isConcurrent() {
        return isConcurrent;
    }

    @Override
    public String toString() {
        return handler.toString();
    }

    static class ResolvableParam {
        final MethodParam param;
        final ArgumentResolver resolver;

        ResolvableParam(MethodParam param,
                        ArgumentResolver resolver) {
            Checks.checkNotNull(param, "param");
            this.param = param;
            this.resolver = resolver;
        }
    }
}
