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
import esa.commons.Result;
import io.esastack.restlight.core.exception.WebServerException;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.ResolverExecutor;

public class RequestEntityResolverExecutor implements ResolverExecutor<RequestEntityResolverContext> {

    private final Param param;
    private final RequestEntityResolver[] resolvers;
    private final RequestEntityResolverAdvice[] advices;
    private final int advicesSize;
    private final RequestEntityResolverContext context;
    private int index;

    public RequestEntityResolverExecutor(Param param, RequestEntityResolver[] resolvers,
                                         RequestEntityResolverAdvice[] advices,
                                         RequestEntityResolverContext context) {
        Checks.checkNotNull(context, "context");
        Checks.checkNotNull(resolvers, "resolvers");
        Checks.checkNotNull(param, "param");
        this.param = param;
        this.resolvers = resolvers;
        this.advices = advices;
        this.context = context;
        this.advicesSize = (advices == null ? 0 : advices.length);
    }

    @Override
    public RequestEntityResolverContext context() {
        return context;
    }

    @Override
    public Object proceed() throws Exception {
        if (advices == null || index >= advicesSize) {
            Result<?, ?> handled;
            for (RequestEntityResolver resolver : resolvers) {
                handled = resolver.resolve(context);
                if (handled.isOk()) {
                    return handled.get();
                }
            }
            throw WebServerException.notSupported("There is no suitable resolver to resolve param: " + param
                    + ", content-type: " + context.requestContext().request().contentType());
        }

        return advices[index++].aroundResolve(this);
    }
}
