/*
 * Copyright 2021 OPPO ESA Stack Project
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
package io.esastack.restlight.core.resolver;

import esa.commons.StringUtils;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restlight.core.method.HandlerMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * This {@link HttpEntityImpl} is designed as not thread-safe and we excepted this should be used in the same thread.
 */
public class HttpEntityImpl implements HttpEntity {

    Class<?> type;
    Type genericType;
    Annotation[] annotations;

    private final HandlerMethod handler;
    private MediaType mediaType;

    public HttpEntityImpl(HandlerMethod handler, MediaType mediaType) {
        this.mediaType = mediaType;
        this.handler = handler;
    }

    @Override
    public Class<?> type() {
        return type;
    }

    @Override
    public Type genericType() {
        return genericType;
    }

    @Override
    public Annotation[] annotations() {
        return annotations;
    }

    @Override
    public MediaType mediaType() {
        return mediaType;
    }

    @Override
    public void type(Class<?> type) {
        this.type = type;
    }

    @Override
    public Optional<HandlerMethod> handler() {
        return Optional.ofNullable(handler);
    }

    @Override
    public void genericType(Type genericType) {
        this.genericType = genericType;
    }

    @Override
    public void annotations(Annotation[] annotations) {
        this.annotations = annotations;
    }

    @Override
    public void mediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    static MediaType parseMediaType(String mediaType) {
        if (StringUtils.isEmpty(mediaType)) {
            return null;
        }
        return MediaTypeUtil.parseMediaType(mediaType);
    }
}

