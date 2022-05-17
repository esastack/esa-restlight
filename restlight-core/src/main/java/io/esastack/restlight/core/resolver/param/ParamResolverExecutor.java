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
import esa.commons.Result;
import io.esastack.restlight.core.exception.WebServerException;
import io.esastack.restlight.core.resolver.ResolverExecutor;

final class ParamResolverExecutor implements ResolverExecutor<ParamResolverContext> {

    private final ParamResolverContext context;
    private final ParamResolver[] resolvers;
    private final ParamResolverAdvice[] advices;
    private int index;

    public ParamResolverExecutor(ParamResolverContext context,
                                 ParamResolver[] resolvers,
                                 ParamResolverAdvice[] advices) {
        Checks.checkNotNull(context, "context");
        Checks.checkNotNull(resolvers, "resolvers");
        Checks.checkNotNull(advices, "advices");
        this.context = context;
        this.resolvers = resolvers;
        this.advices = advices;
    }

    @Override
    public ParamResolverContext context() {
        return context;
    }

    @Override
    public Object proceed() throws Exception {
        if (advices == null || index >= advices.length) {
            for (ParamResolver resolver : resolvers) {
                Object result = resolver.resolve(context);
                if (result instanceof Result) {
                    Result handled = (Result) result;
                    if (handled.isOk()) {
                        return handled.get();
                    }
                    continue;
                }
                return result;
            }
            // todo add message
            throw WebServerException.notSupported("");
        }

        return advices[index++].aroundResolve(this);
    }
}
