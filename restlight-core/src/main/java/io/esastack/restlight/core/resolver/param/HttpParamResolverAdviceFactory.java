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
package io.esastack.restlight.core.resolver.param;

import esa.commons.Checks;
import esa.commons.spi.SPI;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.util.Ordered;

@SPI
public interface HttpParamResolverAdviceFactory extends HttpParamPredicate, Ordered {

    /**
     * Converts given {@link HttpParamResolverAdviceAdapter} to {@link HttpParamResolverAdviceFactory} which
     * always use the given {@link HttpParamResolverAdviceAdapter} as the result of
     * {@link #createResolverAdvice(Param, HttpParamResolver)}
     *
     * @param resolver resolver
     * @return of factory bean
     */
    static HttpParamResolverAdviceFactory singleton(HttpParamResolverAdviceAdapter resolver) {
        return new Singleton(resolver);
    }

    /**
     * Creates an instance of {@link HttpParamResolverAdvice} for given handler method.
     *
     * @param param    method
     * @param resolver argumentResolver associated with this parameter
     * @return advice
     */
    HttpParamResolverAdvice createResolverAdvice(Param param, HttpParamResolver resolver);

    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    class Singleton implements HttpParamResolverAdviceFactory {

        private final HttpParamResolverAdviceAdapter resolver;

        Singleton(HttpParamResolverAdviceAdapter resolver) {
            Checks.checkNotNull(resolver, "resolver");
            this.resolver = resolver;
        }

        @Override
        public HttpParamResolverAdvice createResolverAdvice(Param param,
                                                            HttpParamResolver httpParamResolver) {
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
