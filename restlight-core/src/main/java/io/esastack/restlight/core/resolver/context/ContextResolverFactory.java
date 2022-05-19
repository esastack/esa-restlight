/*
 * Copyright 2022 OPPO ESA Stack Project
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
package io.esastack.restlight.core.resolver.context;

import esa.commons.Checks;
import esa.commons.spi.SPI;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.param.ParamPredicate;
import io.esastack.restlight.core.resolver.param.ParamResolverAdvice;
import io.esastack.restlight.core.util.Ordered;

@SPI
public interface ContextResolverFactory extends ParamPredicate, Ordered {

    /**
     * Converts given {@link ContextResolverAdapter} to {@link ContextResolverFactory} which
     * always use the given {@link ContextResolverAdapter} as the result of
     * {@link #createResolver(Param)}
     *
     * @param resolver resolver
     * @return of factory bean
     */
    static ContextResolverFactory singleton(ContextResolverAdapter resolver) {
        return new ContextResolverFactory.Singleton(resolver);
    }

    /**
     * Creates an instance of {@link ParamResolverAdvice} for given {@code param}.
     *
     * @param param param
     * @return advice
     */
    ContextResolver createResolver(Param param);

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    class Singleton implements ContextResolverFactory {

        private final ContextResolverAdapter resolver;

        Singleton(ContextResolverAdapter resolver) {
            Checks.checkNotNull(resolver, "resolver");
            this.resolver = resolver;
        }

        @Override
        public ContextResolver createResolver(Param param) {
            return resolver;
        }

        @Override
        public boolean supports(Param param) {
            return resolver.supports(param);
        }

        @Override
        public int getOrder() {
            return resolver.getOrder();
        }
    }

}

