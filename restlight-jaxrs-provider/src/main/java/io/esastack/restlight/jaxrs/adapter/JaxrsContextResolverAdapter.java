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
package io.esastack.restlight.jaxrs.adapter;

import esa.commons.Checks;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolverAdapter;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.core.util.ResponseEntityUtils;
import io.esastack.restlight.jaxrs.util.JaxrsUtils;
import io.esastack.restlight.jaxrs.util.MediaTypeUtils;
import io.esastack.restlight.server.context.RequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Providers;

public class JaxrsContextResolverAdapter implements ParamResolverAdapter {

    private final Providers providers;

    public JaxrsContextResolverAdapter(Providers providers) {
        Checks.checkNotNull(providers, "providers");
        this.providers = providers;
    }

    @Override
    public Object resolve(Param param, RequestContext context) throws Exception {
        ContextResolver<?> resolver;
        for (MediaType mediaType : ResponseEntityUtils.getMediaTypes(context)) {
            if ((resolver = providers.getContextResolver(param.type(),
                    MediaTypeUtils.convert(mediaType))) != null) {
                return resolver;
            }
        }
        return null;
    }

    @Override
    public boolean supports(Param param) {
        return JaxrsUtils.hasAnnotation(param, Context.class)
                && providers.getContextResolver(param.type(), jakarta.ws.rs.core.MediaType.WILDCARD_TYPE) != null;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}

