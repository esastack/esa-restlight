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
package io.esastack.restlight.ext.validator;

import esa.commons.annotation.Internal;
import esa.commons.spi.Feature;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.spi.ExceptionHandler;
import io.esastack.restlight.core.spi.ExceptionHandlerFactory;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.server.bootstrap.ExceptionHandlerChain;
import io.esastack.restlight.server.util.Futures;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.internal.InternalThreadLocalMap;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static io.esastack.restlight.server.util.ErrorDetail.sendErrorResult;

@Internal
@Feature(tags = Constants.INTERNAL)
public class ValidationExceptionHandlerFactory implements ExceptionHandlerFactory {

    @Override
    public Optional<ExceptionHandler> handler(DeployContext<? extends RestlightOptions> ctx) {
        return Optional.of(new ValidationExceptionHandler());
    }

    private static class ValidationExceptionHandler implements ExceptionHandler {

        @Override
        public CompletableFuture<Void> handle(RequestContext context, Throwable th,
                                              ExceptionHandlerChain<RequestContext> next) {
            if (th instanceof ConstraintViolationException) {
                //400 bad request

                ConstraintViolationException error = (ConstraintViolationException) th;
                Set<ConstraintViolation<?>> cs = error.getConstraintViolations();
                if (cs == null || cs.isEmpty()) {
                    sendErrorResult(context.request(), context.response(), error,
                            HttpResponseStatus.BAD_REQUEST);
                } else {

                    final StringBuilder sb = InternalThreadLocalMap.get().stringBuilder();
                    for (ConstraintViolation<?> c : cs) {
                        sb.append("{property='").append(c.getPropertyPath()).append('\'');
                        sb.append(",invalidValue='").append(c.getInvalidValue()).append('\'');
                        sb.append(",message='").append(c.getMessage()).append("'}");
                    }
                    sb.append('}');

                    sendErrorResult(context.request(), context.response(), sb.toString(),
                            HttpResponseStatus.BAD_REQUEST);
                }
                return Futures.completedExceptionally(th);
            }

            return next.handle(context, th);
        }

        @Override
        public int getOrder() {
            return -100;
        }

    }
}

