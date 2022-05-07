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
package io.esastack.restlight.core.serialize;

import esa.commons.Primitives;
import esa.commons.Result;
import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.context.ResponseEntity;
import io.esastack.restlight.core.context.HttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.nio.charset.StandardCharsets;

public final class Serializers {

    public static final byte[] ALREADY_WRITE = new byte[0];

    public static Result<byte[], Void> serializeBySerializer(HttpResponseSerializer serializer,
                                                       ResponseEntity entity) throws Exception {
        return serializer.serialize(entity);
    }

    public static byte[] serializeCharSequence(CharSequence charSequence,
                                               HttpResponse response,
                                               MediaType mediaType) {
        setMediaType(response, mediaType);
        return charSequence.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void setMediaType(HttpResponse response, MediaType mediaType) {
        if (mediaType == null || mediaType.isWildcardType() || mediaType.isWildcardSubtype()) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        } else {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, mediaType.value());
        }
    }

    public static byte[] serializePrimitives(Object primitive,
                                             HttpResponse response,
                                             MediaType mediaType) {
        setMediaType(response, mediaType);
        return String.valueOf(primitive).getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] serializeByteArray(byte[] bytes,
                                            HttpResponse response,
                                            MediaType mediaType) {
        setMediaType(response, mediaType);
        return bytes;
    }

    public static byte[] serializeByteBuf(ByteBuf byteBuf,
                                          HttpResponse response,
                                          MediaType mediaType) {
        setMediaType(response, mediaType);
        response.entity(BufferUtil.wrap(byteBuf));
        return ALREADY_WRITE;
    }

    public static byte[] serializeIfPossible(Object value,
                                             HttpResponse response,
                                             MediaType mediaType) {
        if (value instanceof CharSequence) {
            return serializeCharSequence((CharSequence) value, response, mediaType);
        }
        if (value instanceof byte[]) {
            return serializeByteArray((byte[]) value, response, mediaType);
        }
        if (value instanceof ByteBuf) {
            return serializeByteBuf((ByteBuf) value, response, mediaType);
        }

        Class<?> type = value.getClass();
        if (Primitives.isPrimitiveOrWraperType(type)) {
            return serializePrimitives(value, response, mediaType);
        }
        return null;
    }

}
