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
package io.esastack.restlight.core.resolver.rspentity;

import io.esastack.commons.net.http.MediaType;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.resolver.ResponseEntityResolver;
import io.esastack.restlight.core.resolver.ResponseEntityResolverFactory;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.core.serialize.Serializers;
import io.esastack.restlight.core.util.ResponseEntityUtils;

import java.util.List;

public class PrimitiveEntityResolverFactory implements ResponseEntityResolverFactory {

    @Override
    public ResponseEntityResolver createResolver(List<? extends HttpResponseSerializer> serializers) {
        return new PrimitiveResolver();
    }

    /**
     * Implementation for resolving response entity type of Primitives
     */
    private static class PrimitiveResolver extends AbstractResponseEntityResolver {

        private PrimitiveResolver() {
            super(false);
        }

        @Override
        protected boolean supports(ResponseEntity entity) {
            return ResponseEntityUtils.isPrimitiveOrWrapperType(entity);
        }

        @Override
        protected byte[] serialize(ResponseEntity entity,
                                   List<MediaType> mediaTypes,
                                   HttpRequest request) throws Exception {
            return Serializers.serializePrimitives(entity.entity(),
                    entity.response(),
                    selectMediaType(mediaTypes));
        }

        @Override
        public int getOrder() {
            return 130;
        }
    }
}

