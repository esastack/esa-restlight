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
package io.esastack.restlight.jaxrs.resolver.rspentity;

import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.ResponseEntityResolver;
import io.esastack.restlight.core.resolver.ResponseEntityResolverFactory;
import io.esastack.restlight.core.resolver.rspentity.NegotiationResponseResolver;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;

import java.util.List;

public class NegotiationResponseResolverFactory implements ResponseEntityResolverFactory {

    private final String paramName;

    public NegotiationResponseResolverFactory(String paramName) {
        this.paramName = paramName;
    }

    @Override
    public ResponseEntityResolver createResolver(HandlerMethod method,
                                                 List<? extends HttpResponseSerializer> serializers) {
        return new NegotiationResponseResolver(paramName, serializers);
    }

    @Override
    public boolean supports(HandlerMethod method) {
        return true;
    }

    @Override
    public boolean alsoApplyWhenMissingHandler() {
        return true;
    }

    @Override
    public int getOrder() {
        return 310;
    }

}

