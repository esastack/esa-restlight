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
        if (contracts == null || contracts.length == 0) {
            return isRegistrableType(target);
        }

        for (Class<?> contract : contracts) {
            if (ContainerResponseFilter.class.equals(contract)
                    || ReaderInterceptor.class.equals(contract)
                    || WriterInterceptor.class.equals(contract)) {
                continue;
            }
            if (ContainerRequestFilter.class.equals(contract)) {
                if (JaxrsUtils.isPreMatched(target)) {
                    logger.warn("Registering {} as {} in DynamicFeature({}) is unsupported, please remove" +
                            " @PreMatching of it.", target, contract, featureType);
                } else {
                    continue;
                }
            }
            logger.warn("Registering {} as {} in DynamicFeature({}) is unsupported.",
                    target, contract, featureType);
        }
        return true;
    }

    private boolean isRegistrableType(Class<?> target) {
        if (ContainerRequestFilter.class.isAssignableFrom(target)) {
            if (JaxrsUtils.isPreMatched(target)) {
                logger.warn("Registering {} as ContainerRequestFilter in DynamicFeature({}) is unsupported,"
                        + " please remove @PreMatching of it.", target, featureType);
                return false;
            } else {
                return true;
            }
        } else if (ContainerResponseFilter.class.isAssignableFrom(target)) {
            return true;
        } else if (ReaderInterceptor.class.isAssignableFrom(target)) {
            return true;
        } else {
            return WriterInterceptor.class.isAssignableFrom(target);
        }
    }
}
