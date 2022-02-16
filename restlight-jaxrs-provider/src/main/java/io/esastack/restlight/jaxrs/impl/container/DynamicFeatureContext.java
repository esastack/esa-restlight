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
package io.esastack.restlight.jaxrs.impl.container;

import esa.commons.Checks;
import esa.commons.ClassUtils;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import io.esastack.restlight.jaxrs.impl.core.ConfigurableImpl;
import io.esastack.restlight.jaxrs.impl.core.ConfigurationImpl;
import io.esastack.restlight.jaxrs.util.JaxrsUtils;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Configurable;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.WriterInterceptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DynamicFeatureContext implements FeatureContext {

    private static final Logger logger = LoggerFactory.getLogger(DynamicFeatureContext.class);

    private final Class<?> featureType;
    private final ConfigurationImpl configuration;
    private final Configurable<ConfigurableImpl> configurable;

    public DynamicFeatureContext(Class<?> featureType, ConfigurationImpl configuration) {
        Checks.checkNotNull(featureType, "featureType");
        Checks.checkNotNull(configuration, "configuration");
        this.featureType = featureType;
        this.configuration = configuration;
        this.configurable = new ConfigurableImpl(configuration);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public FeatureContext property(String name, Object value) {
        configuration.setProperty(name, value);
        return this;
    }

    @Override
    public FeatureContext register(Class<?> componentClass) {
        if (componentClass == null) {
            return this;
        }
        Map<Class<?>, Integer> contracts = checkContracts(componentClass,
                JaxrsUtils.getComponents(componentClass).toArray(new Class[0]), JaxrsUtils.getOrder(componentClass));
        if (!contracts.isEmpty()) {
            configurable.register(componentClass, contracts);
        }
        return this;
    }

    @Override
    public FeatureContext register(Class<?> componentClass, int priority) {
        if (componentClass == null) {
            return this;
        }
        Map<Class<?>, Integer> contracts = checkContracts(componentClass,
                JaxrsUtils.getComponents(componentClass).toArray(new Class[0]), priority);
        if (!contracts.isEmpty()) {
            configurable.register(componentClass, contracts);
        }
        return this;
    }

    @Override
    public FeatureContext register(Class<?> componentClass, Class<?>... contracts) {
        if (componentClass == null || contracts == null) {
            return this;
        }
        Map<Class<?>, Integer> contracts0 = checkContracts(componentClass, contracts,
                JaxrsUtils.getOrder(componentClass));
        if (!contracts0.isEmpty()) {
            configurable.register(componentClass, contracts0);
        }
        return this;
    }

    @Override
    public FeatureContext register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        if (componentClass == null || contracts == null) {
            return this;
        }
        Map<Class<?>, Integer> contracts0 = checkContracts(componentClass, contracts);
        if (!contracts0.isEmpty()) {
            configurable.register(componentClass, contracts0);
        }
        return this;
    }

    @Override
    public FeatureContext register(Object component) {
        if (component == null) {
            return this;
        }
        final Class<?> userType = ClassUtils.getUserType(component);
        Map<Class<?>, Integer> contracts = checkContracts(userType,
                JaxrsUtils.getComponents(userType).toArray(new Class[0]), JaxrsUtils.getOrder(userType));
        if (!contracts.isEmpty()) {
            configurable.register(component, contracts);
        }
        return this;
    }

    @Override
    public FeatureContext register(Object component, int priority) {
        if (component == null) {
            return this;
        }
        final Class<?> userType = ClassUtils.getUserType(component);
        Map<Class<?>, Integer> contracts = checkContracts(userType,
                JaxrsUtils.getComponents(userType).toArray(new Class[0]), priority);
        if (!contracts.isEmpty()) {
            configurable.register(component, contracts);
        }
        return this;
    }

    @Override
    public FeatureContext register(Object component, Class<?>... contracts) {
        if (component == null || contracts == null) {
            return this;
        }
        Class<?> userType = ClassUtils.getUserType(component);
        Map<Class<?>, Integer> contracts0 = checkContracts(userType, contracts, JaxrsUtils.getOrder(userType));
        if (!contracts0.isEmpty()) {
            configurable.register(component, contracts0);
        }
        return this;
    }

    @Override
    public FeatureContext register(Object component, Map<Class<?>, Integer> contracts) {
        if (component == null || contracts == null) {
            return this;
        }
        Class<?> userType = ClassUtils.getUserType(component);
        Map<Class<?>, Integer> contracts0 = checkContracts(userType, contracts);
        if (!contracts0.isEmpty()) {
            configurable.register(component, contracts0);
        }
        return this;
    }

    private Map<Class<?>, Integer> checkContracts(Class<?> target, Map<Class<?>, Integer> contracts) {
        if (contracts == null || contracts.size() == 0) {
            return Collections.emptyMap();
        }

        Map<Class<?>, Integer> checked = checkContracts(target, contracts.keySet().toArray(new Class[0]),
                JaxrsUtils.getOrder(target));

        List<Class<?>> removable = new LinkedList<>();
        contracts.keySet().forEach(key -> {
            if (!checked.containsKey(key)) {
                removable.add(key);
            }
        });

        removable.forEach(contracts::remove);
        return contracts;
    }

    private Map<Class<?>, Integer> checkContracts(Class<?> target, Class<?>[] contracts, int priority) {
        if (contracts == null || contracts.length == 0) {
            return Collections.emptyMap();
        }

        Map<Class<?>, Integer> checked = new HashMap<>(contracts.length);
        for (Class<?> contract : contracts) {
            if (ContainerResponseFilter.class.equals(contract)
                    || ReaderInterceptor.class.equals(contract)
                    || WriterInterceptor.class.equals(contract)) {
                checked.put(contract, priority);
                continue;
            }
            if (ContainerRequestFilter.class.equals(contract)) {
                if (JaxrsUtils.isPreMatched(target)) {
                    logger.warn("Registering {} as {} in DynamicFeature({}) is unsupported, please remove" +
                            " @PreMatching on it.", target, contract, featureType);
                } else {
                    checked.put(contract, priority);
                }
                continue;
            }
            logger.warn("Registering {} as {} in DynamicFeature({}) is unsupported.",
                    target, contract, featureType);
        }
        return checked;
    }

}
