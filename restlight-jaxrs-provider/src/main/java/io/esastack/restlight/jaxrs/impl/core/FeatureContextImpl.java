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
import jakarta.ws.rs.core.FeatureContext;

import java.util.Map;

public class FeatureContextImpl implements FeatureContext {

    private static final Logger logger = LoggerFactory.getLogger(FeatureContextImpl.class);

    private final Class<?> featureType;
    private final Configurable<? extends Configurable<?>> underlying;

    public FeatureContextImpl(Class<?> featureType, Configurable<? extends Configurable<?>> underlying) {
        Checks.checkNotNull(featureType, "featureType");
        Checks.checkNotNull(underlying, "underlying");
        this.featureType = featureType;
        this.underlying = underlying;
    }

    @Override
    public Configuration getConfiguration() {
        return underlying.getConfiguration();
    }

    @Override
    public FeatureContext property(String name, Object value) {
        underlying.property(name, value);
        return this;
    }

    @Override
    public FeatureContext register(Class<?> componentClass) {
        checkState(componentClass);
        underlying.register(componentClass);
        return this;
    }

    @Override
    public FeatureContext register(Class<?> componentClass, int priority) {
        checkState(componentClass);
        underlying.register(componentClass, priority);
        return this;
    }

    @Override
    public FeatureContext register(Class<?> componentClass, Class<?>... contracts) {
        checkState(componentClass, contracts);
        underlying.register(componentClass, contracts);
        return this;
    }

    @Override
    public FeatureContext register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        checkState(componentClass, contracts);
        underlying.register(componentClass, contracts);
        return this;
    }

    @Override
    public FeatureContext register(Object component) {
        checkState(component);
        underlying.register(component);
        return this;
    }

    @Override
    public FeatureContext register(Object component, int priority) {
        checkState(component);
        underlying.register(component, priority);
        return this;
    }

    @Override
    public FeatureContext register(Object component, Class<?>... contracts) {
        checkState(component, contracts);
        underlying.register(component, contracts);
        return this;
    }

    @Override
    public FeatureContext register(Object component, Map<Class<?>, Integer> contracts) {
        checkState(component, contracts);
        underlying.register(component, contracts);
        return this;
    }

    private void checkState(Object component) {
        Class<?> userType = ClassUtils.getUserType(component);
        if (JaxrsUtils.isFeature(userType)) {
            logger.warn("Registering nested feature({}) in feature({}) is unsupported.",
                    userType, featureType);
        }
    }

    private void checkState(Object component, Class<?>... contracts) {
        if (contracts == null) {
            return;
        }
        for (Class<?> userType : contracts) {
            if (JaxrsUtils.isFeature(userType)) {
                logger.warn("Registering nested feature({}) in feature({}) is unsupported.",
                        ClassUtils.getUserType(component), featureType);
            }
            return;
        }
    }

    private void checkState(Object component, Map<Class<?>, Integer> contracts) {
        if (contracts == null) {
            return;
        }

        for (Class<?> userType : contracts.keySet()) {
            if (JaxrsUtils.isFeature(userType)) {
                logger.warn("Registering nested feature({}) in feature({}) is unsupported.",
                        ClassUtils.getUserType(component), featureType);
            }
            return;
        }
    }
}


