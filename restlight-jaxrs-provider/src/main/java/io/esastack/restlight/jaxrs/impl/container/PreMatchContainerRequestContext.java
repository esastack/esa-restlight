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

import esa.commons.Checks;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.restlight.core.filter.FilterContext;

import java.io.InputStream;
import java.net.URI;

public class PreMatchContainerRequestContext extends AbstractContainerRequestContext {

    public PreMatchContainerRequestContext(FilterContext context) {
        super(context);
    }

    @Override
    public void setRequestUri(URI requestUri) {
        ((FilterContext) context).request().uri(requestUri.toString());
    }

    @Override
    public void setRequestUri(URI baseUri, URI requestUri) {
        uriInfo.baseUri(baseUri);
        ((FilterContext) context).request().uri(requestUri.toString());
    }

    @Override
    public void setMethod(String method) {
        Checks.checkNotNull(method, "method");
        ((FilterContext) context).request().method(HttpMethod.valueOf(method.toUpperCase()));
    }

    @Override
    public void setEntityStream(InputStream input) {
        Checks.checkNotNull(input, "input");
        ((FilterContext) context).request().inputStream(input);
    }
}

