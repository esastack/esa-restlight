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
package io.esastack.restlight.core.resolver;

import esa.commons.spi.SPI;
import io.esastack.commons.net.buffer.Buffer;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.HttpResponse;

/**
 * This resolver will serialize the {@link ResponseEntity} and write the serialized result to given {@link Buffer}.
 */
@SPI
public interface ResponseEntityResolver extends Ordered {

    /**
     * Writes the given {@code value} to {@link HttpResponse}.
     *
     * @param entity    entity
     * @param channel   the channel to write resolved {@code entity}
     * @param context   context
     * @return  resolved value, which must not be {@code null}.
     * @throws Exception    any exception
     */
    HandledValue<Void> writeTo(ResponseEntity entity,
                               ResponseEntityChannel channel,
                               RequestContext context) throws Exception;

    /**
     * Default to HIGHEST_PRECEDENCE.
     *
     * @return order
     */
    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}

