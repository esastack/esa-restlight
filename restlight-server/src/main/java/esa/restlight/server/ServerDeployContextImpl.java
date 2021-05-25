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
package esa.restlight.server;

import esa.commons.Checks;
import esa.restlight.server.bootstrap.DispatcherExceptionHandler;
import esa.restlight.server.bootstrap.DispatcherHandler;
import esa.restlight.server.config.ServerOptions;
import esa.restlight.server.route.ReadOnlyRouteRegistry;
import esa.restlight.server.schedule.Scheduler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ServerDeployContextImpl<O extends ServerOptions> implements ServerDeployContext<O> {

    private final String name;
    private final O options;

    private final Map<String, Object> attributes = new ConcurrentHashMap<>(16);
    private final Map<String, Scheduler> schedulers = new HashMap<>(16);
    private volatile ReadOnlyRouteRegistry registry;
    private volatile DispatcherHandler dispatcherHandler;
    private volatile List<DispatcherExceptionHandler> dispatcherExceptionHandlers;

    protected ServerDeployContextImpl(String name, O options) {
        Checks.checkNotNull(options, "name");
        Checks.checkNotNull(options, "options");
        this.name = name;
        this.options = options;
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
    public Optional<ReadOnlyRouteRegistry> routeRegistry() {
        return Optional.ofNullable(registry);
    }

    @Override
    public Optional<DispatcherHandler> dispatcherHandler() {
        return Optional.of(dispatcherHandler);
    }

    @Override
    public Optional<List<DispatcherExceptionHandler>> dispatcherExceptionHandlers() {
        return Optional.of(Collections.unmodifiableList(dispatcherExceptionHandlers));
    }

    @Override
    public void attribute(String key, Object value) {
        attributes.put(key, value);
    }

    @Override
    public Object attribute(String key) {
        return attributes.get(key);
    }

    @Override
    public Object removeAttribute(String key) {
        return attributes.remove(key);
    }

    Map<String, Scheduler> mutableSchedulers() {
        return schedulers;
    }

    void setRegistry(ReadOnlyRouteRegistry registry) {
        this.registry = registry;
    }

    void setDispatcherHandler(DispatcherHandler dispatcherHandler) {
        this.dispatcherHandler = dispatcherHandler;
    }

    void setDispatcherExceptionHandlers(List<DispatcherExceptionHandler> dispatcherExceptionHandlers) {
        this.dispatcherExceptionHandlers = dispatcherExceptionHandlers;
    }
}
