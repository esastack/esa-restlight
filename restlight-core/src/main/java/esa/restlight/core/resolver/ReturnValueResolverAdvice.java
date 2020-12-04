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
package esa.restlight.core.resolver;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;

/**
 * Allows customizing the return value of handler before resolving it to byte array.
 */
public interface ReturnValueResolverAdvice {

    /**
     * This method will be called before {@link ReturnValueResolver#resolve(Object, AsyncRequest, AsyncResponse)}, and
     * then the value of this method will be passed to {@link ReturnValueResolver#resolve(Object, AsyncRequest,
     * AsyncResponse)} as the first parameter.
     * <p>
     * Note: We could not guarantee that it will be resolved correctly in {@link ReturnValueResolver#resolve(Object,
     * AsyncRequest, AsyncResponse)} method if return value type is different from the passed returnValue parameter,
     * which dependents on the implementation of the {@link ReturnValueResolver}
     *
     * @param returnValue return value of handler
     * @param request     request
     * @param response    response
     *
     * @return return value that was passed or a modified(possibly new) instance
     */
    Object beforeResolve(Object returnValue, AsyncRequest request, AsyncResponse response);

}
