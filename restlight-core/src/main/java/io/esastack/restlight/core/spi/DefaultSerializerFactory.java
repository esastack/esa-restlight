/*
 * Copyright 2022 OPPO ESA Stack Project
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
package io.esastack.restlight.core.spi;

import esa.commons.annotation.Internal;
import esa.commons.spi.SPI;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;

/**
 * DefaultSerializerFactory is used to configure the default serializer which will be used if there's no serializer
 * been set in the context.
 */
@SPI
@Internal
public interface DefaultSerializerFactory {

    /**
     * Returns the default {@link HttpRequestSerializer}.
     *
     * @param ctx deploy context
     * @return request serializer
     */
    HttpRequestSerializer defaultRequestSerializer(DeployContext ctx);

    /**
     * Returns the default {@link HttpResponseSerializer}.
     *
     * @param ctx deploy context
     * @return response serializer
     */
    HttpResponseSerializer defaultResponseSerializer(DeployContext ctx);

}
