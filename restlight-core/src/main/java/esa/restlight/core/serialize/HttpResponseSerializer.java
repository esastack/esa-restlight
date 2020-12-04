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
package esa.restlight.core.serialize;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.util.MediaType;

import java.lang.reflect.Type;

public interface HttpResponseSerializer extends BaseHttpSerializer, TxSerializer {

    /**
     * Is current instance supports the media type
     *
     * @param mediaType media type
     * @param type      type of return value
     *
     * @return supports
     */
    boolean supportsWrite(MediaType mediaType, Type type);

    /**
     * Customizer for response
     *
     * @param request     request
     * @param response    response
     * @param returnValue return value
     *
     * @return object
     */
    Object customResponse(AsyncRequest request, AsyncResponse response, Object returnValue);

}
