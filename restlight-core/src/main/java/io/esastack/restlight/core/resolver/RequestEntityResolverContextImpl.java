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
package io.esastack.restlight.core.resolver;

import esa.commons.Checks;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.server.context.RequestContext;

public class RequestEntityResolverContextImpl implements RequestEntityResolverContext {

    private final Param param;
    private final RequestContext context;
    private final RequestEntity entity;
    private final RequestEntityResolver[] resolvers;
    private final RequestEntityResolverAdvice[] advices;
    private final int advicesSize;
    private int index;

    public RequestEntityResolverContextImpl(Param param,
                                            RequestContext context,
                                            RequestEntity entity,
                                            RequestEntityResolver[] resolvers,
                                            RequestEntityResolverAdvice[] advices) {
        Checks.checkNotNull(param, "param");
        Checks.checkNotNull(context, "context");
        Checks.checkNotNull(entity, "entity");
        Checks.checkNotNull(resolvers, "resolvers");
        this.param = param;
        this.context = context;
        this.entity = entity;
        this.resolvers = resolvers;
        this.advices = advices;
        this.advicesSize = (advices == null ? 0 : advices.length);
    }

    @Override
    public RequestContext context() {
        return context;
    }

    @Override
    public RequestEntity httpEntity() {
        return entity;
    }

    @Override
    public Object proceed() throws Exception {
        if (advices == null || index >= advicesSize) {
            HandledValue<Object> handled;
            for (RequestEntityResolver resolver : resolvers) {
                handled = resolver.readFrom(param, entity, context);
                if (handled.isSuccess()) {
                    return handled.value();
                }
            }
            return null;
        }

        return advices[index++].aroundRead(this);
    }

}

