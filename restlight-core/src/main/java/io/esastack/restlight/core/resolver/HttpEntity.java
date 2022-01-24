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

import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.handler.Handler;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.handler.Filter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Optional;

public interface HttpEntity {

    /**
     * Obtains the type of current entity.
     *
     * @return type
     */
    Class<?> type();

    /**
     * Sets with the given {@code type}.
     *
     * @param type type
     */
    void type(Class<?> type);

    /**
     * Obtains the generic type corresponds to current entity.
     *
     * @return generic type
     */
    Type genericType();

    /**
     * Sets with the given {@link Type}.
     *
     * @param genericType generic type
     */
    void genericType(Type genericType);

    /**
     * Obtains the {@link Annotation}s corresponds to current entity.
     *
     * @return annotations
     */
    Annotation[] annotations();

    /**
     * Sets with the given {@code annotations}.
     *
     * @param annotations annotations
     */
    void annotations(Annotation[] annotations);

    /**
     * Obtains the {@link MediaType} corresponds to current entity.
     *
     * @return mediaType
     */
    MediaType mediaType();

    /**
     * Sets with the given {@code mediaType}.
     *
     * @param mediaType mediaType
     */
    void mediaType(MediaType mediaType);

    /**
     * Obtains the {@link HandlerMethod} if current value is produced by
     * {@link Handler#invoke(RequestContext, Object[])}, otherwise the value may be produces by {@link Filter}.
     *
     * @return handler method if present.
     */
    Optional<HandlerMethod> handler();
}

