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

import esa.commons.Checks;
import io.esastack.restlight.core.util.HttpHeaderUtils;
import io.esastack.restlight.jaxrs.util.MediaTypeUtils;
import io.esastack.restlight.server.core.HttpRequest;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HttpRequestHeaders implements HttpHeaders {

    private final HttpRequest request;
    private final ModifiableMultivaluedMap headers;

    public HttpRequestHeaders(HttpRequest request) {
        Checks.checkNotNull(request, "request");
        this.request = request;
        this.headers = new ModifiableMultivaluedMap(request.headers());
    }

    @Override
    public List<String> getRequestHeader(String name) {
        return headers.get(name);
    }

    @Override
    public String getHeaderString(String name) {
        return HttpHeaderUtils.concatHeaderValues(getRequestHeader(name));
    }

    @Override
    public MultivaluedMap<String, String> getRequestHeaders() {
        return headers;
    }

    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        List<io.esastack.commons.net.http.MediaType> mediaTypes = request.accepts();
        List<MediaType> values = new ArrayList<>(mediaTypes.size());
        for (io.esastack.commons.net.http.MediaType mediaType : mediaTypes) {
            values.add(MediaTypeUtils.convert(mediaType));
        }
        return values;
    }

    @Override
    public List<Locale> getAcceptableLanguages() {
        return HttpHeaderUtils.getAcceptLanguages(request.headers());
    }

    @Override
    public MediaType getMediaType() {
        return MediaTypeUtils.convert(request.contentType());
    }

    @Override
    public Locale getLanguage() {
        String language = headers.getFirst(HttpHeaders.CONTENT_LANGUAGE);
        return HttpHeaderUtils.parseToLanguage(language);
    }

    @Override
    public Map<String, Cookie> getCookies() {
        List<String> cookieValues = headers.get(HttpHeaders.SET_COOKIE);
        if (cookieValues == null || cookieValues.isEmpty()) {
            return Collections.emptyMap();
        } else {
            Map<String, Cookie> cookies = new HashMap<>();
            for (Object item : cookieValues) {
                NewCookie newCookie = RuntimeDelegate.getInstance()
                        .createHeaderDelegate(NewCookie.class)
                        .fromString(item.toString());
                cookies.put(newCookie.getName(), newCookie);
            }
            return cookies;
        }
    }

    @Override
    public Date getDate() {
        String data = headers.getFirst(HttpHeaders.DATE);
        return io.esastack.restlight.server.util.DateUtils.parseByCache(data);
    }

    @Override
    public int getLength() {
        return (int) request.contentLength();
    }
}

