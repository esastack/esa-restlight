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
package io.esastack.restlight.springmvc.resolver.rspentity;

import esa.commons.StringUtils;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.resolver.param.HttpParamResolverFactory;
import io.esastack.restlight.core.context.ResponseEntity;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolver;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverFactory;
import io.esastack.restlight.core.resolver.entity.response.AbstractResponseEntityResolver;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.springmvc.annotation.shaded.ResponseStatus0;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Implementation of {@link HttpParamResolverFactory} for resolving argument that annotated by the ResponseStatus which
 * has a ResponseStatus#reason().
 */
public class ResponseStatusEntityResolverFactory implements ResponseEntityResolverFactory {

    @Override
    public ResponseEntityResolver createResolver(HandlerMethod method,
                                                 List<? extends HttpResponseSerializer> serializers) {
        ResponseStatus0 anno = getResponseStatus(method);
        return new Resolver(anno);
    }

    @Override
    public boolean supports(HandlerMethod method) {
        ResponseStatus0 anno = getResponseStatus(method);
        return anno != null && !StringUtils.isEmpty(anno.reason());
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private static ResponseStatus0 getResponseStatus(HandlerMethod handlerMethod) {
        ResponseStatus0 anno =
                ResponseStatus0.fromShade(handlerMethod
                        .getMethodAnnotation(ResponseStatus0.shadedClass(), true));
        if (anno == null) {
            anno = ResponseStatus0.fromShade(handlerMethod
                    .getClassAnnotation(ResponseStatus0.shadedClass(), true));
        }
        return anno;
    }

    private static class Resolver extends AbstractResponseEntityResolver {

        private final ResponseStatus0 anno;

        private Resolver(ResponseStatus0 anno) {
            super(false);
            this.anno = anno;
        }

        @Override
        protected byte[] serialize(ResponseEntity entity,
                                   List<MediaType> mediaTypes,
                                   RequestContext context) throws Exception {
            entity.response().headers().set(HttpHeaderNames.CONTENT_TYPE, MediaType.TEXT_PLAIN.value());
            return anno.reason().getBytes(StandardCharsets.UTF_8);
        }
    }

}
