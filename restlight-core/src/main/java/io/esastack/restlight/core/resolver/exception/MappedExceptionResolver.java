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
package io.esastack.restlight.core.resolver.exception;

import esa.commons.Checks;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import io.esastack.restlight.core.resolver.ExceptionResolver;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.util.Futures;

import java.util.concurrent.CompletionStage;

public class MappedExceptionResolver implements ExceptionResolver<Throwable> {

    private static final Logger logger = LoggerFactory.getLogger(MappedExceptionResolver.class);

    private final ExceptionMapper mapper;

    public MappedExceptionResolver(ExceptionMapper mapper) {
        Checks.checkNotNull(mapper, "mapper");
        this.mapper = mapper;
    }

    @Override
    public CompletionStage<Void> handleException(RequestContext context, Throwable ex) {
        if (ex == null) {
            return Futures.completedFuture();
        }
        Class<? extends Throwable> type = ex.getClass();
        ExceptionResolver<Throwable> resolver = mapper.mapTo(type);
        if (resolver == null) {
            logger.debug("None HandlerMethod is found to handle exception", ex);
            // just throw it, cause we can not resolve
            return Futures.completedExceptionally(ex);
        }
        return resolver.handleException(context, ex);
    }

}
