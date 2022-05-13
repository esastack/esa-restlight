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
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.RequestEntity;
import io.esastack.restlight.core.context.RequestEntityImpl;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.AdvisedResolverContext;
import io.esastack.restlight.core.resolver.Resolver;
import io.esastack.restlight.core.resolver.entity.EntityResolverExecutor;

import java.util.List;

public class AdvisedRequestEntityResolver implements Resolver<AdvisedResolverContext> {

    private final Param param;
    private final RequestEntityResolver[] resolvers;
    private final RequestEntityResolverAdvice[] advices;

    public AdvisedRequestEntityResolver(Param param,
                                        List<RequestEntityResolver> resolvers,
                                        List<RequestEntityResolverAdvice> advices) {
        Checks.checkNotEmptyArg(resolvers, "resolvers");
        this.param = param;
        this.resolvers = resolvers.toArray(new RequestEntityResolver[0]);
        this.advices = (advices == null ? null : advices.toArray(new RequestEntityResolverAdvice[0]));
    }

    @Override
    public Object resolve(AdvisedResolverContext context) throws Exception {
        RequestContext requestContext = context.requestContext();
        RequestEntity entity = new RequestEntityImpl(param, requestContext);
        RequestEntityResolverContext resolverContext = new RequestEntityResolverContextImpl(requestContext, entity);
        String unSupportMsg = "There is no suitable resolver to resolve param: " + param
                + ", content-type: " + requestContext.request().contentType();
        return new EntityResolverExecutor(resolverContext, resolvers, advices, unSupportMsg).proceed();
    }
}
