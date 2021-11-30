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

import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.jaxrs.impl.JaxrsContextUtils;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

import static io.esastack.restlight.jaxrs.impl.JaxrsContextUtils.hasContextAnnotation;

public class SecurityContextParamResolver implements ParamResolverFactory {

    @Override
    public boolean supports(Param param) {
        return hasContextAnnotation(param) && SecurityContext.class.equals(param.type());
    }

    @Override
    public ParamResolver createResolver(Param param, List<? extends HttpRequestSerializer> serializers) {
        return new SecurityContextResolver();
    }

    private static class SecurityContextResolver implements ParamResolver {

        @Override
        public Object resolve(Param param, RequestContext context) throws Exception {
            return JaxrsContextUtils.getSecurityContext(context);
        }
    }
}

