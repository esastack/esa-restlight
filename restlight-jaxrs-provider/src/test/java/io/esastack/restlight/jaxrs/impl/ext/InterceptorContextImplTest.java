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
package io.esastack.restlight.jaxrs.impl.ext;

import esa.commons.collection.AttributeKey;
import esa.commons.collection.AttributeMap;
import esa.commons.collection.Attributes;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.context.HttpEntity;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.resolver.entity.EntityResolverContext;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class InterceptorContextImplTest {

    @Test
    void testBasic() {
        assertThrows(NullPointerException.class, () -> new InterceptorContextImpl(null));

        final Attributes attributes = new AttributeMap();
        final RequestContext context0 = new RequestContextImpl(attributes,
                mock(HttpRequest.class), mock(HttpResponse.class));
        final HttpEntity entity = mock(HttpEntity.class);

        final EntityResolverContext underlying = new EntityResolverContext() {
            @Override
            public RequestContext requestContext() {
                return context0;
            }

            @Override
            public HttpEntity httpEntity() {
                return entity;
            }
        };

        final InterceptorContextImpl context = new InterceptorContextImpl(underlying);
        assertNull(context.getProperty("name"));
        attributes.attr(AttributeKey.valueOf("name")).set("value1");
        assertEquals("value1", context.getProperty("name"));

        assertEquals(1, context.getPropertyNames().size());
        assertEquals("name", new ArrayList<>(context.getPropertyNames()).get(0));

        context.setProperty("name", "value2");
        assertEquals("value2", context.getProperty("name"));

        context.removeProperty("name");
        assertNull(context.getProperty("name"));

        assertEquals(1, context.getPropertyNames().size());

        verify(entity, never()).annotations();
        context.getAnnotations();
        verify(entity).annotations();

        verify(entity, never()).annotations(null);
        context.setAnnotations(null);
        verify(entity).annotations(null);

        verify(entity, never()).type();
        context.getType();
        verify(entity).type();

        verify(entity, never()).type(Object.class);
        context.setType(Object.class);
        verify(entity).type(Object.class);

        verify(entity, never()).genericType();
        context.getGenericType();
        verify(entity).genericType();

        verify(entity, never()).genericType(Object.class);
        context.setGenericType(Object.class);
        verify(entity).genericType(Object.class);

        verify(entity, never()).mediaType();
        context.getMediaType();
        verify(entity).mediaType();

        verify(entity, never()).mediaType(MediaType.ALL);
        context.setMediaType(jakarta.ws.rs.core.MediaType.WILDCARD_TYPE);
        verify(entity).mediaType(MediaType.ALL);
    }

}

