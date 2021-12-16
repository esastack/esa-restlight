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
import esa.commons.reflect.AnnotationUtils;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpserver.core.RequestContext;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.resolver.ResponseEntityResolver;
import io.esastack.restlight.core.resolver.ResponseEntityResolverFactory;
import io.esastack.restlight.core.resolver.rspentity.AbstractResponseEntityResolver;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.springmvc.annotation.shaded.ResponseStatus0;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link ParamResolverFactory} for resolving argument that annotated by the ResponseStatus which
 * has a ResponseStatus#reason().
 */
public class ResponseStatusEntityResolverFactory implements ResponseEntityResolverFactory {

    @Override
    public ResponseEntityResolver createResolver(List<? extends HttpResponseSerializer> serializers) {
        return new Resolver();
    }

    private static ResponseStatus0 getResponseStatus(HandlerMethod handlerMethod) {
        ResponseStatus0 anno =
                ResponseStatus0.fromShade(handlerMethod.getMethodAnnotation(ResponseStatus0.shadedClass()));
        if (anno == null) {
            anno = ResponseStatus0.fromShade(AnnotationUtils.findAnnotation(handlerMethod.beanType(),
                    ResponseStatus0.shadedClass()));
        }
        return anno;
    }

    private static class Resolver extends AbstractResponseEntityResolver {

        private Resolver() {
            super(false);
        }

        @Override
        protected boolean supports(ResponseEntity entity) {
            HandlerMethod handlerMethod = entity.handler().orElse(null);
            if (handlerMethod == null) {
                return false;
            }
            ResponseStatus0 anno = getResponseStatus(handlerMethod);
            return anno != null && !StringUtils.isEmpty(anno.reason());
        }

        @Override
        protected byte[] serialize(ResponseEntity entity,
                                   List<MediaType> mediaTypes,
                                   RequestContext context) throws Exception {
            entity.response().headers().set(HttpHeaderNames.CONTENT_TYPE, MediaType.TEXT_PLAIN.value());
            return getResponseStatus(Objects.requireNonNull(entity.handler().orElse(null))).reason()
                    .getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }

}
