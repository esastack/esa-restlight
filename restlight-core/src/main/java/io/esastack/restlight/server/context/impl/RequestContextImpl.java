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
import esa.commons.collection.AttributeKey;
import esa.commons.collection.AttributeMap;
import esa.commons.collection.Attributes;
import io.esastack.restlight.server.bootstrap.ResponseContent;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.core.impl.HttpResponseImpl;
import io.esastack.restlight.server.mock.MockHttpResponse;

import java.util.function.Consumer;

public class RequestContextImpl implements RequestContext {

    public static final AttributeKey<ResponseContent> RESPONSE_CONTENT = AttributeKey.valueOf("$response.content");

    private final Attributes attributes;
    private final HttpRequest request;
    private final HttpResponse response;

    public RequestContextImpl(HttpRequest request, HttpResponse response) {
        this(new AttributeMap(16), request, response);
    }

    public RequestContextImpl(Attributes attributes, HttpRequest request, HttpResponse response) {
        Checks.checkNotNull(attributes, "attributes");
        Checks.checkNotNull(request, "request");
        Checks.checkNotNull(response, "response");
        this.attributes = attributes;
        this.request = request;
        this.response = response;
    }

    @Override
    public HttpRequest request() {
        return request;
    }

    @Override
    public HttpResponse response() {
        return response;
    }

    @Override
    public Attributes attrs() {
        return attributes;
    }

    @Override
    public RequestContext onEnd(Consumer<RequestContext> listener) {
        if (response instanceof HttpResponseImpl) {
            HttpResponseImpl httpResponse = (HttpResponseImpl) response;
            httpResponse.onEnd(listener, this);
            return this;
        }

        if (response instanceof MockHttpResponse) {
            MockHttpResponse httpResponse = (MockHttpResponse) response;
            httpResponse.onEnd(listener, this);
            return this;
        }
        return this;
    }
}

