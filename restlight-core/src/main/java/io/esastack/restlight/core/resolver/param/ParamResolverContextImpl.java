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
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.RequestEntity;
import io.esastack.restlight.core.context.RequestEntityImpl;
import io.esastack.restlight.core.handler.method.Param;

public class ParamResolverContextImpl implements ParamResolverContext {

    private final RequestContext requestContext;
    private final RequestEntity entity;
    private final Param param;

    public ParamResolverContextImpl(RequestContext requestContext,
                                    Param param) {
        this(requestContext, new RequestEntityImpl(param, requestContext), param);
    }

    public ParamResolverContextImpl(RequestContext requestContext,
                                    RequestEntity entity,
                                    Param param) {
        Checks.checkNotNull(requestContext, "requestContext");
        Checks.checkNotNull(entity, "entity");
        Checks.checkNotNull(param, "param");
        this.requestContext = requestContext;
        this.entity = entity;
        this.param = param;
    }

    @Override
    public RequestEntity httpEntity() {
        return entity;
    }

    @Override
    public Param param() {
        return param;
    }

    @Override
    public RequestContext requestContext() {
        return requestContext;
    }
}
