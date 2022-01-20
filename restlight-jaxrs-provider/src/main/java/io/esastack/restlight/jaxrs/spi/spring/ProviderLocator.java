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
package io.esastack.restlight.jaxrs.spi.spring;

import esa.commons.annotation.Internal;
import esa.commons.spi.Feature;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.spring.spi.ExtensionLocator;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ext.Provider;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Internal
@Feature(tags = Constants.INTERNAL)
public class ProviderLocator implements ExtensionLocator {

    @Override
    public Collection<Object> getExtensions(ApplicationContext spring,
                                            DeployContext ctx) {
        Map<String, Object> providers = spring.getBeansWithAnnotation(Provider.class);
        Set<Object> extensions = new HashSet<>(providers.values());
        Map<String, Application> applications = spring.getBeansOfType(Application.class);
        extensions.addAll(applications.values());
        return extensions;
    }

}

