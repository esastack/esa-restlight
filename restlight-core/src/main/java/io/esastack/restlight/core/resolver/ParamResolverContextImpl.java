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
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;

import java.util.List;

public class ParamResolverContextImpl implements ParamResolverContext {

    private final RequestContext context;
    private final Param param;
    private final ParamResolver resolver;
    private final List<ParamResolverAdvice> advices;
    private int index;

    public ParamResolverContextImpl(RequestContext context,
                                    Param param,
                                    ParamResolver resolver,
                                    List<ParamResolverAdvice> advices) {
        Checks.checkNotNull(context, "context");
        Checks.checkNotNull(param, "param");
        Checks.checkNotNull(resolver, "resolver");
        this.context = context;
        this.param = param;
        this.resolver = resolver;
        this.advices = advices;
    }

    @Override
    public RequestContext context() {
        return context;
    }

    @Override
    public Param param() {
        return param;
    }

    @Override
    public Object proceed() throws Exception {
        if (advices == null || index >= advices.size()) {
            return resolver.resolve(param, context);
        }

        return advices.get(index++).aroundResolve(this);
    }
}
