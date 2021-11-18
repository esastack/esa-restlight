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
package io.esastack.restlight.core.spi.impl;

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.spi.Filter;
import io.esastack.restlight.core.spi.FilterFactory;

import java.util.Objects;
import java.util.Optional;

public class ResponseEntityWriterFilterFactory implements FilterFactory {

    @Override
    public Optional<Filter> filter(DeployContext<? extends RestlightOptions> ctx) {
        return Optional.of(new ResponseEntityWriterFilter(Objects.requireNonNull(ctx.resolverFactory().orElse(null))));
    }

}

