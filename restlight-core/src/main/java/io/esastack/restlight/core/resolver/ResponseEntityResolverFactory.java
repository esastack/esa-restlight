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
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.core.util.Ordered;

import java.util.List;

@SPI
public interface ResponseEntityResolverFactory extends Ordered {

    /**
     * Converts given {@link ResponseEntityResolver} to {@link ResponseEntityResolverFactory} which
     * always use the given {@link ResponseEntityResolver} as the result of
     * {@link #createResolver(List)}
     *
     * @param resolver resolver
     * @return of factory bean
     */
    static ResponseEntityResolverFactory singleton(ResponseEntityResolver resolver) {
        return new ResponseEntityResolverFactory.Singleton(resolver);
    }

    /**
     * Creates an instance of {@link ResponseEntityResolverAdvice} for given handler method.
     *
     * @param serializers all the {@link HttpResponseSerializer}s in the context
     * @return resolver
     */
    ResponseEntityResolver createResolver(List<? extends HttpResponseSerializer> serializers);

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    class Singleton implements ResponseEntityResolverFactory {

        private final ResponseEntityResolver resolver;

        Singleton(ResponseEntityResolver resolver) {
            Checks.checkNotNull(resolver, "resolver");
            this.resolver = resolver;
        }

        @Override
        public ResponseEntityResolver createResolver(List<? extends HttpResponseSerializer> serializers) {
            return resolver;
        }

        @Override
        public int getOrder() {
            return resolver.getOrder();
        }
    }
}
