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
package io.esastack.restlight.jaxrs.impl.core;

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.netty.http.CookieImpl;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.util.DateUtils;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static io.esastack.commons.net.http.HttpHeaderNames.ACCEPT_LANGUAGE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpRequestHeadersTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new HttpRequestHeaders(null));
        assertDoesNotThrow(() -> new HttpRequestHeaders(MockHttpRequest.aMockRequest().build()));
    }

    @Test
    void testBasic() {
        final HttpHeaders headers = new Http1HeadersImpl();
        final HttpRequest request = mock(HttpRequest.class);
        when(request.headers()).thenReturn(headers);
        final jakarta.ws.rs.core.HttpHeaders proxied = new HttpRequestHeaders(request);

        assertTrue(proxied.getRequestHeader("name").isEmpty());
        headers.add("name", "value1");
        headers.add("name", "value2");
        assertEquals(2, proxied.getRequestHeader("name").size());
        assertEquals("value1", proxied.getRequestHeader("name").get(0));
        assertEquals("value2", proxied.getRequestHeader("name").get(1));

        assertNull(proxied.getHeaderString("name0"));
        assertEquals("value1,value2", proxied.getHeaderString("name"));

        MultivaluedMap<String, String> headersMap = proxied.getRequestHeaders();
        assertEquals(1, headersMap.keySet().size());
        assertEquals(2, headersMap.get("name").size());
        assertEquals("value1", headersMap.get("name").get(0));
        assertEquals("value2", headersMap.get("name").get(1));

        when(request.accepts()).thenReturn(Collections.emptyList());
        assertEquals(0, proxied.getAcceptableMediaTypes().size());

        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.TEXT_HTML);
        mediaTypes.add(MediaType.APPLICATION_JSON);
        when(request.accepts()).thenReturn(mediaTypes);
        List<jakarta.ws.rs.core.MediaType> acceptable = proxied.getAcceptableMediaTypes();
        assertEquals(2, acceptable.size());
        assertEquals(jakarta.ws.rs.core.MediaType.TEXT_HTML_TYPE, acceptable.get(0));
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE, acceptable.get(1));

        assertEquals(0, proxied.getAcceptableLanguages().size());
        headers.add(ACCEPT_LANGUAGE, "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        List<Locale> languages = proxied.getAcceptableLanguages();
        assertEquals(6, languages.size());

        when(request.contentType()).thenReturn(null);
        assertNull(proxied.getMediaType());
        when(request.contentType()).thenReturn(MediaType.ALL);
        assertEquals(jakarta.ws.rs.core.MediaType.WILDCARD_TYPE, proxied.getMediaType());

        assertNull(proxied.getLanguage());
        headers.add(jakarta.ws.rs.core.HttpHeaders.CONTENT_LANGUAGE, "zh-CN");
        assertEquals(Locale.SIMPLIFIED_CHINESE, proxied.getLanguage());

        assertEquals(0, proxied.getCookies().size());
        final Set<io.esastack.commons.net.http.Cookie> cookies = new HashSet<>();
        cookies.add(new CookieImpl("name1", "value1"));
        cookies.add(new CookieImpl("name2", "value2"));
        when(request.cookies()).thenReturn(cookies);
        Map<String, Cookie> cookieMap = proxied.getCookies();
        assertEquals(2, cookieMap.size());
        assertEquals("value1", cookieMap.get("name1").getValue());
        assertEquals("value2", cookieMap.get("name2").getValue());

        assertNull(proxied.getDate());
        final Date date = new Date();
        headers.add(jakarta.ws.rs.core.HttpHeaders.DATE, DateUtils.format(date.getTime()));
        assertNotNull(proxied.getDate());

        when(request.contentLength()).thenReturn(-1L);
        assertEquals(-1, proxied.getLength());
        when(request.contentLength()).thenReturn(100L);
        assertEquals(100, proxied.getLength());
    }

}

