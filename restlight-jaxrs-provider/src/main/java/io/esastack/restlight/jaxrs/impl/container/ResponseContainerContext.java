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

import java.io.InputStream;
import java.net.URI;

public class ResponseContainerContext extends DelegatingContainerRequestContext {

    private static final IllegalStateException ILLEGAL_STATE_IN_RESPONSE = new IllegalStateException("This operation" +
            "is not allowed during container response filter.");

    public ResponseContainerContext(AbstractContainerRequestContext underlying) {
        super(underlying);
    }

    @Override
    public void abortWith(Response response) {
        throw ILLEGAL_STATE_IN_RESPONSE;
    }

    @Override
    public void setRequestUri(URI requestUri) {
        throw ILLEGAL_STATE_IN_RESPONSE;
    }

    @Override
    public void setRequestUri(URI baseUri, URI requestUri) {
        throw ILLEGAL_STATE_IN_RESPONSE;
    }

    @Override
    public void setMethod(String method) {
        throw ILLEGAL_STATE_IN_RESPONSE;
    }

    @Override
    public void setEntityStream(InputStream input) {
        throw ILLEGAL_STATE_IN_RESPONSE;
    }
}

