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
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolverAdapter;
import io.esastack.restlight.core.util.ResponseEntityUtils;
import io.esastack.restlight.jaxrs.configure.ProxyComponent;
import io.esastack.restlight.jaxrs.util.JaxrsUtils;
import io.esastack.restlight.jaxrs.util.MediaTypeUtils;
import io.esastack.restlight.server.context.RequestContext;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JaxrsContextResolverAdapter implements ParamResolverAdapter {

    private final jakarta.ws.rs.ext.ContextResolver<?> delegating;
    private final List<io.esastack.commons.net.http.MediaType> produces;
    private final int order;
    private final Class<?> matchableType;

    public JaxrsContextResolverAdapter(ProxyComponent<jakarta.ws.rs.ext.ContextResolver<?>> delegating) {
        Checks.checkNotNull(delegating, "delegating");
        this.delegating = delegating.proxied();
        this.produces = coverts(JaxrsUtils.produces(delegating.underlying()));
        this.order = JaxrsUtils.getOrder(delegating.underlying());
        this.matchableType = ClassUtils.findFirstGenericType(ClassUtils.getUserType(delegating.underlying()))
                .orElse(Object.class);
    }

    @Override
    public Object resolve(Param param, RequestContext context) throws Exception {
        final List<io.esastack.commons.net.http.MediaType> mediaTypes = ResponseEntityUtils.getMediaTypes(context);
        if (isMatchable(mediaTypes)) {
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

    private boolean isMatchable(List<io.esastack.commons.net.http.MediaType> currents) {
        if (produces.isEmpty()) {
            return true;
        }
        for (io.esastack.commons.net.http.MediaType target : produces) {
            for (io.esastack.commons.net.http.MediaType current : currents) {
                if (current.isCompatibleWith(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<io.esastack.commons.net.http.MediaType> coverts(List<MediaType> target) {
        if (target == null || target.isEmpty()) {
            return Collections.emptyList();
        }
        List<io.esastack.commons.net.http.MediaType> mediaTypes = new ArrayList<>(target.size());
        for (MediaType mediaType : target) {
            mediaTypes.add(MediaTypeUtils.convert(mediaType));
        }
        return mediaTypes;
    }
}

