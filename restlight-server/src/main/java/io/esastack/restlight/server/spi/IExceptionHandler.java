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
package io.esastack.restlight.server.spi;

import esa.commons.annotation.Internal;
import esa.commons.spi.SPI;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.server.bootstrap.ExceptionHandlerChain;
import io.esastack.restlight.server.context.RequestContext;

import java.util.concurrent.CompletableFuture;

/**
 * This {@link IExceptionHandler} is designed for internal using. The exception caught will be proceed by
 * sorted {@link IExceptionHandler}s one by one.
 */
@SPI
@Internal
@FunctionalInterface
public interface IExceptionHandler extends Ordered {

    /**
     * Handles the exception by given {@code context} and next {@link ExceptionHandlerChain}.
     *
     * @param context   context
     * @param th        th
     * @param next      next handler chain
     * @return          handled result
     */
    CompletableFuture<Void> handle(RequestContext context, Throwable th, ExceptionHandlerChain next);

}

