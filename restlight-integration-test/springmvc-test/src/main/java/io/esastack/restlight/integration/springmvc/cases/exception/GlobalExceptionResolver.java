/*
 * Copyright 2022 OPPO ESA Stack Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.esastack.restlight.integration.springmvc.cases.exception;

import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restlight.core.resolver.ExceptionResolver;
import io.esastack.restlight.server.context.RequestContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component
public class GlobalExceptionResolver implements ExceptionResolver<IllegalArgumentException> {
    @Override
    public CompletionStage<Void> handleException(RequestContext context, IllegalArgumentException exception) {
        context.response().status(HttpStatus.FORBIDDEN.code());
        context.response().entity(exception.getMessage());
        return CompletableFuture.completedFuture(null);
    }
}
