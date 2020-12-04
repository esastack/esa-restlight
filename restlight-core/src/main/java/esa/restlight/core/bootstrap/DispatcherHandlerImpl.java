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
package esa.restlight.core.bootstrap;

import esa.commons.annotation.Internal;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.server.bootstrap.DefaultDispatcherHandler;
import esa.restlight.server.route.ReadOnlyRouteRegistry;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.internal.InternalThreadLocalMap;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

/**
 * Extension of {@link DefaultDispatcherHandler}, which checks the {@link ConstraintViolationException} before
 * sending an error.
 */
@Internal
public class DispatcherHandlerImpl extends DefaultDispatcherHandler {

    public DispatcherHandlerImpl(ReadOnlyRouteRegistry mapperRegistry) {
        super(mapperRegistry);
    }


    @Override
    protected void sendError(AsyncRequest request,
                             AsyncResponse response,
                             Throwable ex) {
        if (ex instanceof ConstraintViolationException) {
            //400 bad request

            ConstraintViolationException error = (ConstraintViolationException) ex;
            Set<ConstraintViolation<?>> cs = error.getConstraintViolations();
            if (cs == null || cs.isEmpty()) {
                sendErrorResult(request, response, error, HttpResponseStatus.BAD_REQUEST);
            } else {

                final StringBuilder sb = InternalThreadLocalMap.get().stringBuilder();
                for (ConstraintViolation c : cs) {
                    sb.append("{property='").append(c.getPropertyPath()).append('\'');
                    sb.append(",invalidValue='").append(c.getInvalidValue()).append('\'');
                    sb.append(",message='").append(c.getMessage()).append("\'}");
                }
                sb.append('}');

                sendErrorResult(request, response, sb.toString(), HttpResponseStatus.BAD_REQUEST);
            }
        } else {
            super.sendError(request, response, ex);
        }
    }
}
