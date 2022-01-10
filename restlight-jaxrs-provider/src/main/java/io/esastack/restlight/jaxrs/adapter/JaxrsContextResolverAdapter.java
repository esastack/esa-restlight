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
import esa.commons.ClassUtils;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolverAdapter;
import io.esastack.restlight.core.util.ResponseEntityUtils;
import io.esastack.restlight.jaxrs.configure.ProxyComponent;
import io.esastack.restlight.jaxrs.util.JaxrsUtils;
import io.esastack.restlight.server.context.RequestContext;
import jakarta.ws.rs.ext.ContextResolver;

import java.util.List;

public class JaxrsContextResolverAdapter implements ParamResolverAdapter {

    private final ContextResolver<?> delegating;
    private final MediaType[] produces;
    private final int order;
    private final Class<?> matchableType;

    public JaxrsContextResolverAdapter(ProxyComponent<ContextResolver<?>> delegating) {
        Checks.checkNotNull(delegating, "delegating");
        this.delegating = delegating.proxied();
        this.produces = MediaTypes.covert(JaxrsUtils.produces(delegating.underlying()));
        this.order = JaxrsUtils.getOrder(delegating.underlying());
        this.matchableType = ClassUtils.findFirstGenericType(ClassUtils.getUserType(delegating.underlying()))
                .orElse(Object.class);
    }

    @Override
    public Object resolve(Param param, RequestContext context) throws Exception {
        final List<MediaType> mediaTypes = ResponseEntityUtils.getMediaTypes(context);
        if (MediaTypes.isCompatibleWith(produces, mediaTypes)) {
            return delegating.getContext(param.type());
        } else {
            return null;
        }
    }

    @Override
    public boolean supports(Param param) {
        return matchableType.isAssignableFrom(param.type());
    }

    @Override
    public int getOrder() {
        return order;
    }

}

