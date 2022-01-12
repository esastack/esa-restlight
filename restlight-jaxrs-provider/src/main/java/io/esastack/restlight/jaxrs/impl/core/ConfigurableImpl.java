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
        return register(componentClass, JaxrsUtils.extractContracts(componentClass,
                JaxrsUtils.getOrder(componentClass)));
    }

    @Override
    public ConfigurableImpl register(Class<?> componentClass, int priority) {
        return register(componentClass, JaxrsUtils.extractContracts(componentClass, priority));
    }

    @Override
    public ConfigurableImpl register(Class<?> componentClass, Class<?>... contracts) {
        if (contracts == null || contracts.length == 0) {
            return this;
        }

        configuration.addProviderClass(componentClass, effectiveContracts(componentClass, contracts));
        return this;
    }

    @Override
    public ConfigurableImpl register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        if (contracts == null || contracts.isEmpty()) {
            return this;
        }

        configuration.addProviderClass(componentClass, effectiveContracts(componentClass, contracts));
        return this;
    }

    @Override
    public ConfigurableImpl register(Object component) {
        return register(component, JaxrsUtils.extractContracts(component, JaxrsUtils.getOrder(component)));
    }

    @Override
    public ConfigurableImpl register(Object component, int priority) {
        return register(component, JaxrsUtils.extractContracts(component, priority));
    }

    @Override
    public ConfigurableImpl register(Object component, Class<?>... contracts) {
        if (contracts == null || contracts.length == 0) {
            return this;
        }

        configuration.addProviderInstance(component, effectiveContracts(ClassUtils.getUserType(component), contracts));
        return this;
    }

    @Override
    public ConfigurableImpl register(Object component, Map<Class<?>, Integer> contracts) {
        if (contracts == null || contracts.isEmpty()) {
            return this;
        }
        configuration.addProviderInstance(component, effectiveContracts(ClassUtils.getUserType(component), contracts));
        return this;
    }

    private static Map<Class<?>, Integer> effectiveContracts(Class<?> target, Class<?>... contracts) {
        int order = JaxrsUtils.getOrder(target);
        Map<Class<?>, Integer> effectiveContracts = new HashMap<>();
        for (Class<?> contract : contracts) {
            if (!target.isAssignableFrom(contract)) {
                logger.warn("Failed to register {} as {}", target, contract);
            } else {
                effectiveContracts.put(contract, order);
            }
        }

        return effectiveContracts;
    }

    private static Map<Class<?>, Integer> effectiveContracts(Class<?> clazz, Map<Class<?>, Integer> contracts) {
        Map<Class<?>, Integer> effectiveContracts = new HashMap<>();
        for (Map.Entry<Class<?>, Integer> entry : contracts.entrySet()) {
            if (!entry.getKey().isAssignableFrom(clazz)) {
                logger.warn("Failed to register {} as {}", clazz, entry.getKey());
            } else {
                effectiveContracts.put(entry.getKey(), entry.getValue());
            }
        }
        return effectiveContracts;
    }
}


