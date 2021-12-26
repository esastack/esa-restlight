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
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.resolver.ResponseEntityResolver;
import io.esastack.restlight.core.resolver.ResponseEntityResolverFactory;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.core.serialize.Serializers;
import io.esastack.restlight.core.util.ResponseEntityUtils;
import io.esastack.restlight.server.context.RequestContext;

import java.util.List;

public class CharSequenceEntityResolverFactory implements ResponseEntityResolverFactory {

    @Override
    public ResponseEntityResolver createResolver(List<? extends HttpResponseSerializer> serializers) {
        return new CharSequenceResolver();
    }

    @Override
    public int getOrder() {
        return 100;
    }

    /**
     * Implementation for resolving response entity type of {@link CharSequence}
     */
    private static class CharSequenceResolver extends AbstractResponseEntityResolver {

        private CharSequenceResolver() {
            super(false);
        }

        @Override
        protected boolean supports(ResponseEntity entity) {
            return ResponseEntityUtils.isAssignableFrom(entity, CharSequence.class);
        }

        @Override
        protected byte[] serialize(ResponseEntity entity,
                                   List<MediaType> mediaTypes,
                                   RequestContext context) throws Exception {
            return Serializers.serializeCharSequence(((CharSequence) context.response().entity()),
                    context.response(),
                    selectMediaType(mediaTypes));
        }

    }
}

