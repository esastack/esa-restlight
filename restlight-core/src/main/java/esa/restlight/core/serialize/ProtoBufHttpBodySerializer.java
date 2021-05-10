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

import com.google.protobuf.Message;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.httpserver.core.HttpInputStream;
import esa.httpserver.core.HttpOutputStream;
import esa.restlight.core.util.MediaType;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.AsciiString;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class ProtoBufHttpBodySerializer implements HttpBodySerializer {

    /**
     * The media-type for protobuf {@code application/x-protobuf}.
     */
    public static final MediaType PROTOBUF
            = MediaType.of("application", "x-protobuf", StandardCharsets.UTF_8);

    /**
     * The HTTP header containing the protobuf schema.
     */
    public static final AsciiString X_PROTOBUF_SCHEMA_HEADER = AsciiString.cached("X-Protobuf-Schema");

    /**
     * The HTTP header containing the protobuf message.
     */
    public static final AsciiString X_PROTOBUF_MESSAGE_HEADER = AsciiString.cached("X-Protobuf-Message");

    private final ProtoBufSerializer serializer;

    public ProtoBufHttpBodySerializer() {
        this(new ProtoBufSerializer());
    }

    public ProtoBufHttpBodySerializer(ProtoBufSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public boolean supportsWrite(MediaType mediaType, Type type) {
        return PROTOBUF.isCompatibleWith(mediaType);
    }

    @Override
    public boolean supportsRead(MediaType mediaType, Type type) {
        return PROTOBUF.isCompatibleWith(mediaType);
    }

    @Override
    public Object customResponse(AsyncRequest request, AsyncResponse response, Object returnValue) {
        if (returnValue instanceof Message) {
            Message message = (Message) returnValue;
            response.setHeader(HttpHeaderNames.CONTENT_TYPE, PROTOBUF.value());
            response.setHeader(X_PROTOBUF_SCHEMA_HEADER, message.getDescriptorForType().getFile().getName());
            response.setHeader(X_PROTOBUF_MESSAGE_HEADER, message.getDescriptorForType().getFullName());
        }
        return returnValue;
    }

    @Override
    public byte[] serialize(Object target) {
        return serializer.serialize(target);
    }

    @Override
    public void serialize(Object target, HttpOutputStream outputStream) throws Exception {
        serializer.serialize(target, outputStream);
    }

    @Override
    public <T> T deserialize(byte[] data, Type type) throws Exception {
        if (data == null || data.length == 0) {
            return null;
        }
        return serializer.deserialize(data, type);
    }

    @Override
    public <T> T deserialize(HttpInputStream inputStream, Type type) throws Exception {
        if (inputStream.available() == 0) {
            return null;
        }
        return serializer.deserialize(inputStream, type);
    }

    @Override
    public <T> T deSerialize(byte[] data, Type type) throws Exception {
        return deserialize(data, type);
    }

    @Override
    public <T> T deSerialize(HttpInputStream inputStream, Type type) throws Exception {
        return deserialize(inputStream, type);
    }
}
