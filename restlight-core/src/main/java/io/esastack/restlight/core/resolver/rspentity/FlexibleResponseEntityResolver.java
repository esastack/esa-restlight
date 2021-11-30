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
package io.esastack.restlight.core.resolver.rspentity;

import io.esastack.commons.net.http.MediaType;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.core.resolver.HandledValue;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.core.serialize.Serializers;
import io.esastack.restlight.core.util.FutureUtils;
import io.esastack.restlight.server.bootstrap.WebServerException;

import java.util.List;

/**
 * Implementation of {@link ParamResolverFactory} for resolving argument that annotated by the ResponseBody.
 */
public abstract class FlexibleResponseEntityResolver extends AbstractResponseEntityResolver {

    private final List<? extends HttpResponseSerializer> serializers;

    protected FlexibleResponseEntityResolver(List<? extends HttpResponseSerializer> serializers) {
        super(true);
        this.serializers = serializers;
    }

    @Override
    protected byte[] serialize(ResponseEntity entity, List<MediaType> mediaTypes, HttpRequest request)
            throws Exception {
        if (serializers.isEmpty()) {
            throw WebServerException.badRequest("Could not find any compatible serializer to handle " +
                    "the return value(type=" + entity.type().getName() +
                    "), acceptMediaTypes: " + mediaTypes);
        }

        HandledValue<byte[]> value;
        if (mediaTypes.isEmpty()) {
            for (HttpResponseSerializer ser : serializers) {
                value = Serializers.serializeBySerializer(ser, entity);
                if (value.isSuccess()) {
                    return value.value();
                }
            }
        } else {
            for (MediaType mediaType : mediaTypes) {
                for (HttpResponseSerializer ser : serializers) {
                    entity.mediaType(mediaType);
                    value = Serializers.serializeBySerializer(ser, entity);
                    if (value.isSuccess()) {
                        return value.value();
                    }
                }
            }
        }

        throw WebServerException.badRequest("Could not find any compatible serializer to handle " +
                "the return value(type=" + entity.type().getName() +
                "), acceptMediaTypes: " + mediaTypes);
    }

    @Override
    protected boolean isSimpleType(ResponseEntity entity) {
        // try to extract the real type of the return value instance in every request and serialize it if
        // the response entity type is Object.class(maybe the real type of the response entity instance would
        // be String, byte[], ByteBuf, or primitives...)
        return Object.class.equals(entity.type())
                || Object.class.equals(FutureUtils.retrieveFirstGenericTypeOfFutureReturnType(entity.type(),
                entity.genericType()));
    }
}
