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

import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.httpserver.core.HttpInputStream;
import io.esastack.httpserver.core.HttpOutputStream;
import io.esastack.restlight.core.resolver.HandledValue;
import io.esastack.restlight.core.resolver.RequestEntity;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.lang.reflect.Type;

public abstract class BaseHttpBodySerializer implements HttpBodySerializer {

    @Override
    public <T> HandledValue<T> deserialize(RequestEntity entity) throws Exception {
        if (!supportsRead(entity)) {
            return HandledValue.failed();
        }
        if (preferStream()) {
            return doDeserialize(entity.inputStream(), entity.type());
        } else {
            return doDeserialize(entity.byteData(), entity.type());
        }
    }

    @Override
    public HandledValue<byte[]> serialize(ResponseEntity entity) throws Exception {
        if (!supportsWrite(entity)) {
            return HandledValue.failed();
        }
        addContentType(entity);
        return HandledValue.succeed(doSerialize(entity.entity()));
    }

    @Override
    public HandledValue<Void> serialize(ResponseEntity entity, HttpOutputStream outputStream) throws Exception {
        if (!supportsWrite(entity)) {
            return HandledValue.failed();
        }
        addContentType(entity);
        doSerialize(entity.entity(), outputStream);
        return HandledValue.succeed(null);
    }

    protected byte[] doSerialize(Object target) throws Exception {
        return serializer().serialize(target);
    }

    protected void doSerialize(Object target, HttpOutputStream outputStream) throws Exception {
        serializer().serialize(target, outputStream);
    }

    protected  <T> T doDeserialize(byte[] data, Type type) throws Exception {
        if (data == null || data.length == 0) {
            return null;
        }
        return serializer().deserialize(data, type);
    }

    protected <T> T doDeserialize(HttpInputStream inputStream, Type type) throws Exception {
        if (inputStream.available() == 0) {
            return null;
        }
        return serializer().deserialize(inputStream, type);
    }

    /**
     * Whether supports to serialize the given {@code entity} or not.
     *
     * @param entity    entity
     * @return  {@code true} meas support, otherwise not.
     */
    protected boolean supportsWrite(ResponseEntity entity) {
        MediaType mediaType = entity.mediaType();
        return mediaType != null && MediaTypeUtil.APPLICATION_JSON.isCompatibleWith(mediaType);
    }

    /**
     * Whether supports to deserialize the given {@code entity} or not.
     *
     * @param entity    entity
     * @return  {@code true} meas support, otherwise not.
     */
    protected boolean supportsRead(RequestEntity entity) {
        MediaType mediaType = entity.mediaType();
        return mediaType != null && MediaTypeUtil.APPLICATION_JSON.isCompatibleWith(mediaType);
    }

    /**
     * Adds the content-type to given {@code response}.
     *
     * @param entity  response entity
     */
    protected void addContentType(ResponseEntity entity) {
        entity.response().setHeader(HttpHeaderNames.CONTENT_TYPE, MediaTypeUtil.APPLICATION_JSON_UTF8.value());
    }

    /**
     * Obtains the {@link Serializer} to use.
     *
     * @return  serializer
     */
    protected abstract Serializer serializer();
}
