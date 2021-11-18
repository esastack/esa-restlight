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
import io.esastack.restlight.jaxrs.impl.core.ConfigurableImpl;
import io.esastack.restlight.jaxrs.impl.core.ConfigurationImpl;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Configurable;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.WriterInterceptor;

import java.util.Map;

public class DynamicFeatureContext implements FeatureContext {

    private final ConfigurationImpl configuration;
    private final Configurable<ConfigurableImpl> configurable;

    public DynamicFeatureContext(ConfigurationImpl configuration) {
        Checks.checkNotNull(configuration, "configuration");
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
        if (isRegistrable(componentClass, null)) {
            configurable.register(componentClass);
        }
        return this;
    }

    @Override
    public FeatureContext register(Class<?> componentClass, int priority) {
        if (isRegistrable(componentClass, null)) {
            configurable.register(componentClass, priority);
        }
        return this;
    }

    @Override
    public FeatureContext register(Class<?> componentClass, Class<?>... contracts) {
        if (isRegistrable(componentClass, contracts)) {
            configurable.register(componentClass, contracts);
        }
        return this;
    }

    @Override
    public FeatureContext register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        if (isRegistrable(componentClass, contracts.keySet().toArray(new Class<?>[0]))) {
            configurable.register(componentClass, contracts);
        }
        return this;
    }

    @Override
    public FeatureContext register(Object component) {
        if (isRegistrable(ClassUtils.getUserType(component), null)) {
            configurable.register(component);
        }
        return this;
    }

    @Override
    public FeatureContext register(Object component, int priority) {
        if (isRegistrable(ClassUtils.getUserType(component), null)) {
            configurable.register(component, priority);
        }
        return this;
    }

    @Override
    public FeatureContext register(Object component, Class<?>... contracts) {
        if (isRegistrable(ClassUtils.getUserType(component), contracts)) {
            configurable.register(component, contracts);
        }
        return this;
    }

    @Override
    public FeatureContext register(Object component, Map<Class<?>, Integer> contracts) {
        if (isRegistrable(ClassUtils.getUserType(component), contracts.keySet().toArray(new Class<?>[0]))) {
            configurable.register(component, contracts);
        }
        return this;
    }

    private boolean isRegistrable(Class<?> target, Class<?>[] contracts) {
        if (ContainerRequestFilter.class.isAssignableFrom(target)) {
            return isLegalContracts(contracts);
        } else if (ContainerResponseFilter.class.isAssignableFrom(target)) {
            return isLegalContracts(contracts);
        } else if (ReaderInterceptor.class.isAssignableFrom(target)) {
            return isLegalContracts(contracts);
        } else if (WriterInterceptor.class.isAssignableFrom(target)) {
            return isLegalContracts(contracts);
        } else if (Feature.class.isAssignableFrom(target)) {
            return isLegalContracts(contracts);
        }
        return false;
    }

    private boolean isLegalContracts(Class<?>[] contracts) {
        if (contracts == null || contracts.length == 0) {
            return true;
        }
        for (Class<?> contract : contracts) {
            if (ContainerRequestFilter.class.equals(contract)
                    || ContainerResponseFilter.class.equals(contract)
                    || ReaderInterceptor.class.equals(contract)
                    || WriterInterceptor.class.equals(contract)
                    || Feature.class.equals(contract)) {
                continue;
            }
            return false;
        }
        return true;
    }
}

