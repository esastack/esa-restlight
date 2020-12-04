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

import esa.restlight.core.resolver.ReturnValueResolver;
import esa.restlight.core.util.MediaType;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SerializersTest {

    private final Pojo pojo = new Pojo("pojo", 27);

    @Test
    void serializeBySerializer() throws Exception {
        final PojoProtobuf.Pojo.Builder pojoBuilder = PojoProtobuf.Pojo.newBuilder().setName("pojo").setAge(27);
        final PojoProtobuf.Pojo pojoProto = pojoBuilder.build();
        final JacksonHttpBodySerializer jacksonHttpBodySerializer = new JacksonHttpBodySerializer();
        final GsonHttpBodySerializer gsonHttpBodySerializer = new GsonHttpBodySerializer();
        final FastJsonHttpBodySerializer fastJsonHttpBodySerializer = new FastJsonHttpBodySerializer();
        final ProtoBufHttpBodySerializer protoBufHttpBodySerializer = new ProtoBufHttpBodySerializer();
        final MockAsyncResponse response = new MockAsyncResponse();
        final byte[] pojoBytesJackson = jacksonHttpBodySerializer.serialize(pojo);
        assertArrayEquals(pojoBytesJackson,
                Serializers.serializeBySerializer(jacksonHttpBodySerializer, pojo, response));
        final byte[] pojoBytesGson = gsonHttpBodySerializer.serialize(pojo);
        assertArrayEquals(pojoBytesGson,
                Serializers.serializeBySerializer(gsonHttpBodySerializer, pojo, response));
        final byte[] pojoBytesFastJson = fastJsonHttpBodySerializer.serialize(pojo);
        assertArrayEquals(pojoBytesFastJson,
                Serializers.serializeBySerializer(fastJsonHttpBodySerializer, pojo, response));
        final byte[] pojoBytesProto = protoBufHttpBodySerializer.serialize(pojoProto);
        assertArrayEquals(pojoBytesProto, pojoProto.toByteArray());

        final BaseHttpBodySerializer baseHttpBodySerializer = new BaseHttpBodySerializer() {
            @Override
            public boolean preferStream() {
                return true;
            }

            @Override
            protected Serializer serializer() {
                return new JacksonSerializer();
            }
        };
        assertEquals(ReturnValueResolver.ALREADY_WRITE,
                Serializers.serializeBySerializer(baseHttpBodySerializer, pojo, response));
        assertArrayEquals(pojoBytesJackson, response.getSentData().toString(StandardCharsets.UTF_8).getBytes());

    }

    @Test
    void alreadyWrite() {
        assertFalse(Serializers.alreadyWrite(new byte[]{}));
        assertTrue(Serializers.alreadyWrite(ReturnValueResolver.ALREADY_WRITE));
    }

    @Test
    void testAlreadyWrite() {
        assertArrayEquals(Serializers.alreadyWrite(), ReturnValueResolver.ALREADY_WRITE);
    }

    @Test
    void serializeCharSequence() {
        final MockAsyncResponse asyncResponse = new MockAsyncResponse();
        final CharSequence charSequence = pojo.toString();
        assertArrayEquals(pojo.toString().getBytes(StandardCharsets.UTF_8),
                Serializers.serializeCharSequence(charSequence, asyncResponse, null));
        assertEquals(asyncResponse.getHeader(HttpHeaderNames.CONTENT_TYPE), MediaType.APPLICATION_OCTET_STREAM_VALUE);

        Serializers.serializeCharSequence(charSequence, asyncResponse, MediaType.APPLICATION_JSON);
        assertEquals(asyncResponse.getHeader(HttpHeaderNames.CONTENT_TYPE), MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void serializePrimitives() {
        final MockAsyncResponse asyncResponse = new MockAsyncResponse();
        assertArrayEquals(pojo.toString().getBytes(StandardCharsets.UTF_8),
                Serializers.serializePrimitives(pojo, asyncResponse, null));
    }

    @Test
    void serializeByteArray() {
        final byte[] bytes = pojo.toString().getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(bytes, Serializers.serializeByteArray(bytes, new MockAsyncResponse(), null));
    }

    @Test
    void serializeByteBuf() {
        final ByteBuf byteBuf = Unpooled.wrappedBuffer(pojo.toString().getBytes(StandardCharsets.UTF_8));
        assertEquals(Serializers.serializeByteBuf(byteBuf,
                new MockAsyncResponse(),
                null),
                Serializers.alreadyWrite());
    }

    @Test
    void serializeIfPossible() {
        final MockAsyncResponse response = new MockAsyncResponse();
        final CharSequence charSequence = pojo.toString();
        final Integer primitivesWrapper = 1;
        final ByteBuf byteBuf = Unpooled.wrappedBuffer(pojo.toString().getBytes(StandardCharsets.UTF_8));
        byte[] bytes = pojo.toString().getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(bytes, Serializers.serializeIfPossible(charSequence, response, null));
        assertArrayEquals(bytes, Serializers.serializeIfPossible(bytes, response, null));
        assertArrayEquals(Serializers.alreadyWrite(),
                Serializers.serializeIfPossible(byteBuf, response, null));
        assertArrayEquals(null, Serializers.serializeIfPossible(pojo, response, null));
        assertArrayEquals(String.valueOf(primitivesWrapper).getBytes(),
                Serializers.serializeIfPossible(primitivesWrapper, response, null));
    }
}
