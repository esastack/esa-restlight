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
package io.esastack.restlight.jaxrs.spi;

import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdvice;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdviceFactory;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.jaxrs.adapter.JaxrsResponseAdapter;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.Response;

/**
 * This factory is designed for converting {@link GenericEntity}, {@link Response}, {@link Response.ResponseBuilder}
 * purpose.
 */
public class JaxrsResponseAdapterFactory implements ResponseEntityResolverAdviceFactory {

    private static final ResponseEntityResolverAdvice SINGLETON = new JaxrsResponseAdapter();

    @Override
    public ResponseEntityResolverAdvice createResolverAdvice(HandlerMethod method) {
        return SINGLETON;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}

