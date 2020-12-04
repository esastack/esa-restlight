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
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.util.Ordered;

@SPI
public interface ReturnValueResolverAdviceFactory extends ReturnValueResolverPredicate, Ordered {

    /**
     * Converts given {@link ReturnValueResolverAdviceAdapter} to {@link ReturnValueResolverAdviceFactory} which
     * always use the given {@link ReturnValueResolverAdviceAdapter} as the result of
     * {@link #createResolverAdvice(InvocableMethod, ReturnValueResolver)}
     *
     * @param resolver resolver
     * @return of factory bean
     */
    static ReturnValueResolverAdviceFactory singleton(ReturnValueResolverAdviceAdapter resolver) {
        return new Singleton(resolver);
    }

    /**
     * Creates an instance of {@link ReturnValueResolverAdvice} for given handler method.
     *
     * @param method              method
     * @param resolver returnValueResolver associated with this handler method
     * @return advice
     */
    ReturnValueResolverAdvice createResolverAdvice(InvocableMethod method,
                                                   ReturnValueResolver resolver);

    /**
     * Default to HIGHEST_PRECEDENCE.
     *
     * @return order
     */
    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    class Singleton implements ReturnValueResolverAdviceFactory {

        private final ReturnValueResolverAdviceAdapter resolver;

        Singleton(ReturnValueResolverAdviceAdapter resolver) {
            Checks.checkNotNull(resolver, "resolver");
            this.resolver = resolver;
        }

        @Override
        public ReturnValueResolverAdvice createResolverAdvice(InvocableMethod method,
                                                              ReturnValueResolver returnValueResolver) {
            return resolver;
        }

        @Override
        public boolean supports(InvocableMethod parameter) {
            return resolver.supports(parameter);
        }

        @Override
        public int getOrder() {
            return resolver.getOrder();
        }
    }
}
