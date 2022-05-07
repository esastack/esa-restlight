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

import esa.commons.Result;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.context.RequestEntity;
import io.esastack.restlight.core.context.ResponseEntity;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.lang.reflect.Type;

public abstract class BaseHttpBodySerializer implements HttpBodySerializer {

    @Override
    public <T> Result<T, Void> deserialize(RequestEntity entity) throws Exception {
        if (!supportsRead(entity)) {
            return Result.err();
        }

        return Result.ok(doDeserialize(entity.body().getBytes(), entity.type()));
    }

    @Override
    public Result<byte[], Void> serialize(ResponseEntity entity) throws Exception {
        if (!supportsWrite(entity)) {
            return Result.err();
        }
        addContentType(entity);
        return Result.ok(doSerialize(entity.response().entity()));
    }

    protected byte[] doSerialize(Object target) throws Exception {
        return serializer().serialize(target);
    }

    protected <T> T doDeserialize(byte[] data, Type type) throws Exception {
        if (data == null || data.length == 0) {
            return null;
        }
        return serializer().deserialize(data, type);
    }

    /**
     * Whether supports to serialize the given {@code entity} or not.
     *
     * @param entity entity
     * @return {@code true} meas support, otherwise not.
     */
    protected boolean supportsWrite(ResponseEntity entity) {
        MediaType mediaType = entity.mediaType();
        return mediaType != null && MediaType.APPLICATION_JSON.isCompatibleWith(mediaType);
    }

    /**
     * Whether supports to deserialize the given {@code entity} or not.
     *
     * @param entity entity
     * @return {@code true} meas support, otherwise not.
     */
    protected boolean supportsRead(RequestEntity entity) {
        MediaType mediaType = entity.mediaType();
        return mediaType != null && MediaType.APPLICATION_JSON.isCompatibleWith(mediaType);
    }

    /**
     * Adds the content-type to given {@code response}.
     *
     * @param entity response entity
     */
    protected void addContentType(ResponseEntity entity) {
        entity.response().headers().set(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8.value());
    }

    /**
     * Obtains the {@link Serializer} to use.
     *
     * @return serializer
     */
    protected abstract Serializer serializer();
}
