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

package io.esastack.restlight.core.resolver.param.entity;

import esa.commons.Checks;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.context.RequestEntity;
import io.esastack.restlight.core.context.RequestEntityImpl;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.converter.StringConverterProvider;
import io.esastack.restlight.core.resolver.factory.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.param.ParamResolver;
import io.esastack.restlight.core.resolver.param.ParamResolverAdapter;
import io.esastack.restlight.core.resolver.param.ParamResolverContext;
import io.esastack.restlight.core.resolver.param.ParamResolverFactory;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.spi.ParamResolverProvider;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * {@link AdvisedRequestEntityResolverProvider} wraps the {@link RequestEntityResolver} as a {@link ParamResolver}.
 * the {@link RequestEntityResolver} executes logic mainly work in {@link RequestEntityResolverExecutor}.
 */
public final class AdvisedRequestEntityResolverProvider implements ParamResolverProvider {

    @Override
    public Optional<ParamResolverFactory> factoryBean(DeployContext ctx) {
        return Optional.of(new AdvisedRequestEntityResolverFactory(ctx));
    }

    private static class AdvisedRequestEntityResolverFactory implements ParamResolverFactory {

        private final DeployContext deployContext;

        private AdvisedRequestEntityResolverFactory(DeployContext deployContext) {
            this.deployContext = deployContext;
        }

        @Override
        public ParamResolver<ParamResolverContext> createResolver(Param param, StringConverterProvider converters,
                                                                  List<? extends HttpRequestSerializer> serializers,
                                                                  HandlerResolverFactory resolverFactory) {
            List<RequestEntityResolver> resolvers = resolverFactory.getRequestEntityResolvers(param);
            if (resolvers == null || resolvers.isEmpty()) {
                return null;
            }
            List<RequestEntityResolverAdvice> advices = Optional
                    .ofNullable(resolverFactory.getRequestEntityResolverAdvices(param))
                    .orElse(Collections.emptyList());
            return new AdvisedRequestEntityResolverResolver(param, resolvers.toArray(new RequestEntityResolver[0]),
                    advices.toArray(new RequestEntityResolverAdvice[0]));
        }

        @Override
        public boolean supports(Param param) {
            return true;
        }

        @Override
        public int getOrder() {
            return LOWEST_PRECEDENCE;
        }
    }

    private static class AdvisedRequestEntityResolverResolver implements ParamResolverAdapter {

        private final Param param;
        private final RequestEntityResolver[] resolvers;
        private final RequestEntityResolverAdvice[] advices;

        private AdvisedRequestEntityResolverResolver(Param param, RequestEntityResolver[] resolvers,
                                                     RequestEntityResolverAdvice[] advices) {
            Checks.checkNotNull(param, "param");
            this.param = param;
            this.resolvers = resolvers;
            this.advices = advices;
        }


        @Override
        public boolean supports(Param param) {
            return true;
        }

        @Override
        public Object resolve(ParamResolverContext context) throws Exception {
            RequestEntity requestEntity = new RequestEntityImpl(param, context.requestContext());
            RequestEntityResolverContext resolverContext =
                    new RequestEntityResolverContextImpl(requestEntity, context.requestContext());
            return new RequestEntityResolverExecutor(param, resolvers, advices, resolverContext).proceed();
        }

        @Override
        public int getOrder() {
            return LOWEST_PRECEDENCE;
        }
    }
}
