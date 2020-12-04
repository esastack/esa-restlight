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
package esa.restlight.starter.actuator.adapt;

import esa.commons.Checks;
import esa.commons.UrlUtils;
import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.handler.HandlerMapping;
import esa.restlight.core.handler.HandlerMappingProvider;
import esa.restlight.core.handler.RouteHandler;
import esa.restlight.core.handler.impl.HandlerMappingImpl;
import esa.restlight.core.handler.impl.RouteHandlerImpl;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.HttpMethod;
import esa.restlight.core.util.ConverterUtils;
import esa.restlight.server.route.Mapping;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.web.*;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Abstract implementation of {@link HandlerMappingProvider} for creating {@link HandlerMapping}s which handle the
 * actuator web operations in {@link WebEndpointsSupplier}.
 */
public abstract class BaseHandlerMappingProvider implements HandlerMappingProvider {

    private final EndpointMapping endpointMapping;
    private final Collection<ExposableWebEndpoint> endpoints;

    BaseHandlerMappingProvider(WebEndpointsSupplier webEndpointsSupplier,
                               WebEndpointProperties webEndpointProperties) {
        Checks.checkNotNull(webEndpointsSupplier, "webEndpointsSupplier");
        Checks.checkNotNull(webEndpointProperties, "webEndpointProperties");
        this.endpointMapping = new EndpointMapping(webEndpointProperties.getBasePath());
        this.endpoints = webEndpointsSupplier.getEndpoints();
    }

    @Override
    public Collection<HandlerMapping> mappings(DeployContext<? extends RestlightOptions> ctx) {
        return endpoints.stream()
                .flatMap(e -> e.getOperations()
                        .stream()
                        .map(op -> this.createMapping(op, ctx)))
                .collect(Collectors.toList());
    }

    private HandlerMapping createMapping(WebOperation op, DeployContext<? extends RestlightOptions> ctx) {
        // create mapping by predicate
        final WebOperationRequestPredicate predicate = op.getRequestPredicate();
        String path;
        if (ctx.options().getContextPath() != null) {
            path = ConverterUtils.standardContextPath(ctx.options().getContextPath()) +
                    endpointMapping.getPath() +
                    UrlUtils.prependLeadingSlash(predicate.getPath());
        } else {
            path = endpointMapping.getPath() +
                    UrlUtils.prependLeadingSlash(predicate.getPath());
        }
        final Mapping mapping =
                Mapping.mapping().path(path)
                        .method(HttpMethod.valueOf(predicate.getHttpMethod().name()))
                        .consumes(predicate.getConsumes().toArray(new String[0]))
                        .produces(predicate.getProduces().toArray(new String[0]));

        final RouteHandler handler =
                new RouteHandlerImpl(getHandler(op),
                        false,
                        ctx.options().getScheduling().getDefaultScheduler());
        return new HandlerMappingImpl(mapping, handler);
    }

    /**
     * Gets a instance of {@link HandlerMethod} for given {@link WebOperation}
     *
     * @param op operation
     *
     * @return instance of {@link HandlerMethod}
     */
    abstract HandlerMethod getHandler(WebOperation op);
}
