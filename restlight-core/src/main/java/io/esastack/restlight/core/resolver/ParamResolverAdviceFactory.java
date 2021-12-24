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
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.util.Ordered;

@SPI
public interface ParamResolverAdviceFactory extends ParamPredicate, Ordered {

    /**
     * Converts given {@link ParamResolverAdviceAdapter} to {@link ParamResolverAdviceFactory} which
     * always use the given {@link ParamResolverAdviceAdapter} as the result of
     * {@link #createResolverAdvice(Param, ParamResolver)}
     *
     * @param resolver resolver
     * @return of factory bean
     */
    static ParamResolverAdviceFactory singleton(ParamResolverAdviceAdapter resolver) {
        return new Singleton(resolver);
    }

    /**
     * Creates an instance of {@link ParamResolverAdvice} for given handler method.
     *
     * @param param    method
     * @param resolver argumentResolver associated with this parameter
     * @return advice
     */
    ParamResolverAdvice createResolverAdvice(Param param, ParamResolver resolver);

    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    class Singleton implements ParamResolverAdviceFactory {

        private final ParamResolverAdviceAdapter resolver;

        Singleton(ParamResolverAdviceAdapter resolver) {
            Checks.checkNotNull(resolver, "resolver");
            this.resolver = resolver;
        }

        @Override
        public ParamResolverAdvice createResolverAdvice(Param param,
                                                        ParamResolver paramResolver) {
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
