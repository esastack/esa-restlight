/*
 * Copyright 2020 OPPO ESA Stack Project
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

import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.server.context.RequestContext;

import java.lang.reflect.Method;

/**
 * A {@link Handler} defines abstract of an bean {@link Method} which probably will be expressed as
 * the return value of {@link #handlerMethod()}, and the {@link Handler} could be invoke by calling {@link
 * #invoke(RequestContext, Object[])} method.
 */
public interface Handler extends HandlerInvoker {

    /**
     * Gets the handler method.
     *
     * @return handler
     */
    HandlerMethod handlerMethod();

    /**
     * Obtains the handler object.
     *
     * @return  object
     */
    Object bean();
}
