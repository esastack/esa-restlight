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
package io.esastack.restlight.server.context.impl;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.collection.LinkedMultiValueMap;
import esa.commons.collection.MultiValueMap;
import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpserver.core.HttpInputStream;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.httpserver.impl.HttpInputStreamImpl;
import io.esastack.httpserver.impl.HttpRequestProxy;
import io.esastack.restlight.server.context.FilteringRequest;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class FilteringRequestImpl extends HttpRequestProxy implements FilteringRequest {

    private HttpMethod method;
    private String strUri;
    private URI uri;
    private Buffer body;
    private InputStream ins;
    private Map<String, List<String>> params;

    public FilteringRequestImpl(HttpRequest underlying) {
        super(underlying);
    }

    @Override
    public void method(HttpMethod method) {
        Checks.checkNotNull(method, "method");
        this.method = method;
    }

    @Override
    public HttpMethod method() {
        if (method != null) {
            return method;
        } else {
            return super.method();
        }
    }

    @Override
    public void uri(String uri) {
        Checks.checkNotNull(uri, "uri");
        this.strUri = uri;
        this.uri = URI.create(uri);
    }

    @Override
    public String uri() {
        if (strUri != null) {
            return strUri;
        } else {
            return super.uri();
        }
    }

    @Override
    public String path() {
        if (uri == null) {
            return super.path();
        } else {
            return uri.getRawPath();
        }
    }

    @Override
    public String query() {
        if (uri == null) {
            return super.query();
        } else {
            return uri.getRawQuery();
        }
    }

    @Override
    public void body(byte[] body) {
        this.body = Buffer.defaultAlloc().buffer(body);
    }

    @Override
    public Buffer body() {
        if (body == null) {
            return super.body();
        } else {
            return body;
        }
    }

    @Override
    public void body(Buffer body) {
        Checks.checkNotNull(body, "body");
        this.body = body;
    }

    @Override
    public void inputStream(InputStream ins) {
        Checks.checkNotNull(ins, "ins");
        this.ins = ins;
    }

    @Override
    public HttpInputStream inputStream() {
        if (ins == null) {
            return super.inputStream();
        } else {
            return new HttpInputStreamImpl(ins);
        }
    }

    @Override
    public Map<String, List<String>> paramsMap() {
        if (strUri == null) {
            return super.paramsMap();
        }
        if (params == null) {
            final MultiValueMap<String, String> params0 = new LinkedMultiValueMap<>();
            final String query = uri.getQuery();
            String[] subQueries = query.split("&");
            for (String subQuery : subQueries) {
                String[] pairs = subQuery.split("=");
                params0.add(pairs[0], pairs.length == 2 ? pairs[1] : null);
            }
            addFormUriEncodedParams(params0);
            this.params = params0;
        }
        return params;
    }

    private void addFormUriEncodedParams(MultiValueMap<String, String> params) {
        if (HttpMethod.POST.equals(method()) && body().readableBytes() > 0) {
            String contentType = headers().get(HttpHeaderNames.CONTENT_TYPE);
            if (contentType != null
                    && contentType.length() >= MediaType.APPLICATION_FORM_URLENCODED_VALUE.length()
                    && contentType.charAt(0) == MediaType.APPLICATION_FORM_URLENCODED_VALUE.charAt(0)) {
                MediaType mediaType = contentType();

                if (mediaType != null && mediaType.isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)) {
                    Charset charset = mediaType.charset();
                    if (charset == null) {
                        charset = StandardCharsets.UTF_8;
                    }
                    String body = body().string(charset);
                    if (StringUtils.isEmpty(body)) {
                        return;
                    }

                    String[] navs = body.split("&");
                    for (String nav : navs) {
                        String[] pairs = nav.split("=");
                        params.add(pairs[0], pairs.length == 2 ? pairs[1] : null);
                    }
                }
            }
        }
    }
}

