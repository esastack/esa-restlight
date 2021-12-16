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

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.ParamResolverAdvice;
import io.esastack.restlight.core.resolver.ParamResolverContextImpl;
import io.esastack.restlight.server.context.RequestContext;

import java.util.List;

class AdvisedParamResolver implements ResolverWrap {

    private final ParamResolver resolver;
    private final List<ParamResolverAdvice> advices;
    private final boolean absentAdvices;

    AdvisedParamResolver(ParamResolver resolver, List<ParamResolverAdvice> advices) {
        this.resolver = resolver;
        this.absentAdvices = (advices == null || advices.isEmpty());
        this.advices = advices;
    }

    @Override
    public Object resolve(DeployContext<? extends RestlightOptions> deployContext,
                          Param param, RequestContext context) throws Exception {
        if (absentAdvices) {
            return resolver.resolve(param, context);
        }
        return new ParamResolverContextImpl(context, param, resolver, advices).proceed();
    }
}

