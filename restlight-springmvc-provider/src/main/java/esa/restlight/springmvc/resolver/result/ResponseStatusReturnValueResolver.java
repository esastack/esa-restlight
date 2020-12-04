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
package esa.restlight.springmvc.resolver.result;

import esa.commons.StringUtils;
import esa.commons.reflect.AnnotationUtils;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.resolver.ReturnValueResolver;
import esa.restlight.core.resolver.ReturnValueResolverFactory;
import esa.restlight.core.serialize.HttpResponseSerializer;
import esa.restlight.core.util.MediaType;
import esa.restlight.springmvc.annotation.shaded.ResponseStatus0;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Implementation of {@link ArgumentResolverFactory} for resolving argument that annotated by the ResponseStatus which
 * has a ResponseStatus#reason().
 */
public class ResponseStatusReturnValueResolver implements ReturnValueResolverFactory {

    @Override
    public boolean supports(InvocableMethod invocableMethod) {
        ResponseStatus0 anno = getResponseStatus(invocableMethod);
        return anno != null && !StringUtils.isEmpty(anno.reason());
    }

    @Override
    public ReturnValueResolver createResolver(InvocableMethod method,
                                              List<? extends HttpResponseSerializer> serializers) {
        return new Resolver(getResponseStatus(method).reason());
    }

    private static ResponseStatus0 getResponseStatus(InvocableMethod invocableMethod) {
        ResponseStatus0 anno =
                ResponseStatus0.fromShade(invocableMethod.getMethodAnnotation(ResponseStatus0.shadedClass()));
        if (anno == null) {
            anno = ResponseStatus0.fromShade(AnnotationUtils.findAnnotation(invocableMethod.beanType(),
                    ResponseStatus0.shadedClass()));
        }
        return anno;
    }


    @Override
    public int getOrder() {
        return 0;
    }

    private static class Resolver implements ReturnValueResolver {

        private final byte[] reason;

        Resolver(String reason) {
            this.reason = reason.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public byte[] resolve(Object returnValue, AsyncRequest request, AsyncResponse response) {
            response.setHeader(HttpHeaderNames.CONTENT_TYPE, MediaType.TEXT_PLAIN.value());
            return reason;
        }
    }

}
