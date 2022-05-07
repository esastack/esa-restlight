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
import io.esastack.restlight.core.handler.FutureTransfer;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.util.Ordered;

import java.util.Optional;

/**
 * This factory is used to create {@link FutureTransfer} for given {@link HandlerMethod}.
 */
@Internal
@SPI
@FunctionalInterface
public interface FutureTransferFactory extends Ordered {

    /**
     * Creates an optional instance of {@link FutureTransfer}.
     *
     * @param method handler method
     * @return an optional instance of {@link FutureTransfer}.
     */
    Optional<FutureTransfer> futureTransfer(HandlerMethod method);

}

