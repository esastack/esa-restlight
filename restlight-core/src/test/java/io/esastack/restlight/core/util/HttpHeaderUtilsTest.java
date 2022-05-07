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
package io.esastack.restlight.core.util;

import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static io.esastack.commons.net.http.HttpHeaderNames.ACCEPT_ENCODING;
import static io.esastack.commons.net.http.HttpHeaderNames.ACCEPT_LANGUAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpHeaderUtilsTest {

    @Test
    void testGetAcceptEncodings() {
        assertTrue(HttpHeaderUtils.getAcceptEncodings(null).isEmpty());
        final HttpHeaders headers = new Http1HeadersImpl();
        assertTrue(HttpHeaderUtils.getAcceptEncodings(headers).isEmpty());

        headers.add(ACCEPT_ENCODING, "gzip;q=1.0");
        headers.add(ACCEPT_ENCODING, "*;q=0.5,compress");
        headers.add(ACCEPT_ENCODING, "deflate;,");

        final List<String> encodings = HttpHeaderUtils.getAcceptEncodings(headers);
        assertEquals("gzip", encodings.get(0));
        assertEquals("*", encodings.get(1));
        assertEquals("compress", encodings.get(2));
        assertEquals("deflate", encodings.get(3));
    }

    @Test
    void testGetAcceptLanguages() {
        assertTrue(HttpHeaderUtils.getAcceptLanguages(null).isEmpty());
        final HttpHeaders headers = new Http1HeadersImpl();
        assertTrue(HttpHeaderUtils.getAcceptLanguages(headers).isEmpty());

        headers.add(ACCEPT_LANGUAGE, "de");
        headers.add(ACCEPT_LANGUAGE, "de-CH");
        headers.add(ACCEPT_LANGUAGE, "en-US,en;q=0.5");
        headers.add(ACCEPT_LANGUAGE, "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5");

        final List<Locale> languages = HttpHeaderUtils.getAcceptLanguages(headers);
        assertEquals(8, languages.size());
        assertEquals("de", languages.get(0).getLanguage());
        assertEquals("de", languages.get(1).getLanguage());
        assertEquals("CH", languages.get(1).getCountry());
        assertEquals("en", languages.get(2).getLanguage());
        assertEquals("US", languages.get(2).getCountry());
        assertEquals("en", languages.get(3).getLanguage());
        assertEquals("zh", languages.get(4).getLanguage());
        assertEquals("CN", languages.get(4).getCountry());
        assertEquals("zh", languages.get(5).getLanguage());
        assertEquals("zh", languages.get(6).getLanguage());
        assertEquals("TW", languages.get(6).getCountry());
        assertEquals("zh", languages.get(7).getLanguage());
        assertEquals("HK", languages.get(7).getCountry());
    }

    @Test
    void testGetLanguage() {
        assertNull(HttpHeaderUtils.getLanguage(null));
        final HttpHeaders headers = new Http1HeadersImpl();
        assertNull(HttpHeaderUtils.getLanguage(headers));
        headers.add(HttpHeaderNames.CONTENT_LANGUAGE, "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5");
        final Locale locale0 = HttpHeaderUtils.getLanguage(headers);
        assertEquals("zh", locale0.getLanguage());
        assertEquals("CN", locale0.getCountry());

        headers.set(HttpHeaderNames.CONTENT_LANGUAGE, "zh-TW;1=0.7");
        final Locale locale1 = HttpHeaderUtils.getLanguage(headers);
        assertEquals("zh", locale1.getLanguage());
        assertEquals("TW", locale1.getCountry());
    }

    @Test
    void testParseToEncoding() {
        assertNull(HttpHeaderUtils.parseToEncoding(null));
        assertNull(HttpHeaderUtils.parseToEncoding(""));
        assertEquals("deflate", HttpHeaderUtils.parseToEncoding("deflate"));
        assertEquals("gzip", HttpHeaderUtils.parseToEncoding("gzip; q=1.0"));
    }

    @Test
    void testParseToLanguage() {
        assertNull(HttpHeaderUtils.parseToLanguage(null));
        final Locale locale0 = HttpHeaderUtils.parseToLanguage("zh-CN ");
        assertNotNull(locale0);
        assertEquals("zh", locale0.getLanguage());
        assertEquals("CN", locale0.getCountry());

        final Locale locale1 = HttpHeaderUtils.parseToLanguage("zh; q=0.8");
        assertNotNull(locale1);
        assertEquals("zh", locale1.getLanguage());

        final Locale locale2 = HttpHeaderUtils.parseToLanguage(" zh-TW;q=0.7");
        assertNotNull(locale2);
        assertEquals("zh", locale2.getLanguage());
        assertEquals("TW", locale2.getCountry());
    }

    @Test
    void testConcatHeaderValues() {
        assertNull(HttpHeaderUtils.concatHeaderValues(null));
        assertNull(HttpHeaderUtils.concatHeaderValues(new LinkedList<>()));
        assertEquals("abc", HttpHeaderUtils.concatHeaderValues(Collections.singletonList("abc")));
        final List<String> values = new LinkedList<>();
        values.add("a");
        values.add("b");
        values.add("v");
        assertEquals("a,b,v", HttpHeaderUtils.concatHeaderValues(values));
    }

}

