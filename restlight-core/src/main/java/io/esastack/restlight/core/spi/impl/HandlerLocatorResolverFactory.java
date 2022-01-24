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
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.handler.RouterRegistries;
import io.esastack.restlight.core.handler.impl.HandlerLocatorResolver;
import io.esastack.restlight.core.handler.locate.HandlerValueResolverLocator;
import io.esastack.restlight.core.spi.HandlerValueResolverLocatorFactory;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.server.route.Router;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Internal
@Feature(tags = Constants.INTERNAL)
public class HandlerLocatorResolverFactory implements HandlerValueResolverLocatorFactory {

    @Override
    public HandlerValueResolverLocator resolver(DeployContext ctx) {
        return new HandlerLocatorResolverLocator(ctx);
    }

    private static class HandlerLocatorResolverLocator implements HandlerValueResolverLocator {

        private final DeployContext context;
        private final RouterRegistries registries = new RouterRegistries() {

            private final ConcurrentHashMap<Class<?>, Router> registries = new ConcurrentHashMap<>(8);

            @Override
            public Router getOrCompute(Class<?> clazz, Function<Class<?>, Router> factory) {
                return registries.computeIfAbsent(clazz, factory);
            }
        };

        private HandlerLocatorResolverLocator(DeployContext context) {
            this.context = context;
        }

        @Override
        public Optional<HandlerValueResolver> getHandlerValueResolver(HandlerMapping mapping) {
            if (mapping.methodInfo().isLocator()) {
                return Optional.of(new HandlerLocatorResolver(context, mapping, registries));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public int getOrder() {
            return -100;
        }
    }
}

