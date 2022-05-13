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
package io.esastack.restlight.jaxrs.resolver.param;

import esa.commons.Checks;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.converter.StringConverterProvider;
import io.esastack.restlight.core.resolver.param.ParamResolver;
import io.esastack.restlight.core.resolver.param.ParamResolverContext;
import io.esastack.restlight.core.resolver.param.ParamResolverFactory;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.jaxrs.impl.container.ResourceContextImpl;
import io.esastack.restlight.jaxrs.util.JaxrsUtils;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;

import java.util.List;

public class ResourceContextParamResolver implements ParamResolverFactory {

    private final DeployContext context;

    public ResourceContextParamResolver(DeployContext context) {
        Checks.checkNotNull(context, "context");
        this.context = context;
    }

    @Override
    public boolean supports(Param param) {
        return JaxrsUtils.hasAnnotation(param, Context.class) && ResourceContext.class.equals(param.type());
    }

    @Override
    public ParamResolver createResolver(Param param,
                                        StringConverterProvider converters,
                                        List<? extends HttpRequestSerializer> serializers) {
        return new ResourceContextResolver(context);
    }

    private static class ResourceContextResolver implements ParamResolver {

        private final DeployContext context;

        private ResourceContextResolver(DeployContext context) {
            this.context = context;
        }

        @Override
        public Object resolve(ParamResolverContext context) throws Exception {
            return new ResourceContextImpl(this.context.handlerFactory().orElse(null), context.requestContext());
        }
    }
}

