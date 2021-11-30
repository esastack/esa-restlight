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
package io.esastack.restlight.core.handler;

import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.handler.impl.HandlerContext;
import io.esastack.restlight.core.method.HandlerMethod;

/**
 * This {@link HandlerContexts} is used to save {@link HandlerContext} of given {@link HandlerMethod}.
 */
public interface HandlerContexts {

    /**
     * Adds {@link HandlerContext} of given {@link HandlerMethod}.
     *
     * @param handlerMethod handler method
     * @param context   context
     */
    void addContext(HandlerMethod handlerMethod, HandlerContext<? extends RestlightOptions> context);

    /**
     * Obtains {@link HandlerContext} of given {@link HandlerMethod}.
     *
     * @param handlerMethod handler method
     * @return  context of given {@link HandlerMethod}.
     */
    HandlerContext<? extends RestlightOptions> getContext(HandlerMethod handlerMethod);

}

