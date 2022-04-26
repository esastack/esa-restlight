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
import esa.commons.Result;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.resolver.ResponseEntityChannel;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdapter;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.core.util.ResponseEntityUtils;
import io.esastack.restlight.jaxrs.util.JaxrsUtils;
import io.esastack.restlight.jaxrs.util.MediaTypeUtils;
import io.esastack.restlight.server.context.RequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Providers;

public class MessageBodyWriterAdapter<T> implements ResponseEntityResolverAdapter {

    private final Providers providers;

    public MessageBodyWriterAdapter(Providers providers) {
        Checks.checkNotNull(providers, "providers");
        this.providers = providers;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<Void, Void> writeTo(ResponseEntity entity,
                                      ResponseEntityChannel channel,
                                      RequestContext context) throws Exception {
        Class<?> type = entity.type();
        if (type == null) {
            entity.type(ClassUtils.getUserType(entity.response().entity()));
        }

        for (io.esastack.commons.net.http.MediaType mediaType : ResponseEntityUtils.getMediaTypes(context)) {
            MediaType mediaType0 = MediaTypeUtils.convert(mediaType);
            MessageBodyWriter<T> writer = (MessageBodyWriter<T>) providers.getMessageBodyWriter(entity.type(),
                    entity.genericType(), entity.annotations(), mediaType0);
            if (writer != null) {
                T value = (T) entity.response().entity();
                MultivaluedMap<String, Object> headers = JaxrsUtils.convertToMap(context.response().headers());
                try {
                    writer.writeTo(value, entity.type(), entity.genericType(), entity.annotations(),
                            mediaType0, headers, ResponseEntityStreamUtils.getUnClosableOutputStream(context));
                } finally {
                    JaxrsUtils.convertThenAddToHeaders(headers, context.response().headers());
                }

                ResponseEntityStreamUtils.close(context);
                return Result.ok();
            }
        }
        return Result.err();
    }

    @Override
    public boolean supports(HandlerMethod method) {
        return true;
    }

    @Override
    public boolean alsoApplyWhenMissingHandler() {
        return true;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}

