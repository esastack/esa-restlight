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

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class DelegatingContainerRequestContextTest {

    @Test
    void testBasic() {
        assertThrows(NullPointerException.class, () -> new DelegatingContainerRequestContext(null));
        assertDoesNotThrow(() -> new DelegatingContainerRequestContext(mock(AbstractContainerRequestContext.class)));

        final AbstractContainerRequestContext context = mock(AbstractContainerRequestContext.class);
        final DelegatingContainerRequestContext delegating = new DelegatingContainerRequestContext(context);

        verify(context, never()).getProperty("name");
        delegating.getProperty("name");
        verify(context).getProperty("name");

        verify(context, never()).getPropertyNames();
        delegating.getPropertyNames();
        verify(context).getPropertyNames();

        verify(context, never()).setProperty("name", "value");
        delegating.setProperty("name", "value");
        verify(context).setProperty("name", "value");

        verify(context, never()).removeProperty("name");
        delegating.removeProperty("name");
        verify(context).removeProperty("name");

        verify(context, never()).getUriInfo();
        delegating.getUriInfo();
        verify(context).getUriInfo();

        verify(context, never()).getRequest();
        delegating.getRequest();
        verify(context).getRequest();

        verify(context, never()).getMethod();
        delegating.getMethod();
        verify(context).getMethod();

        verify(context, never()).getHeaders();
        delegating.getHeaders();
        verify(context).getHeaders();

        verify(context, never()).getHeaderString("name");
        delegating.getHeaderString("name");
        verify(context).getHeaderString("name");

        verify(context, never()).getDate();
        delegating.getDate();
        verify(context).getDate();

        verify(context, never()).getLanguage();
        delegating.getLanguage();
        verify(context).getLanguage();

        verify(context, never()).getLength();
        delegating.getLength();
        verify(context).getLength();

        verify(context, never()).getMediaType();
        delegating.getMediaType();
        verify(context).getMediaType();

        verify(context, never()).getAcceptableMediaTypes();
        delegating.getAcceptableMediaTypes();
        verify(context).getAcceptableMediaTypes();

        verify(context, never()).getAcceptableLanguages();
        delegating.getAcceptableLanguages();
        verify(context).getAcceptableLanguages();

        verify(context, never()).getCookies();
        delegating.getCookies();
        verify(context).getCookies();

        verify(context, never()).hasEntity();
        delegating.hasEntity();
        verify(context).hasEntity();

        verify(context, never()).getEntityStream();
        delegating.getEntityStream();
        verify(context).getEntityStream();

        verify(context, never()).getSecurityContext();
        delegating.getSecurityContext();
        verify(context).getSecurityContext();

        final SecurityContext securityContext = mock(SecurityContext.class);
        verify(context, never()).setSecurityContext(securityContext);
        delegating.setSecurityContext(securityContext);
        verify(context).setSecurityContext(securityContext);

        final Response response = mock(Response.class);
        verify(context, never()).abortWith(response);
        delegating.abortWith(response);
        verify(context).abortWith(response);

        final URI uri = URI.create("/abc");
        verify(context, never()).setRequestUri(uri);
        delegating.setRequestUri(uri);
        verify(context).setRequestUri(uri);

        verify(context, never()).setRequestUri(uri, uri);
        delegating.setRequestUri(uri, uri);
        verify(context).setRequestUri(uri, uri);

        verify(context, never()).setMethod("get");
        delegating.setMethod("get");
        verify(context).setMethod("get");

        final InputStream ins = mock(InputStream.class);
        verify(context, never()).setEntityStream(ins);
        delegating.setEntityStream(ins);
        verify(context).setEntityStream(ins);
    }

}

