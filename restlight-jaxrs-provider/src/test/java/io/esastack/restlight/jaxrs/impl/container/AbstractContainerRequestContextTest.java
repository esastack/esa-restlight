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

import esa.commons.collection.AttributeKey;
import esa.commons.collection.AttributeMap;
import esa.commons.collection.Attributes;
import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.buffer.BufferAllocator;
import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.netty.http.CookieImpl;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.esastack.restlight.server.core.HttpInputStream;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.util.DateUtils;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractContainerRequestContextTest {

    @Test
    void testBasic() {
        final Buffer buffer = BufferAllocator.getDefault().buffer();
        final HttpResponse response = mock(HttpResponse.class);
        final HttpHeaders headers = new Http1HeadersImpl();
        final HttpRequest request = mock(HttpRequest.class);
        final Attributes attrs = new AttributeMap();
        when(request.headers()).thenReturn(headers);
        when(request.body()).thenReturn(buffer);
        when(request.scheme()).thenReturn("https");

        final AbstractContainerRequestContext context = new ContainerRequestContextImpl(
                new RequestContextImpl(attrs, request, response));
        final int defaultAttrsCount = attrs.size();

        assertNull(context.getProperty("ab"));
        attrs.attr(AttributeKey.valueOf("ab")).set("xyz");
        assertEquals("xyz", context.getProperty("ab"));
        assertEquals(defaultAttrsCount + 1, context.getPropertyNames().size());
        assertTrue(context.getPropertyNames().contains("ab"));
        assertThrows(UnsupportedOperationException.class, () -> context.getPropertyNames().add("def"));

        context.setProperty("ab", null);
        assertNull(context.getProperty("ab"));
        assertEquals(defaultAttrsCount, context.getPropertyNames().size());
        context.setProperty("ab", "xyz0");
        assertEquals("xyz0", context.getProperty("ab"));
        assertEquals(defaultAttrsCount + 1, context.getPropertyNames().size());

        context.removeProperty("ab");
        assertNull(context.getProperty("ab"));
        assertEquals(defaultAttrsCount, context.getPropertyNames().size());

        assertNotNull(context.getUriInfo());
        assertNotNull(context.getRequest());

        when(request.method()).thenReturn(HttpMethod.GET);
        assertEquals("GET", context.getMethod());

        headers.add("name", "LiMing");
        headers.add("age", "26");
        assertEquals(2, context.getHeaders().size());
        assertEquals("LiMing", context.getHeaders().getFirst("name"));
        assertEquals("26", context.getHeaders().getFirst("age"));
        context.getHeaders().add("address", "China");
        context.getHeaders().remove("name");
        context.getHeaders().remove("age");
        assertEquals(1, context.getHeaders().size());
        assertEquals("China", context.getHeaders().getFirst("address"));

        assertEquals("China", context.getHeaderString("address"));

        assertNull(context.getDate());
        assertNull(context.getLanguage());
        assertTrue(context.getAcceptableLanguages().isEmpty());
        assertEquals(0, context.getLength());
        assertNull(context.getMediaType());
        assertTrue(context.getAcceptableMediaTypes().isEmpty());
        assertTrue(context.getCookies().isEmpty());
        assertFalse(context.hasEntity());

        headers.add("date", DateUtils.formatByCache(System.currentTimeMillis()));
        assertNotNull(context.getDate());
        when(request.contentLength()).thenReturn(111L);
        assertEquals(111, context.getLength());
        headers.add("Content-Language", "en-US");
        Locale locale = context.getLanguage();
        assertEquals("US", locale.getCountry());
        assertEquals("en", locale.getLanguage());
        when(request.contentType()).thenReturn(MediaType.ALL);
        assertEquals(new jakarta.ws.rs.core.MediaType(), context.getMediaType());
        final List<MediaType> accepts = new ArrayList<>();
        accepts.add(MediaType.ALL);
        accepts.add(MediaType.APPLICATION_JSON);
        when(request.accepts()).thenReturn(accepts);
        assertEquals(2, context.getAcceptableMediaTypes().size());
        assertEquals(new jakarta.ws.rs.core.MediaType(), context.getAcceptableMediaTypes().get(0));
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON,
                context.getAcceptableMediaTypes().get(1).toString());

        final Set<Cookie> cookies = new HashSet<>();
        cookies.add(new CookieImpl("a", "b"));
        cookies.add(new CookieImpl("c", "d"));
        when(request.cookies()).thenReturn(cookies);
        assertEquals(2, context.getCookies().size());
        assertEquals("b", context.getCookies().get("a").getValue());
        assertEquals("d", context.getCookies().get("c").getValue());

        headers.add(HttpHeaderNames.ACCEPT_LANGUAGE, "en-Us");
        headers.add(HttpHeaderNames.ACCEPT_LANGUAGE, "de-DE");
        assertEquals(2, context.getAcceptableLanguages().size());
        assertEquals("US", context.getAcceptableLanguages().get(0).getCountry());
        assertEquals("en", context.getAcceptableLanguages().get(0).getLanguage());
        assertEquals("DE", context.getAcceptableLanguages().get(1).getCountry());
        assertEquals("de", context.getAcceptableLanguages().get(1).getLanguage());

        String s = "Hello";
        buffer.writeBytes(s.getBytes(StandardCharsets.UTF_8));
        assertTrue(context.hasEntity());
        final HttpInputStream stream = mock(HttpInputStream.class);
        when(request.inputStream()).thenReturn(stream);
        assertSame(stream, context.getEntityStream());

        assertNull(context.getSecurityContext());
        final SecurityContext sCtx = mock(SecurityContext.class);
        context.setSecurityContext(sCtx);
        assertSame(sCtx, context.getSecurityContext());

        final Response rsp = Response.accepted().build();
        context.abortWith(rsp);
        assertTrue(context.isAborted());
        verify(response).entity(rsp);
    }


    private static class ContainerRequestContextImpl extends AbstractContainerRequestContext {

        private ContainerRequestContextImpl(RequestContext context) {
            super(context);
        }

        @Override
        public void setRequestUri(URI requestUri) {

        }

        @Override
        public void setRequestUri(URI baseUri, URI requestUri) {

        }

        @Override
        public void setMethod(String method) {

        }

        @Override
        public void setEntityStream(InputStream input) {

        }
    }
}

