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
import io.esastack.restlight.core.handler.Handlers;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.converter.StringConverterProvider;
import io.esastack.restlight.core.resolver.factory.HandlerResolverFactory;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;

import java.util.List;

public class HandlersParamResolver implements ParamResolverFactory {

    private final DeployContext deployContext;

    public HandlersParamResolver(DeployContext deployContext) {
        Checks.checkNotNull(deployContext, "deployContext");
        this.deployContext = deployContext;
    }

    @Override
    public boolean supports(Param param) {
        return Handlers.class.equals(param.type());
    }

    @Override
    public ParamResolver<ParamResolverContext> createResolver(Param param,
                                                              StringConverterProvider converters,
                                                              List<? extends HttpRequestSerializer> serializers,
                                                              HandlerResolverFactory resolverFactory) {
        return (ctx) -> deployContext.handlers().orElse(null);
    }

}

