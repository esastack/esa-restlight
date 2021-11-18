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

import esa.commons.annotation.Internal;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.util.Ordered;

/**
 * The class is designed to execute some additional operations around method invoking. Eg: validate parameters pre
 * invoking or validate return value post invoking.
 *
 * @deprecated use {@link HandlerAdvice} please
 */
@Deprecated
@Internal
public interface MethodAdvice extends Ordered {

    /**
     * Execute some additional operations before invoking
     *
     * @param context  current request context
     * @param args     args
     * @return true if allow next step, otherwise false
     */
    default boolean preInvoke(RequestContext context, Object[] args) {
        return true;
    }

    /**
     * Execute some additional operations after invoking
     *
     * @param context  current request context
     * @param result   the result of method invocation
     * @return default return result
     */
    default Object postInvoke(RequestContext context, Object result) {
        return result;
    }

}
