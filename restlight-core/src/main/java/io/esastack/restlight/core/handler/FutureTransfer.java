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

import esa.commons.annotation.Internal;
import io.esastack.restlight.server.context.RequestContext;

import java.util.concurrent.CompletionStage;

/**
 * The {@link FutureTransfer} is designed to transfer the given value to {@link CompletionStage} format.
 */
@Internal
@FunctionalInterface
public interface FutureTransfer {

    /**
     * Transfer the given {@code value} to {@link CompletionStage}.
     *
     * @param context current context
     * @param value   value
     * @return transferred value
     */
    CompletionStage<Object> transferTo(RequestContext context, Object value);

}

