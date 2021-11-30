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
package io.esastack.restlight.core.spi.impl;

import esa.commons.annotation.Internal;
import esa.commons.spi.Feature;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.handler.FutureTransfer;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.spi.FutureTransferFactory;
import io.esastack.restlight.core.util.Constants;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Internal
@Feature(tags = Constants.INTERNAL)
public class CompletableFutureTransferFactory implements FutureTransferFactory {

    @Override
    public Optional<FutureTransfer> futureTransfer(HandlerMethod method) {
        if (CompletableFuture.class.isAssignableFrom(method.method().getReturnType())) {
            return Optional.of(CompletableFutureTransfer.SINGLETON);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public int getOrder() {
        return 100;
    }

    private static class CompletableFutureTransfer implements FutureTransfer {

        private static final FutureTransfer SINGLETON = new CompletableFutureTransfer();

        private CompletableFutureTransfer() {
        }

        @SuppressWarnings("unchecked")
        @Override
        public CompletableFuture<Object> transferTo(RequestContext context, Object value) {
            return (CompletableFuture<Object>) value;
        }
    }

}

