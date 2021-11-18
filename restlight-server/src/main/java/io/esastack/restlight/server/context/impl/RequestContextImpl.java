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
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.httpserver.core.HttpResponse;
import io.esastack.httpserver.core.RequestContext;
import io.esastack.httpserver.impl.AttributesProxy;

public class RequestContextImpl extends AttributesProxy implements RequestContext {

    private final HttpRequest request;
    private final HttpResponse response;

    public RequestContextImpl(HttpRequest request, HttpResponse response) {
        super(request);
        Checks.checkNotNull(request, "request");
        Checks.checkNotNull(response, "response");
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
}

