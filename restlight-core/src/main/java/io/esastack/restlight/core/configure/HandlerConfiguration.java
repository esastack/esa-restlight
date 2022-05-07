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
package io.esastack.restlight.core.configure;

import esa.commons.Checks;
import esa.commons.collection.Attributes;
import io.esastack.restlight.core.resolver.context.ContextResolverFactory;
import io.esastack.restlight.core.resolver.param.ParamResolverAdviceFactory;
import io.esastack.restlight.core.resolver.param.ParamResolverFactory;
import io.esastack.restlight.core.resolver.reqentity.RequestEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.reqentity.RequestEntityResolverFactory;
import io.esastack.restlight.core.resolver.rspentity.ResponseEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.rspentity.ResponseEntityResolverFactory;
import io.esastack.restlight.core.resolver.converter.StringConverterFactory;
import io.esastack.restlight.core.spi.RouteFilterFactory;

import java.util.Collection;
import java.util.List;

public class HandlerConfiguration {

    private final Attributes attributes;
    private final List<RouteFilterFactory> routeFilters;
    private final List<StringConverterFactory> stringConverters;
    private final List<ParamResolverFactory> paramResolvers;
    private final List<ParamResolverAdviceFactory> paramResolverAdvices;
    private final List<ContextResolverFactory> contextResolvers;
    private final List<RequestEntityResolverFactory> requestEntityResolvers;
    private final List<RequestEntityResolverAdviceFactory> requestEntityResolverAdvices;
    private final List<ResponseEntityResolverFactory> responseEntityResolvers;
    private final List<ResponseEntityResolverAdviceFactory> responseEntityResolverAdvices;

    public HandlerConfiguration(Attributes attributes,
                                List<RouteFilterFactory> routeFilters,
                                List<StringConverterFactory> stringConverters,
                                List<ParamResolverFactory> paramResolvers,
                                List<ParamResolverAdviceFactory> paramResolverAdvices,
                                List<ContextResolverFactory> contextResolvers,
                                List<RequestEntityResolverFactory> requestEntityResolvers,
                                List<RequestEntityResolverAdviceFactory> requestEntityResolverAdvices,
                                List<ResponseEntityResolverFactory> responseEntityResolvers,
                                List<ResponseEntityResolverAdviceFactory> responseEntityResolverAdvices) {
        Checks.checkNotNull(attributes, "attributes");
        this.attributes = attributes;
        this.routeFilters = routeFilters;
        this.stringConverters = stringConverters;
        this.paramResolvers = paramResolvers;
        this.paramResolverAdvices = paramResolverAdvices;
        this.contextResolvers = contextResolvers;
        this.requestEntityResolvers = requestEntityResolvers;
        this.requestEntityResolverAdvices = requestEntityResolverAdvices;
        this.responseEntityResolvers = responseEntityResolvers;
        this.responseEntityResolverAdvices = responseEntityResolverAdvices;
    }

    public Attributes attrs() {
        return attributes;
    }

    public List<StringConverterFactory> getStringConverts() {
        return stringConverters;
    }

    public List<ParamResolverFactory> getParamResolvers() {
        return paramResolvers;
    }

    public List<ParamResolverAdviceFactory> getParamResolverAdvices() {
        return paramResolverAdvices;
    }

    public List<ContextResolverFactory> getContextResolvers() {
        return contextResolvers;
    }

    public List<RequestEntityResolverFactory> getRequestEntityResolvers() {
        return requestEntityResolvers;
    }

    public List<RequestEntityResolverAdviceFactory> getRequestEntityResolverAdvices() {
        return requestEntityResolverAdvices;
    }

    public List<ResponseEntityResolverFactory> getResponseEntityResolvers() {
        return responseEntityResolvers;
    }

    public List<ResponseEntityResolverAdviceFactory> getResponseEntityResolverAdvices() {
        return responseEntityResolverAdvices;
    }

    public List<RouteFilterFactory> getRouteFilters() {
        return routeFilters;
    }

    void addRouteFilters(Collection<? extends RouteFilterFactory> filters) {
        if (filters != null) {
            this.routeFilters.addAll(filters);
        }
    }

    void addParamResolvers(Collection<? extends ParamResolverFactory> resolvers) {
        if (resolvers != null) {
            this.paramResolvers.addAll(resolvers);
        }
    }

    void addParamResolverAdvices(Collection<? extends ParamResolverAdviceFactory> advices) {
        if (advices != null) {
            this.paramResolverAdvices.addAll(advices);
        }
    }

    void addContextResolvers(Collection<? extends ContextResolverFactory> resolvers) {
        if (resolvers != null) {
            this.contextResolvers.addAll(resolvers);
        }
    }

    void addRequestEntityResolvers(Collection<? extends RequestEntityResolverFactory> resolvers) {
        if (resolvers != null) {
            this.requestEntityResolvers.addAll(resolvers);
        }
    }

    void addRequestEntityResolverAdvices(Collection<? extends RequestEntityResolverAdviceFactory> advices) {
        if (advices != null) {
            this.requestEntityResolverAdvices.addAll(advices);
        }
    }

    void addResponseEntityResolvers(Collection<? extends ResponseEntityResolverFactory> resolvers) {
        if (resolvers != null) {
            this.responseEntityResolvers.addAll(resolvers);
        }
    }

    void addResponseEntityResolverAdvices(Collection<? extends ResponseEntityResolverAdviceFactory> advices) {
        if (advices != null) {
            this.responseEntityResolverAdvices.addAll(advices);
        }
    }
}

