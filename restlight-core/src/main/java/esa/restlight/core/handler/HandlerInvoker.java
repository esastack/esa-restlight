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

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;

/**
 * Interface defines the invoking of handler.
 */
public interface HandlerInvoker {

    /**
     * Resolves the arguments from the given {@link AsyncRequest} and do the controller invocation by reflection. this
     * function won't do anything about the return value of the controller and the exception threw in the invocation.
     *
     * @param request  request
     * @param response response
     * @param args     provided args
     * @return future
     * @throws Throwable exception occurred
     */
    Object invoke(AsyncRequest request, AsyncResponse response, Object[] args) throws Throwable;

}
