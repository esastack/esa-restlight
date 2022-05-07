/*
 * Copyright 2021 OPPO ESA Stack Project
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
package io.esastack.restlight.core.handler.impl;

import esa.commons.Checks;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.context.RequestEntity;
import io.esastack.restlight.core.context.RequestEntityImpl;
import io.esastack.restlight.core.resolver.reqentity.RequestEntityResolver;
import io.esastack.restlight.core.resolver.reqentity.RequestEntityResolverAdvice;
import io.esastack.restlight.core.resolver.reqentity.RequestEntityResolverContextImpl;
import io.esastack.restlight.core.context.RequestContext;

import java.util.List;

class AdvisedRequestEntityResolver implements ResolverWrap {

    private final Param param;
    private final RequestEntityResolver[] resolvers;
    private final RequestEntityResolverAdvice[] advices;

    AdvisedRequestEntityResolver(Param param,
                                 List<RequestEntityResolver> resolvers,
                                 List<RequestEntityResolverAdvice> advices) {
        Checks.checkNotEmptyArg(resolvers, "resolvers");
        this.param = param;
        this.resolvers = resolvers.toArray(new RequestEntityResolver[0]);
        this.advices = (advices == null ? null : advices.toArray(new RequestEntityResolverAdvice[0]));
    }

    @Override
    public Object resolve(DeployContext deployContext, RequestContext context) throws Exception {
        RequestEntity entity = new RequestEntityImpl(param, context);
        return new RequestEntityResolverContextImpl(this.param, context, entity, resolvers, advices).proceed();
    }
}
