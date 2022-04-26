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
import esa.commons.Result;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.RequestEntity;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdapter;
import io.esastack.restlight.jaxrs.impl.core.ModifiableMultivaluedMap;
import io.esastack.restlight.jaxrs.util.MediaTypeUtils;
import io.esastack.restlight.server.context.RequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Providers;

public class MessageBodyReaderAdapter<T> implements RequestEntityResolverAdapter {

    private final Providers providers;

    public MessageBodyReaderAdapter(Providers providers) {
        Checks.checkNotNull(providers, "providers");
        this.providers = providers;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<?, Void> readFrom(RequestEntity entity, RequestContext context) throws Exception {
        Class<?> type;
        if ((type = entity.type()) == null) {
            return Result.err();
        }
        MediaType mediaType = MediaTypeUtils.convert(entity.mediaType());
        MessageBodyReader<T> reader = (MessageBodyReader<T>) providers.getMessageBodyReader(type,
                entity.genericType(),
                entity.annotations(),
                mediaType);
        if (reader == null) {
            return Result.err();
        }
        Object value = reader.readFrom((Class<T>) type, entity.genericType(), entity.annotations(),
                mediaType, new ModifiableMultivaluedMap(context.request().headers()), entity.inputStream());
        return Result.ok(value);
    }

    @Override
    public boolean supports(Param param) {
        // because the mediaType/type/genericType may be updated during ReaderInterceptor, so we can't
        // bind this to param at the starting time.
        return true;
    }
}

