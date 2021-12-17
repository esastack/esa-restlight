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

import esa.commons.Primitives;
import esa.commons.StringUtils;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restlight.core.resolver.HandledValue;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.resolver.ResponseEntityChannel;
import io.esastack.restlight.core.resolver.ResponseEntityResolver;
import io.esastack.restlight.core.serialize.Serializers;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.route.predicate.ProducesPredicate;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.internal.InternalThreadLocalMap;

import java.util.Collections;
import java.util.List;

/**
 * The basic {@link ResponseEntityResolver} which is used resolve the given {@link ResponseEntity}. In order
 * to maintain a uniform style, we use a unified way to handle simple response entity types, such as
 * {@link CharSequence}, {@link byte}s, {@link ByteBuf} and {@link Primitives}, and you can get more information
 * from {@link Serializers#serializeIfPossible(Object, HttpResponse, MediaType)}.
 */
public abstract class AbstractResponseEntityResolver implements ResponseEntityResolver {

    /**
     * Whether to serialize the simple response entity by selected type or not.
     */
    private final boolean maySimpleType;

    protected AbstractResponseEntityResolver(boolean maySimpleType) {
        this.maySimpleType = maySimpleType;
    }

    @Override
    public HandledValue<Void> writeTo(ResponseEntity entity,
                                      ResponseEntityChannel channel,
                                      RequestContext context) throws Exception {
        if (!supports(entity)) {
            return HandledValue.failed();
        }
        final Object entityValue = context.response().entity();
        final HttpResponse response = context.response();
        final List<MediaType> mediaTypes = getMediaTypes(context);

        final byte[] serialized;
        if (maySimpleType && isSimpleType(entity)) {
            // try to serialize simple type by return value instance.
            final MediaType mediaType = selectMediaType(mediaTypes);
            byte[] ret = Serializers.serializeIfPossible(entityValue, response, mediaType);
            if (ret == null) {
                serialized = serialize(entity, mediaTypes, context);
            } else {
                serialized = ret;
            }
        } else {
            serialized = serialize(entity, mediaTypes, context);
        }

        channel.writeThenEnd(serialized);
        return HandledValue.succeed(null);
    }

    /**
     * Whether current resolver supports given {@code entity} or not.
     *
     * @param entity    response entity
     * @return  {@code true} if supports, otherwise {@code false}.
     */
    protected abstract boolean supports(ResponseEntity entity);

    /**
     * Whether the given {@code entity} should be handled as simple format.
     *
     * @param entity    entity
     * @return  {@code true} if the entity is simple format, otherwise {@code false}.
     */
    protected boolean isSimpleType(ResponseEntity entity) {
        return true;
    }

    protected MediaType selectMediaType(List<MediaType> mediaTypes) {
        final MediaType mediaType;
        if (mediaTypes.isEmpty()) {
            mediaType = null;
        } else {
            mediaType = mediaTypes.get(0);
        }
        return mediaType;
    }

    protected List<MediaType> getMediaTypes(RequestContext context) {
        List<MediaType> compatibleTypes = context.attr(ProducesPredicate.COMPATIBLE_MEDIA_TYPES).get();
        if (compatibleTypes == null) {
            String accept = context.request().headers().get(HttpHeaderNames.ACCEPT);
            if (!StringUtils.isEmpty(accept)) {
                List<MediaType> ret = InternalThreadLocalMap.get().arrayList();
                MediaTypeUtil.parseMediaTypes(accept, ret);
                return ret;
            }
        } else {
            return compatibleTypes;
        }
        return Collections.emptyList();
    }

    /**
     * Serializes the given {@code entityValue} to byte[] format.
     *
     * @param entity   response entity
     * @param mediaTypes    mediaTypes
     * @param context       context
     * @return      byte array.
     * @throws Exception    any exception
     */
    protected abstract byte[] serialize(ResponseEntity entity,
                                        List<MediaType> mediaTypes,
                                        RequestContext context) throws Exception;
}
