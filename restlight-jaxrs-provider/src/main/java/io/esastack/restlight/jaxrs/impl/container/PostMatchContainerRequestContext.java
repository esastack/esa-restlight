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

import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.spi.impl.RouteTracking;
import io.esastack.restlight.core.context.RequestContext;

import java.io.InputStream;
import java.net.URI;

public class PostMatchContainerRequestContext extends AbstractContainerRequestContext {

    private static final IllegalStateException ILLEGAL_STATE_AFTER_MATCHING = new IllegalStateException(
            "This operation is not allowed after matching request, maybe @PreMatching is missed?");
    private final HandlerMethod handler;

    public PostMatchContainerRequestContext(RequestContext context) {
        super(context);
        this.handler = RouteTracking.matchedMethod(context);
    }

    @Override
    public void setRequestUri(URI requestUri) {
        throw ILLEGAL_STATE_AFTER_MATCHING;
    }

    @Override
    public void setRequestUri(URI baseUri, URI requestUri) {
        throw ILLEGAL_STATE_AFTER_MATCHING;
    }

    @Override
    public void setMethod(String method) {
        throw ILLEGAL_STATE_AFTER_MATCHING;
    }

    @Override
    public void setEntityStream(InputStream input) {
        throw ILLEGAL_STATE_AFTER_MATCHING;
    }

    public HandlerMethod handler() {
        return handler;
    }
}

