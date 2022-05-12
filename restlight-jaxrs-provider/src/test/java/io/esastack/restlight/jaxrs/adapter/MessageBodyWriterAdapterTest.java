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

import esa.commons.Result;
import esa.commons.collection.AttributeKey;
import esa.commons.collection.AttributeMap;
import esa.commons.collection.Attributes;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.restlight.core.context.HttpOutputStream;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.ResponseContent;
import io.esastack.restlight.core.context.ResponseEntity;
import io.esastack.restlight.core.context.ResponseEntityChannel;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverContext;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverContextImpl;
import io.esastack.restlight.core.route.predicate.ProducesPredicate;
import io.netty.buffer.ByteBufAllocator;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Providers;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageBodyWriterAdapterTest {

    @Test
    void testWriteTo() throws Throwable {
        assertThrows(NullPointerException.class, () -> new MessageBodyWriterAdapter<>(null));

        final Providers providers = mock(Providers.class);
        final MessageBodyWriterAdapter<?> adapter = new MessageBodyWriterAdapter<>(providers);
        assertEquals(90, adapter.getOrder());

        final HttpResponse response = mock(HttpResponse.class);
        final ResponseEntity entity = mock(ResponseEntity.class);
        final Attributes attrs = new AttributeMap();
        when(entity.response()).thenReturn(response);
        final RequestContext context = new RequestContextImpl(attrs, mock(HttpRequest.class), response);
        final ResponseEntityResolverContext resolverContext =
                new ResponseEntityResolverContextImpl(context, entity, mock(ResponseEntityChannel.class));
        assertFalse(adapter.resolve(resolverContext).isOk());

        // MessageBodyWriter is null.
        final HttpOutputStream os = mock(HttpOutputStream.class);
        attrs.attr(AttributeKey.valueOf("$closable.stream")).set(os);
        attrs.attr(ProducesPredicate.COMPATIBLE_MEDIA_TYPES).set(Collections.singletonList(MediaType.ALL));
        when(response.entity()).thenReturn("DEF");
        when(providers.getMessageBodyWriter(any(), any(), any(), any())).thenReturn(null);
        assertFalse(adapter.resolve(resolverContext).isOk());
        verify(os, never()).close();

        // MessageBodyWriter is not null.
        final HttpHeaders headers = new Http1HeadersImpl();
        when(response.headers()).thenReturn(headers);
        headers.add("name0", "value0");

        final AtomicReference<Object> writableValue = new AtomicReference<>();
        final AtomicReference<Class<?>> writableClass = new AtomicReference<>();
        final AtomicReference<Type> writableType = new AtomicReference<>();
        final AtomicReference<Annotation[]> writableAnnotations = new AtomicReference<>();
        final AtomicReference<jakarta.ws.rs.core.MediaType> writableMediaType = new AtomicReference<>();
        final MultivaluedMap<String, Object> writableHeaders = new MultivaluedHashMap<>();
        final AtomicReference<OutputStream> writableOs = new AtomicReference<>();
        final MessageBodyWriter<?> writer = mock(MessageBodyWriter.class);
        doAnswer(invocationOnMock -> {
            writableValue.set(invocationOnMock.getArguments()[0]);
            writableClass.set((Class<?>) invocationOnMock.getArguments()[1]);
            writableType.set((Type) invocationOnMock.getArguments()[2]);
            writableAnnotations.set((Annotation[]) invocationOnMock.getArguments()[3]);
            writableMediaType.set((jakarta.ws.rs.core.MediaType) invocationOnMock.getArguments()[4]);
            writableHeaders.putAll((MultivaluedMap<String, Object>) invocationOnMock.getArguments()[5]);
            writableOs.set((OutputStream) invocationOnMock.getArguments()[6]);
            return null;
        }).when(writer).writeTo(any(), any(), any(), any(), any(), any(), any());
        doReturn(writer).when(providers).getMessageBodyWriter(any(), any(), any(), any());

        final ResponseContent content = mock(ResponseContent.class);
        context.attrs().attr(RequestContextImpl.RESPONSE_CONTENT).set(content);
        when(content.alloc()).thenReturn(ByteBufAllocator.DEFAULT);
        final Result<Void, Void> handled = adapter.resolve(resolverContext);
        assertTrue(handled.isOk());
        verify(os).close();
        assertEquals(1, writableHeaders.size());
        assertEquals("value0", writableHeaders.getFirst("name0"));
        assertEquals("DEF", writableValue.get());
        assertNull(writableClass.get());
        assertNull(writableType.get());
        assertNull(writableAnnotations.get());
        assertEquals(jakarta.ws.rs.core.MediaType.WILDCARD_TYPE, writableMediaType.get());
    }

}

