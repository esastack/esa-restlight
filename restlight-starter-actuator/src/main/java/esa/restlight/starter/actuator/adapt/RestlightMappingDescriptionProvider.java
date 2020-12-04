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

import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.spring.util.RestlightDeployContextAware;
import org.springframework.boot.actuate.web.mappings.MappingDescriptionProvider;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
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
                        if (route.handler().isPresent()
                                && route.handler().get() instanceof InvocableMethod) {
                            InvocableMethod handler = (InvocableMethod) route.handler().get();
                            return new MappingResult(String.join(",", route.mapping().path()),
                                    getBeanName(handler, context),
                                    handler.method().toGenericString());
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return mappings;
    }

    private String getBeanName(InvocableMethod method, ApplicationContext context) {
        String[] names = null;
        try {
            names = context.getBeanNamesForType(method.beanType());
        } catch (Exception ignored) {
        }

        if (names != null) {
            for (String name : names) {
                Object bean = context.getBean(name);
                if (bean == method.object()) {
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

    private static class MappingResult implements Serializable {

        private static final long serialVersionUID = 4155179740279359L;

        private String path;
        private String bean;
        private String method;

        MappingResult(String path, String bean, String method) {
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
