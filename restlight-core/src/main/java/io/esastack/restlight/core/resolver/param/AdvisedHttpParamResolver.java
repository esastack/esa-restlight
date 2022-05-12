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

import io.esastack.restlight.core.resolver.Resolver;
import io.esastack.restlight.core.resolver.ResolverContext;

import java.util.List;

public class AdvisedHttpParamResolver implements Resolver<ResolverContext> {

    private final ParamResolver resolver;
    private final List<ParamResolverAdvice> advices;
    private final boolean absentAdvices;

    public AdvisedHttpParamResolver(ParamResolver resolver, List<ParamResolverAdvice> advices) {
        this.resolver = resolver;
        this.absentAdvices = (advices == null || advices.isEmpty());
        this.advices = advices;
    }

    @Override
    public Object resolve(ResolverContext context) throws Exception {
        ParamResolverContext resolverContext = new ParamResolverContextImpl(context);
        if (absentAdvices) {
            return resolver.resolve(resolverContext);
        }
        return new ParamResolverExecutor(resolverContext, resolver, advices).proceed();
    }
}

