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
package io.esastack.restlight.core.resolver.entity.request;

import esa.commons.Checks;
import esa.commons.spi.SPI;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.param.ParamPredicate;
import io.esastack.restlight.core.util.Ordered;

@SPI
public interface RequestEntityResolverAdviceFactory extends ParamPredicate, Ordered {

    /**
     * Converts given {@link RequestEntityResolverAdviceAdapter} to {@link RequestEntityResolverAdviceFactory} which
     * always use the given {@link RequestEntityResolverAdviceAdapter} as the result of
     * {@link #createResolverAdvice(Param)}
     *
     * @param resolver resolver
     * @return of factory bean
     */
    static RequestEntityResolverAdviceFactory singleton(RequestEntityResolverAdviceAdapter resolver) {
        return new RequestEntityResolverAdviceFactory.Singleton(resolver);
    }

    /**
     * Creates an instance of {@link RequestEntityResolverAdvice} for given handler method.
     *
     * @param param param
     * @return advice
     */
    RequestEntityResolverAdvice createResolverAdvice(Param param);

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    class Singleton implements RequestEntityResolverAdviceFactory {

        private final RequestEntityResolverAdviceAdapter resolver;

        Singleton(RequestEntityResolverAdviceAdapter resolver) {
            Checks.checkNotNull(resolver, "resolver");
            this.resolver = resolver;
        }

        @Override
        public RequestEntityResolverAdvice createResolverAdvice(Param param) {
            return this.resolver;
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

