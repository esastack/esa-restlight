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
package io.esastack.restlight.core.locator;

import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.util.Ordered;

import java.util.Optional;

/**
 * This is used to get an {@link Optional} instance of {@link HandlerValueResolver} from given target {@link
 * HandlerMapping}.
 */
public interface HandlerValueResolverLocator extends Ordered {

    /**
     * Obtains an optional {@link HandlerValueResolver} for given {@link HandlerMapping} if possible.
     *
     * @param mapping mapping
     * @return an optional instance of {@link HandlerValueResolver}, which must not be {@code null}.
     */
    Optional<HandlerValueResolver> getHandlerValueResolver(HandlerMapping mapping);

}

