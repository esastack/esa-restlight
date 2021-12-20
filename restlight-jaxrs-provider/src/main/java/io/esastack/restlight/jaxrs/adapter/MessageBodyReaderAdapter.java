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
package io.esastack.restlight.jaxrs.adapter;

import esa.commons.Checks;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandledValue;
import io.esastack.restlight.core.resolver.RequestEntity;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdapter;
import io.esastack.restlight.jaxrs.impl.core.ModifiableMultivaluedMap;
import io.esastack.restlight.jaxrs.util.MediaTypeUtils;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.MessageBodyReader;

import java.util.List;

public class MessageBodyReaderAdapter<T> implements RequestEntityResolverAdapter {

    private final MessageBodyReader<T> underlying;
    private final List<MediaType> consumes;
    private final int order;

    public MessageBodyReaderAdapter(MessageBodyReader<T> underlying, List<MediaType> consumes, int order) {
        Checks.checkNotNull(underlying, "underlying");
        Checks.checkNotNull(consumes, "consumes");
        this.consumes = consumes;
        this.underlying = underlying;
        this.order = order;
    }

    @Override
    public HandledValue<Object> readFrom(Param param, RequestEntity entity, RequestContext context) throws Exception {
        MediaType mediaType = MediaTypeUtils.convert(entity.mediaType());
        if (!isCompatible(mediaType)) {
            return HandledValue.failed();
        }
        if (!underlying.isReadable(entity.type(), entity.genericType(), entity.annotations(), mediaType)) {
            return HandledValue.failed();
        }
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) entity.type();
        return HandledValue.succeed(underlying.readFrom(clazz, entity.genericType(), entity.annotations(),
                mediaType, new ModifiableMultivaluedMap(context.request().headers()), entity.inputStream()));
    }

    @Override
    public boolean supports(Param param) {
        return true;
    }

    @Override
    public int getOrder() {
        return order;
    }

    private boolean isCompatible(MediaType current) {
        if (consumes.isEmpty()) {
            return true;
        }
        for (MediaType type : consumes) {
            if (type.isCompatible(current)) {
                return true;
            }
        }
        return false;
    }
}

