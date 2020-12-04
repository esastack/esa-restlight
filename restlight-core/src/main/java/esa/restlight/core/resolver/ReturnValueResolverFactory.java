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
import esa.restlight.core.serialize.HttpResponseSerializer;
import esa.restlight.core.util.Ordered;

import java.util.List;

@SPI
public interface ReturnValueResolverFactory extends ReturnValueResolverPredicate, Ordered {

    /**
     * Converts given {@link ReturnValueResolverAdapter} to {@link ReturnValueResolverFactory} which
     * always use the given {@link ReturnValueResolverAdapter} as the result of
     * {@link #createResolver(InvocableMethod, List)}
     *
     * @param resolver resolver
     * @return of factory bean
     */
    static ReturnValueResolverFactory singleton(ReturnValueResolverAdapter resolver) {
        return new Singleton(resolver);
    }

    /**
     * Creates an instance of {@link ReturnValueResolverAdvice} for given handler method.
     *
     * @param method      method
     * @param serializers all the {@link HttpResponseSerializer}s in the context
     * @return resolver
     */
    ReturnValueResolver createResolver(InvocableMethod method, List<? extends HttpResponseSerializer> serializers);

    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    class Singleton implements ReturnValueResolverFactory {

        private final ReturnValueResolverAdapter resolver;

        Singleton(ReturnValueResolverAdapter resolver) {
            Checks.checkNotNull(resolver, "resolver");
            this.resolver = resolver;
        }

        @Override
        public ReturnValueResolver createResolver(InvocableMethod method,
                                                  List<? extends HttpResponseSerializer> serializers) {
            return resolver;
        }

        @Override
        public boolean supports(InvocableMethod invocableMethod) {
            return resolver.supports(invocableMethod);
        }

        @Override
        public int getOrder() {
            return resolver.getOrder();
        }
    }

}
