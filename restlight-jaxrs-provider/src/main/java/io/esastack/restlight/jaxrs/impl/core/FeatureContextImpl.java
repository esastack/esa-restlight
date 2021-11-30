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
import jakarta.ws.rs.core.Configurable;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.FeatureContext;

import java.util.Map;

public class FeatureContextImpl implements FeatureContext {

    private final Configurable<? extends Configurable<?>> underlying;

    public FeatureContextImpl(Configurable<? extends Configurable<?>> underlying) {
        Checks.checkNotNull(underlying, "underlying");
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
        underlying.register(componentClass);
        return this;
    }

    @Override
    public FeatureContext register(Class<?> componentClass, int priority) {
        underlying.register(componentClass, priority);
        return this;
    }

    @Override
    public FeatureContext register(Class<?> componentClass, Class<?>... contracts) {
        underlying.register(componentClass, contracts);
        return this;
    }

    @Override
    public FeatureContext register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        underlying.register(componentClass, contracts);
        return this;
    }

    @Override
    public FeatureContext register(Object component) {
        underlying.register(component);
        return this;
    }

    @Override
    public FeatureContext register(Object component, int priority) {
        underlying.register(component, priority);
        return this;
    }

    @Override
    public FeatureContext register(Object component, Class<?>... contracts) {
        underlying.register(component, contracts);
        return this;
    }

    @Override
    public FeatureContext register(Object component, Map<Class<?>, Integer> contracts) {
        underlying.register(component, contracts);
        return this;
    }
}


