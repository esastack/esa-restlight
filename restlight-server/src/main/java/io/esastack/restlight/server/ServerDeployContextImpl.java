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
package io.esastack.restlight.server;

import esa.commons.Checks;
import esa.commons.collection.Attribute;
import esa.commons.collection.AttributeKey;
import esa.commons.collection.AttributeMap;
import esa.commons.collection.Attributes;
import io.esastack.restlight.server.bootstrap.DispatcherHandler;
import io.esastack.restlight.server.config.ServerOptions;
import io.esastack.restlight.server.route.RouteRegistry;
import io.esastack.restlight.server.schedule.Scheduler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class ServerDeployContextImpl<O extends ServerOptions> implements ServerDeployContext<O> {

    private final String name;
    private final O options;

    private final Attributes attributes;
    private final Map<String, Scheduler> schedulers = new HashMap<>(16);
    private volatile RouteRegistry registry;
    private volatile DispatcherHandler dispatcherHandler;

    protected ServerDeployContextImpl(String name, O options) {
        Checks.checkNotNull(options, "name");
        Checks.checkNotNull(options, "options");
        this.name = name;
        this.options = options;
        this.attributes = new AttributeMap(8);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public O options() {
        return options;
    }

    @Override
    public Map<String, Scheduler> schedulers() {
        return Collections.unmodifiableMap(schedulers);
    }

    @Override
    public Optional<RouteRegistry> routeRegistry() {
        return Optional.ofNullable(registry);
    }

    @Override
    public Optional<DispatcherHandler> dispatcherHandler() {
        return Optional.of(dispatcherHandler);
    }

    @Override
    public <V> Attribute<V> attr(AttributeKey<V> key) {
        return attributes.attr(key);
    }

    @Override
    public boolean hasAttr(AttributeKey<?> key) {
        return attributes.hasAttr(key);
    }

    @Override
    public void forEach(BiConsumer<? super AttributeKey<?>, ? super Attribute<?>> consumer) {
        attributes.forEach(consumer);
    }

    @Override
    public int size() {
        return attributes.size();
    }

    @Override
    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    Map<String, Scheduler> mutableSchedulers() {
        return schedulers;
    }

    void setRegistry(RouteRegistry registry) {
        this.registry = registry;
    }

    void setDispatcherHandler(DispatcherHandler dispatcherHandler) {
        this.dispatcherHandler = dispatcherHandler;
    }
}
