/*
 * Copyright 2020 OPPO ESA Stack Project
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
package io.esastack.restlight.core.spi.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import esa.commons.annotation.Internal;
import esa.commons.collection.AttributeKey;
import esa.commons.spi.Feature;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.serialize.HttpBodySerializer;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.core.serialize.JacksonHttpBodySerializer;
import io.esastack.restlight.core.spi.DefaultSerializerFactory;
import io.esastack.restlight.core.util.Constants;

@Internal
@Feature(tags = Constants.INTERNAL)
public class JacksonDefaultSerializerFactory implements DefaultSerializerFactory {

    @Internal
    public static final AttributeKey<Object> OBJECT_MAPPER = AttributeKey.valueOf("$object_mapper");

    private volatile HttpBodySerializer jackson;

    @Override
    public HttpRequestSerializer defaultRequestSerializer(DeployContext<? extends RestlightOptions> ctx) {
        return getInstance(ctx);
    }

    @Override
    public HttpResponseSerializer defaultResponseSerializer(DeployContext<? extends RestlightOptions> ctx) {
        return getInstance(ctx);
    }

    private HttpBodySerializer getInstance(DeployContext<? extends RestlightOptions> ctx) {
        if (jackson == null) {
            synchronized (this) {
                final Object objectMapper = ctx.attrs().attr(OBJECT_MAPPER).get();
                if (objectMapper instanceof ObjectMapper) {
                    jackson = new JacksonHttpBodySerializer((ObjectMapper) objectMapper);
                    return jackson;
                } else {
                    jackson = new JacksonHttpBodySerializer();
                }
            }
        }
        return jackson;
    }
}
