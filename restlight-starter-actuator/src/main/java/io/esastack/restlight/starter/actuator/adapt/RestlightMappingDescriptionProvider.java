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
package io.esastack.restlight.starter.actuator.adapt;

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.util.RouteUtils;
import io.esastack.restlight.spring.util.RestlightDeployContextAware;
import org.springframework.boot.actuate.web.mappings.MappingDescriptionProvider;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class RestlightMappingDescriptionProvider implements MappingDescriptionProvider, RestlightDeployContextAware {

    private static final String MAPPING_NAME = "Restlight-Mappings";

    private List<MappingResult> mappings;
    private DeployContext<? extends RestlightOptions> deployContext;

    @Override
    public String getMappingName() {
        return MAPPING_NAME;
    }

    @Override
    public Object describeMappings(ApplicationContext context) {
        if (mappings == null
                && deployContext != null
                && deployContext.routeRegistry().isPresent()) {

            mappings = deployContext.routeRegistry()
                    .get()
                    .routes()
                    .stream()
                    .map(route -> {
                        Optional<HandlerMethod> handlerMethod;
                        if (route.handler().isPresent()
                                && (handlerMethod = RouteUtils.extractHandlerMethod(route)).isPresent()) {
                            return new MappingResult(String.join(",", route.mapping().path()),
                                    getBeanName(handlerMethod.get(),
                                            RouteUtils.extractHandlerBean(route).orElse(null), context),
                                    handlerMethod.get().method().toGenericString());
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return mappings;
    }

    private String getBeanName(HandlerMethod method, Object bean, ApplicationContext context) {
        String[] names = null;
        try {
            names = context.getBeanNamesForType(method.beanType());
        } catch (Exception ignored) {
        }

        if (bean != null && names != null) {
            for (String name : names) {
                if (bean == context.getBean(name)) {
                    return name;
                }
            }
        }
        return method.beanType().getSimpleName();
    }

    @Override
    public void setDeployContext(DeployContext<? extends RestlightOptions> ctx) {
        this.deployContext = ctx;
    }

    static class MappingResult implements Serializable {

        private static final long serialVersionUID = 4155179740279359L;

        private String path;
        private String bean;
        private String method;

        private MappingResult(String path, String bean, String method) {
            this.path = path;
            this.bean = bean;
            this.method = method;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getBean() {
            return bean;
        }

        public void setBean(String bean) {
            this.bean = bean;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }
    }

}
