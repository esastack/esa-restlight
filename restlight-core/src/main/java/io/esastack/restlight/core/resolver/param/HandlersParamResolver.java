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
package io.esastack.restlight.core.resolver.param;

import esa.commons.Checks;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.configure.Handlers;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;

import java.util.List;

public class HandlersParamResolver implements ParamResolverFactory {

    private final DeployContext<? extends RestlightOptions> deployContext;

    public HandlersParamResolver(DeployContext<? extends RestlightOptions> deployContext) {
        Checks.checkNotNull(deployContext, "deployContext");
        this.deployContext = deployContext;
    }

    @Override
    public boolean supports(Param param) {
        return Handlers.class.equals(param.type());
    }

    @Override
    public ParamResolver createResolver(Param param, List<? extends HttpRequestSerializer> serializers) {
        return (p, ctx) -> deployContext.handlers().orElse(null);
    }

}

