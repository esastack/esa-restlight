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
package io.esastack.restlight.core.resolver;

import esa.commons.Checks;
import esa.commons.spi.SPI;
import io.esastack.restlight.core.util.Ordered;

@SPI
public interface ResponseEntityResolverAdviceFactory extends ResponseEntityPredicate, Ordered {

    /**
     * Converts given {@link ResponseEntityResolverAdviceAdapter} to {@link ResponseEntityResolverAdviceFactory} which
     * always use the given {@link ResponseEntityResolverAdviceAdapter} as the result of
     * {@link #createResolverAdvice(ResponseEntity)}
     *
     * @param resolver resolver
     * @return of factory bean
     */
    static ResponseEntityResolverAdviceFactory singleton(ResponseEntityResolverAdviceAdapter resolver) {
        return new Singleton(resolver);
    }

    /**
     * Creates an instance of {@link ResponseEntityResolverAdvice} for given handler method.
     *
     * @param entity response entity
     * @return advice
     */
    ResponseEntityResolverAdvice createResolverAdvice(ResponseEntity entity);

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    class Singleton implements ResponseEntityResolverAdviceFactory {

        private final ResponseEntityResolverAdviceAdapter resolver;

        Singleton(ResponseEntityResolverAdviceAdapter resolver) {
            Checks.checkNotNull(resolver, "resolver");
            this.resolver = resolver;
        }

        @Override
        public ResponseEntityResolverAdvice createResolverAdvice(ResponseEntity entity) {
            return this.resolver;
        }

        @Override
        public boolean supports(ResponseEntity entity) {
            return resolver.supports(entity);
        }

        @Override
        public int getOrder() {
            return resolver.getOrder();
        }
    }
}
