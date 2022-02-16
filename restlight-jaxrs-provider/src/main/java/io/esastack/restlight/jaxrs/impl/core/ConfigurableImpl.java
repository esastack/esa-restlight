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
package io.esastack.restlight.jaxrs.impl.core;

import esa.commons.Checks;
import esa.commons.ClassUtils;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import io.esastack.restlight.jaxrs.util.JaxrsUtils;
import jakarta.ws.rs.core.Configurable;
import jakarta.ws.rs.core.Configuration;

import java.util.HashMap;
import java.util.Map;

public class ConfigurableImpl implements Configurable<ConfigurableImpl> {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurableImpl.class);

    private final ConfigurationImpl configuration;

    public ConfigurableImpl(ConfigurationImpl configuration) {
        Checks.checkNotNull(configuration, "configuration");
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public ConfigurableImpl property(String name, Object value) {
        if (value == null) {
            configuration.removeProperty(name);
        } else {
            configuration.setProperty(name, value);
        }

        return this;
    }

    @Override
    public ConfigurableImpl register(Class<?> componentClass) {
        if (componentClass == null) {
            return this;
        }
        return register(componentClass, JaxrsUtils.extractContracts(componentClass,
                JaxrsUtils.getOrder(componentClass)));
    }

    @Override
    public ConfigurableImpl register(Class<?> componentClass, int priority) {
        if (componentClass == null) {
            return this;
        }
        return register(componentClass, JaxrsUtils.extractContracts(componentClass, priority));
    }

    @Override
    public ConfigurableImpl register(Class<?> componentClass, Class<?>... contracts) {
        if (componentClass == null || contracts == null || contracts.length == 0) {
            return this;
        }

        configuration.addProviderClass(componentClass, checkContracts(componentClass, contracts));
        return this;
    }

    @Override
    public ConfigurableImpl register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        if (componentClass == null || contracts == null || contracts.isEmpty()) {
            return this;
        }

        configuration.addProviderClass(componentClass, contracts);
        return this;
    }

    @Override
    public ConfigurableImpl register(Object component) {
        if (component == null) {
            return this;
        }
        return register(component, JaxrsUtils.extractContracts(component, JaxrsUtils.getOrder(component)));
    }

    @Override
    public ConfigurableImpl register(Object component, int priority) {
        if (component == null) {
            return this;
        }
        return register(component, JaxrsUtils.extractContracts(component, priority));
    }

    @Override
    public ConfigurableImpl register(Object component, Class<?>... contracts) {
        if (component == null || contracts == null || contracts.length == 0) {
            return this;
        }

        configuration.addProviderInstance(component, checkContracts(ClassUtils.getUserType(component), contracts));
        return this;
    }

    @Override
    public ConfigurableImpl register(Object component, Map<Class<?>, Integer> contracts) {
        if (component == null || contracts == null || contracts.isEmpty()) {
            return this;
        }
        configuration.addProviderInstance(component, contracts);
        return this;
    }

    private Map<Class<?>, Integer> checkContracts(Class<?> target, Class<?>... contracts) {
        int order = JaxrsUtils.getOrder(target);
        Map<Class<?>, Integer> checked = new HashMap<>();
        for (Class<?> contract : contracts) {
            if (!contract.isAssignableFrom(target)) {
                logger.warn("Failed to register {} as {}", target, contract);
            } else {
                checked.put(contract, order);
            }
        }

        return checked;
    }
}


