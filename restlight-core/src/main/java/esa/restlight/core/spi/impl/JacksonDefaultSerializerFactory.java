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
package esa.restlight.core.spi.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import esa.commons.annotation.Internal;
import esa.commons.spi.Feature;
import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.serialize.HttpBodySerializer;
import esa.restlight.core.serialize.HttpRequestSerializer;
import esa.restlight.core.serialize.HttpResponseSerializer;
import esa.restlight.core.serialize.JacksonHttpBodySerializer;
import esa.restlight.core.spi.DefaultSerializerFactory;
import esa.restlight.core.util.Constants;

@Internal
@Feature(tags = Constants.INTERNAL)
public class JacksonDefaultSerializerFactory implements DefaultSerializerFactory {

    @Internal
    public static final String OBJECT_MAPPER = "$object_mapper";

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
                final Object objectMapper = ctx.attribute(OBJECT_MAPPER);
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
