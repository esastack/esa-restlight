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

import esa.commons.Checks;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.HandledValue;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.core.serialize.Serializers;
import io.esastack.restlight.core.util.FutureUtils;
import io.esastack.restlight.server.context.RequestContext;

import java.util.Collections;
import java.util.List;

public class FixedResponseEntityResolver extends AbstractResponseEntityResolver {

    private final boolean simpleType;
    private final HttpResponseSerializer serializer;

    public FixedResponseEntityResolver(HandlerMethod method, HttpResponseSerializer serializer) {
        super(true);
        Checks.checkNotNull(method, "method");
        Checks.checkNotNull(serializer, "serializer");
        this.simpleType = Object.class.equals(method.method().getReturnType())
                || Object.class.equals(FutureUtils.retrieveFirstGenericTypeOfFutureReturnType(method.method()));
        this.serializer = serializer;
    }

    @Override
    protected byte[] serialize(ResponseEntity entity,
                               List<MediaType> mediaTypes,
                               RequestContext context) throws Exception {
        HandledValue<byte[]> handled = Serializers.serializeBySerializer(serializer, entity);
        if (handled.isSuccess()) {
            return handled.value();
        } else {
            throw new IllegalStateException("Could not resolve the return value(type=" + entity.type().getName()
                    + ") by specified HttpResponseSerializer: " + serializer.getClass().getName());
        }
    }

    @Override
    protected boolean isSimpleType(ResponseEntity entity) {
        return simpleType;
    }

    @Override
    protected List<MediaType> getMediaTypes(RequestContext context) {
        return Collections.emptyList();
    }

}

