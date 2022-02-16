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
package io.esastack.restlight.jaxrs.impl.container;

import io.esastack.restlight.jaxrs.impl.core.ResponseImpl;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ContainerResponseContextImplTest {

    @Test
    void testBasic() {
        final OutputStream os = mock(OutputStream.class);
        final ResponseImpl response = mock(ResponseImpl.class);
        final ContainerResponseContextImpl context = new ContainerResponseContextImpl(os, response);
        verify(response, never()).getStatus();
        context.getStatus();
        verify(response, times(1)).getStatus();

        verify(response, never()).setStatus(200);
        context.setStatus(200);
        verify(response, times(1)).setStatus(200);

        verify(response, never()).getStatusInfo();
        context.getStatusInfo();
        verify(response, times(1)).getStatusInfo();

        verify(response, never()).setStatus(Response.Status.OK);
        context.setStatusInfo(Response.Status.OK);
        verify(response, times(1)).setStatus(Response.Status.OK);

        verify(response, never()).getHeaders();
        context.getHeaders();
        verify(response, times(1)).getHeaders();

        verify(response, never()).getStringHeaders();
        context.getStringHeaders();
        verify(response, times(1)).getStringHeaders();

        verify(response, never()).getHeaderString("a");
        context.getHeaderString("a");
        verify(response, times(1)).getHeaderString("a");

        verify(response, never()).getAllowedMethods();
        context.getAllowedMethods();
        verify(response, times(1)).getAllowedMethods();

        verify(response, never()).getDate();
        context.getDate();
        verify(response, times(1)).getDate();

        verify(response, never()).getLanguage();
        context.getLanguage();
        verify(response, times(1)).getLanguage();

        verify(response, never()).getLength();
        context.getLength();
        verify(response, times(1)).getLength();

        verify(response, never()).getMediaType();
        context.getMediaType();
        verify(response, times(1)).getMediaType();

        verify(response, never()).getCookies();
        context.getCookies();
        verify(response, times(1)).getCookies();

        verify(response, never()).getEntityTag();
        context.getEntityTag();
        verify(response, times(1)).getEntityTag();

        verify(response, never()).getLastModified();
        context.getLastModified();
        verify(response, times(1)).getLastModified();

        verify(response, never()).getLocation();
        context.getLocation();
        verify(response, times(1)).getLocation();

        verify(response, never()).getLinks();
        context.getLinks();
        verify(response, times(1)).getLinks();

        verify(response, never()).getLink("/abc");
        context.getLink("/abc");
        verify(response, times(1)).getLink("/abc");

        verify(response, never()).hasLink("/def");
        context.hasLink("/def");
        verify(response, times(1)).hasLink("/def");

        verify(response, never()).getLinkBuilder("/ddd");
        context.getLinkBuilder("/ddd");
        verify(response, times(1)).getLinkBuilder("/ddd");

        verify(response, never()).hasEntity();
        context.hasEntity();
        verify(response, times(1)).hasEntity();

        verify(response, never()).getEntity();
        context.getEntity();
        verify(response, times(1)).getEntity();
        context.getEntityClass();
        verify(response, times(2)).getEntity();
        context.getEntityType();
        verify(response, times(3)).getEntity();

        final Object entity = new Object();
        verify(response, never()).setEntity(entity);
        context.setEntity(entity);
        verify(response, times(1)).setEntity(entity);

        verify(response, never()).setEntity(entity, new Annotation[0],
                MediaType.WILDCARD_TYPE);
        context.setEntity(entity, new Annotation[0], MediaType.WILDCARD_TYPE);
        verify(response, times(1)).setEntity(entity, new Annotation[0],
                MediaType.WILDCARD_TYPE);

        verify(response, never()).getAnnotations();
        context.getEntityAnnotations();
        verify(response, times(1)).getAnnotations();

        assertSame(os, context.getEntityStream());
        final OutputStream os1 = mock(OutputStream.class);
        context.setEntityStream(os1);
        assertSame(os1, context.getEntityStream());
    }

}

