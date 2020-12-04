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
import esa.restlight.core.util.Ordered;

@SPI
public interface ArgumentResolverAdviceFactory extends ArgumentResolverPredicate, Ordered {

    /**
     * Converts given {@link ArgumentResolverAdviceAdapter} to {@link ArgumentResolverAdviceFactory} which
     * always use the given {@link ArgumentResolverAdviceAdapter} as the result of
     * {@link #createResolverAdvice(Param, ArgumentResolver)}
     *
     * @param resolver resolver
     * @return of factory bean
     */
    static ArgumentResolverAdviceFactory singleton(ArgumentResolverAdviceAdapter resolver) {
        return new Singleton(resolver);
    }

    /**
     * Creates an instance of {@link ArgumentResolverAdvice} for given handler method.
     *
     * @param param method
     * @param resolver  argumentResolver associated with this parameter
     * @return advice
     */
    ArgumentResolverAdvice createResolverAdvice(Param param, ArgumentResolver resolver);

    /**
     * Default to HIGHEST_PRECEDENCE.
     *
     * @return order
     */
    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    class Singleton implements ArgumentResolverAdviceFactory {

        private final ArgumentResolverAdviceAdapter resolver;

        Singleton(ArgumentResolverAdviceAdapter resolver) {
            Checks.checkNotNull(resolver, "resolver");
            this.resolver = resolver;
        }

        @Override
        public ArgumentResolverAdvice createResolverAdvice(Param param,
                                                           ArgumentResolver argumentResolver) {
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
