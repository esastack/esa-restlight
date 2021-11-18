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
package io.esastack.restlight.jaxrs.spi;

import esa.commons.annotation.Internal;
import esa.commons.spi.Feature;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.handler.FutureTransfer;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.MethodParam;
import io.esastack.restlight.core.spi.FutureTransferFactory;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.jaxrs.impl.JaxrsContextUtils;
import io.esastack.restlight.server.util.Futures;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Internal
@Feature(tags = Constants.INTERNAL)
public class AsyncResponseTransferFactory implements FutureTransferFactory {

    @Override
    public Optional<FutureTransfer> futureTransfer(HandlerMethod method) {
        for (MethodParam param : method.parameters()) {
            if (param.hasAnnotation(Suspended.class) && param.type().equals(AsyncResponse.class)) {
                return Optional.of(AsyncResponseTransfer.SINGLETON);
            }
        }
        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private static class AsyncResponseTransfer implements FutureTransfer {

        private static final FutureTransfer SINGLETON = new AsyncResponseTransfer();

        private AsyncResponseTransfer() {
        }

        @Override
        public CompletableFuture<Object> transferTo(RequestContext context, Object value) {
            CompletableFuture<Object> asyncResponse = JaxrsContextUtils.getAsyncResponse(context);
            if (asyncResponse == null) {
                return Futures.completedFuture(value);
            } else {
                return asyncResponse;
            }
        }
    }
}

