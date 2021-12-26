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
import esa.commons.ClassUtils;
import io.esastack.restlight.core.resolver.HandledValue;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.resolver.ResponseEntityChannel;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdapter;
import io.esastack.restlight.jaxrs.util.MediaTypeUtils;
import io.esastack.restlight.jaxrs.util.RuntimeDelegateUtils;
import io.esastack.restlight.server.context.RequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;

import java.util.List;

public class MessageBodyWriterAdapter<T> implements ResponseEntityResolverAdapter {

    private final MessageBodyWriter<T> underlying;
    private final Class<?> matchableType;
    private final List<MediaType> produces;
    private final int order;

    public MessageBodyWriterAdapter(MessageBodyWriter<T> underlying,
                                    Class<?> matchableType,
                                    List<MediaType> produces,
                                    int order) {
        Checks.checkNotNull(underlying, "underlying");
        Checks.checkNotNull(matchableType, "matchableType");
        Checks.checkNotNull(produces, "produces");
        this.underlying = underlying;
        this.matchableType = matchableType;
        this.produces = produces;
        this.order = order;
    }

    @Override
    public HandledValue<Void> writeTo(ResponseEntity entity,
                                      ResponseEntityChannel channel,
                                      RequestContext context) throws Exception {
        if (entity.response().entity() == null) {
            return HandledValue.failed();
        }
        Class<?> type = entity.type();
        if (type == null) {
            type = ClassUtils.getUserType(entity.response().entity());
        }
        if (!matchableType.isAssignableFrom(type)) {
            return HandledValue.failed();
        }

        MediaType mediaType = MediaTypeUtils.convert(entity.mediaType());
        if (!isCompatible(mediaType)
                || !underlying.isWriteable(entity.type(), entity.genericType(), entity.annotations(), mediaType)) {
            return HandledValue.failed();
        }
        @SuppressWarnings("unchecked")
        T value = (T) entity.response().entity();
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        RuntimeDelegateUtils.addHeadersToMap(context.response().headers(), headers);
        try {
            underlying.writeTo(value, entity.type(), entity.genericType(), entity.annotations(),
                    mediaType, headers, ResponseEntityStreamAutoClose.getNonClosableOutputStream(context));
        } finally {
            RuntimeDelegateUtils.addHeadersFromMap(context.response().headers(), headers, true);
        }

        return HandledValue.succeed(null);
    }

    @Override
    public int getOrder() {
        return order;
    }

    private boolean isCompatible(MediaType current) {
        if (produces.isEmpty()) {
            return true;
        }
        for (MediaType type : produces) {
            if (type.isCompatible(current)) {
                return true;
            }
        }
        return false;
    }
}

