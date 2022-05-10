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
package io.esastack.restlight.core.handler;

import io.esastack.restlight.core.context.RequestContext;

/**
 * Interface defines the invoking of handler.
 */
public interface HandlerInvoker {

    /**
     * Resolves the arguments from the given {@link RequestContext} and do the controller invocation by reflection. this
     * function won't do anything about the return value of the controller and the exception threw in the invocation.
     *
     * @param context request context
     * @param args    provided args
     * @return future
     * @throws Throwable exception occurred
     */
    Object invoke(RequestContext context, Object[] args) throws Throwable;

}
