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
package io.esastack.restlight.server.bootstrap;

import esa.commons.annotation.Internal;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.route.ExceptionHandler;
import io.esastack.restlight.server.route.RouteFailureException;

import java.util.concurrent.CompletionStage;

/**
 * This {@link IExceptionHandler} is designed for internal using. The exception caught will be proceed by
 * sorted {@link IExceptionHandler}s one by one. Generally speaking, we expect that all exception should
 * be handled by matched custom {@link ExceptionHandler}s, but there is no guarantee that each exception
 * has a matching {@link ExceptionHandler}(eg. route hasn't matched, absent exception handlers), in this
 * case, we still want to handle some exception internally, such as {@link RouteFailureException}. So we
 * design this {@link IExceptionHandler} to handle exception one by one and in most case, the last one
 * of the {@link ExceptionHandlerChain} is matched {@link ExceptionHandler}.
 *
 * @see ExceptionHandlerChain
 */
@Internal
@FunctionalInterface
public interface IExceptionHandler extends Ordered {

    /**
     * Handles the exception by given {@code context} and next {@link ExceptionHandlerChain}.
     *
     * @param context context
     * @param th      th
     * @param next    next handler chain
     * @return handled result
     */
    CompletionStage<Void> handle(RequestContext context, Throwable th, ExceptionHandlerChain next);

}

