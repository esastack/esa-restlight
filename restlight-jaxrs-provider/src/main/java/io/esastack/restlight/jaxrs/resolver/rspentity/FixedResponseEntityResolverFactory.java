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
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.resolver.ResponseEntityResolver;
import io.esastack.restlight.core.resolver.ResponseEntityResolverFactory;
import io.esastack.restlight.core.resolver.rspentity.FixedResponseEntityResolver;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;

import java.util.List;

public class FixedResponseEntityResolverFactory implements ResponseEntityResolverFactory {

    @Override
    public ResponseEntityResolver createResolver(HandlerMethod method,
                                                 List<? extends HttpResponseSerializer> serializers) {
        return new FixedResponseEntityResolver0(serializers);
    }

    @Override
    public boolean supports(HandlerMethod method) {
        return method != null;
    }

    @Override
    public int getOrder() {
        return 210;
    }

    private static class FixedResponseEntityResolver0 extends FixedResponseEntityResolver {

        private FixedResponseEntityResolver0(List<? extends HttpResponseSerializer> serializers) {
            super(serializers);
        }

        @Override
        protected boolean supports(ResponseEntity entity) {
            return entity.handler().isPresent();
        }

    }

}

