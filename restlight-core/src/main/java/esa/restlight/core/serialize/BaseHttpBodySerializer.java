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

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.httpserver.core.HttpInputStream;
import esa.httpserver.core.HttpOutputStream;
import esa.restlight.core.util.MediaType;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.lang.reflect.Type;

public abstract class BaseHttpBodySerializer implements HttpBodySerializer {

    @Override
    public boolean supportsRead(MediaType mediaType, Type type) {
        return mediaType != null && MediaType.APPLICATION_JSON.isCompatibleWith(mediaType);
    }

    @Override
    public boolean supportsWrite(MediaType mediaType, Type type) {
        return mediaType != null && MediaType.APPLICATION_JSON.isCompatibleWith(mediaType);
    }

    @Override
    public Object customResponse(AsyncRequest request, AsyncResponse response, Object returnValue) {
        response.setHeader(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8.value());
        return returnValue;
    }

    @Override
    public byte[] serialize(Object target) throws Exception {
        return serializer().serialize(target);
    }

    @Override
    public <T> T deSerialize(byte[] data, Type type) throws Exception {
        if (data == null || data.length == 0) {
            return null;
        }
        return serializer().deSerialize(data, type);
    }

    @Override
    public void serialize(Object target, HttpOutputStream outputStream) throws Exception {
        serializer().serialize(target, outputStream);
    }

    @Override
    public <T> T deSerialize(HttpInputStream inputStream, Type type) throws Exception {
        if (inputStream.available() == 0) {
            return null;
        }
        return serializer().deSerialize(inputStream, type);
    }

    protected abstract Serializer serializer();
}
