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

import com.google.protobuf.Message;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.resolver.RequestEntity;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.server.core.HttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.AsciiString;

import java.nio.charset.StandardCharsets;

public class ProtoBufHttpBodySerializer extends BaseHttpBodySerializer {

    /**
     * The media-type for protobuf {@code application/x-protobuf}.
     */
    public static final MediaType PROTOBUF
            = MediaType.builder("application", "x-protobuf").charset(StandardCharsets.UTF_8).build();

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
    protected boolean supportsWrite(ResponseEntity entity) {
        return PROTOBUF.isCompatibleWith(entity.mediaType());
    }

    @Override
    protected boolean supportsRead(RequestEntity entity) {
        return PROTOBUF.isCompatibleWith(entity.mediaType());
    }

    @Override
    protected void addContentType(ResponseEntity entity) {
        Object entityValue = entity.response().entity();
        HttpResponse response = entity.response();
        if (entityValue instanceof Message) {
            Message message = (Message) entityValue;
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, PROTOBUF.value());
            response.headers().set(X_PROTOBUF_SCHEMA_HEADER, message.getDescriptorForType().getFile().getName());
            response.headers().set(X_PROTOBUF_MESSAGE_HEADER, message.getDescriptorForType().getFullName());
        }
    }

    @Override
    protected Serializer serializer() {
        return serializer;
    }
}
