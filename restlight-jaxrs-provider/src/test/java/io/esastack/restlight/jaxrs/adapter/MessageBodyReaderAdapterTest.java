/*
 * Copyright 2022 OPPO ESA Stack Project
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

import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandledValue;
import io.esastack.restlight.core.resolver.RequestEntity;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.HttpRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Providers;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MessageBodyReaderAdapterTest {

    @Test
    void testBasic() {
        assertThrows(NullPointerException.class, () -> new MessageBodyWriterAdapter<>(null));
        MessageBodyReaderAdapter<?> adapter = new MessageBodyReaderAdapter<>(mock(Providers.class));
        assertEquals(Ordered.LOWEST_PRECEDENCE, adapter.getOrder());
        assertTrue(adapter.supports(mock(Param.class)));
    }

    @Test
    void testReadFrom() throws Throwable {
        final Providers providers = mock(Providers.class);
        final RequestEntity entity = mock(RequestEntity.class);
        final RequestContext context = mock(RequestContext.class);
        final HttpRequest request = mock(HttpRequest.class);
        when(context.request()).thenReturn(request);
        when(request.headers()).thenReturn(new Http1HeadersImpl());
        MessageBodyReaderAdapter<?> adapter = new MessageBodyReaderAdapter<>(providers);
        assertFalse(adapter.readFrom(null, entity, context).isSuccess());

        doReturn(String.class).when(entity).type();
        when(entity.mediaType()).thenReturn(MediaType.ALL);
        when(providers.getMessageBodyReader(any(), any(), any(), any())).thenReturn(null);
        assertFalse(adapter.readFrom(null, entity, context).isSuccess());

        when(providers.getMessageBodyReader(any(), any(), any(), any())).thenReturn(new MessageBodyReader<Object>() {
            @Override
            public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
                                      jakarta.ws.rs.core.MediaType mediaType) {
                return true;
            }

            @Override
            public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations,
                                   jakarta.ws.rs.core.MediaType mediaType,
                                   MultivaluedMap<String, String> httpHeaders,
                                   InputStream entityStream) throws WebApplicationException {
                return "DEF";
            }
        });
        HandledValue<Object> handled = adapter.readFrom(null, entity, context);
        assertTrue(handled.isSuccess());
        assertEquals("DEF", handled.value());
    }

}

