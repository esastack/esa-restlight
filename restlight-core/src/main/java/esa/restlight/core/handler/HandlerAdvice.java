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
package esa.restlight.core.handler;

import esa.commons.annotation.Internal;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.util.Ordered;

/**
 * The class is designed to execute some additional operations around method invoking. Eg: validate parameters pre
 * invoking or validate return value post invoking.
 */
@Internal
public interface HandlerAdvice extends Ordered {

    /**
     * Performs some additional operations around invoking
     *
     * @param request  AsyncRequest
     * @param response AsyncResponse
     * @param args     args of target method
     * @param invoker  HandlerInvoker
     * @return object
     * @throws Throwable exception occurred
     */
    Object invoke(AsyncRequest request,
                  AsyncResponse response,
                  Object[] args,
                  HandlerInvoker invoker) throws Throwable;
}
