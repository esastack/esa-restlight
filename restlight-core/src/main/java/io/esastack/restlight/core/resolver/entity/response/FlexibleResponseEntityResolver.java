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
package io.esastack.restlight.core.resolver.entity.response;

import esa.commons.Result;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.resolver.param.HttpParamResolverFactory;
import io.esastack.restlight.core.context.ResponseEntity;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.core.serialize.Serializers;
import io.esastack.restlight.core.util.FutureUtils;
import io.esastack.restlight.core.exception.WebServerException;
import io.esastack.restlight.core.context.RequestContext;

import java.util.List;

/**
 * Implementation of {@link HttpParamResolverFactory} for resolving argument that annotated by the ResponseBody.
 */
public class FlexibleResponseEntityResolver extends AbstractResponseEntityResolver {

    private final List<? extends HttpResponseSerializer> serializers;

    public FlexibleResponseEntityResolver(List<? extends HttpResponseSerializer> serializers) {
        super(true);
        this.serializers = serializers;
    }

    @Override
    protected byte[] serialize(ResponseEntity entity, List<MediaType> mediaTypes, RequestContext context)
            throws Exception {
        if (serializers.isEmpty()) {
            throw WebServerException.notAcceptable("Could not find any compatible serializer to handle " +
                    "the return value(type=" + entity.type().getName() +
                    "), acceptMediaTypes: " + mediaTypes);
        }

        Result<byte[], Void> handled;
        if (mediaTypes.isEmpty()) {
            for (HttpResponseSerializer ser : serializers) {
                handled = Serializers.serializeBySerializer(ser, entity);
                if (handled.isOk()) {
                    return handled.get();
                }
            }
        } else {
            for (MediaType mediaType : mediaTypes) {
                for (HttpResponseSerializer ser : serializers) {
                    entity.mediaType(mediaType);
                    handled = Serializers.serializeBySerializer(ser, entity);
                    if (handled.isOk()) {
                        return handled.get();
                    }
                }
            }
        }

        throw WebServerException.notAcceptable("Could not find any compatible serializer to handle " +
                "the return value(type=" + entity.type().getName() +
                "), acceptMediaTypes: " + mediaTypes);
    }

    @Override
    protected boolean isSimpleType(ResponseEntity entity) {
        // try to extract the real type of the return value instance in every request and serialize it if
        // the response entity type is Object.class(maybe the real type of the response entity instance would
        // be String, byte[], ByteBuf, or primitives...)
        Class<?> entityType = entity.type();
        if (entityType == null) {
            return false;
        }
        return Object.class.equals(entityType)
                || Object.class.equals(FutureUtils.retrieveFirstGenericTypeOfFutureReturnType(entityType,
                entity.genericType()));
    }
}
