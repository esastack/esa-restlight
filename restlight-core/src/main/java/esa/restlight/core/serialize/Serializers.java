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
package esa.restlight.core.serialize;

import esa.commons.Primitives;
import esa.httpserver.core.AsyncResponse;
import esa.httpserver.core.HttpOutputStream;
import esa.restlight.core.resolver.ReturnValueResolver;
import esa.restlight.core.util.MediaType;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class Serializers {

    private static byte[] ALREADY_WRITE = ReturnValueResolver.ALREADY_WRITE;

    public static byte[] serializeBySerializer(HttpResponseSerializer serializer,
                                               Object returnValue,
                                               AsyncResponse response) throws Exception {
        if (serializer.preferStream()) {
            serializeAndCloseStream(serializer, response.outputStream(), returnValue);
            return alreadyWrite();
        }
        return serializer.serialize(returnValue);
    }

    public static boolean alreadyWrite(byte[] bytes) {
        return ALREADY_WRITE == bytes;
    }

    public static byte[] alreadyWrite() {
        return ALREADY_WRITE;
    }

    private static void serializeAndCloseStream(HttpResponseSerializer serializer,
                                                HttpOutputStream outputStream,
                                                Object obj) throws IOException {
        try {
            serializer.serialize(obj, outputStream);
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            outputStream.close();
        }
    }

    public static byte[] serializeCharSequence(CharSequence charSequence,
                                               AsyncResponse response,
                                               MediaType mediaType) {
        setMediaType(response, mediaType);
        return charSequence.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void setMediaType(AsyncResponse response, MediaType mediaType) {
        if (mediaType == null || mediaType.isWildcardType() || mediaType.isWildcardSubtype()) {
            response.setHeader(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        } else {
            response.setHeader(HttpHeaderNames.CONTENT_TYPE, mediaType.value());
        }
    }

    public static byte[] serializePrimitives(Object primitive,
                                             AsyncResponse response,
                                             MediaType mediaType) {
        setMediaType(response, mediaType);
        return String.valueOf(primitive).getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] serializeByteArray(byte[] bytes,
                                            AsyncResponse response,
                                            MediaType mediaType) {
        setMediaType(response, mediaType);
        return bytes;
    }

    public static byte[] serializeByteBuf(ByteBuf bytes,
                                          AsyncResponse response,
                                          MediaType mediaType) {
        setMediaType(response, mediaType);
        response.sendResult(bytes);
        return ALREADY_WRITE;
    }

    public static byte[] serializeIfPossible(Object value,
                                             AsyncResponse response,
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
