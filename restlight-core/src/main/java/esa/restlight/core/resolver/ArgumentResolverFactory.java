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
package esa.restlight.core.resolver;

import esa.commons.Checks;
import esa.commons.spi.SPI;
import esa.restlight.core.method.Param;
import esa.restlight.core.serialize.HttpRequestSerializer;
import esa.restlight.core.util.Ordered;

import java.util.List;

@SPI
public interface ArgumentResolverFactory extends ArgumentResolverPredicate, Ordered {

    /**
     * Converts given {@link ArgumentResolverAdapter} to {@link ArgumentResolverFactory} which
     * always use the given {@link ArgumentResolverAdapter} as the result of
     * {@link #createResolver(Param, List)}
     *
     * @param resolver resolver
     * @return of factory bean
     */
    static ArgumentResolverFactory singleton(ArgumentResolverAdapter resolver) {
        return new Singleton(resolver);
    }

    /**
     * Creates a instance of {@link ArgumentResolver} for given handler method.
     *
     * @param param   method
     * @param serializers all the {@link HttpRequestSerializer}s of current context
     * @return resolver
     */
    ArgumentResolver createResolver(Param param,
                                    List<? extends HttpRequestSerializer> serializers);

    /**
     * Default to HIGHEST_PRECEDENCE.
     *
     * @return order
     */
    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    class Singleton implements ArgumentResolverFactory {

        private final ArgumentResolverAdapter resolver;

        Singleton(ArgumentResolverAdapter resolver) {
            Checks.checkNotNull(resolver, "resolver");
            this.resolver = resolver;
        }

        @Override
        public ArgumentResolver createResolver(Param parameter,
                                               List<? extends HttpRequestSerializer> serializers) {
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
