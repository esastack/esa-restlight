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
package esa.restlight.springmvc.bootstrap;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.server.bootstrap.DefaultDispatcherExceptionHandler;
import esa.restlight.springmvc.util.ResponseStatusUtils;
import io.netty.handler.codec.http.HttpResponseStatus;

public class SpringMvcDispatcherExceptionHandler extends DefaultDispatcherExceptionHandler {

    @Override
    protected HttpResponseStatus toCustomStatus(AsyncRequest request, AsyncResponse response, Throwable throwable) {
        return ResponseStatusUtils.getCustomResponse(throwable);
    }
}

