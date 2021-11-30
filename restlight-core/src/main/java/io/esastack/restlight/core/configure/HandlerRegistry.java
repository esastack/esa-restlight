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
package io.esastack.restlight.core.configure;

import io.esastack.restlight.core.handler.HandlerMapping;

import java.util.Collection;
import java.util.List;

/**
 * This is a facade registry which is designed to expose methods for user to add or remove Controller dynamically
 * after the server has started.
 */
public interface HandlerRegistry {

    /**
     * Gets all registered {@link HandlerMapping}s.
     *
     * @return immutable handler mappings.
     */
    List<HandlerMapping> mappings();

    /**
     * Register a controller dynamically.
     *
     * @param handler handler
     */
    void addHandler(Object handler);

    /**
     * Register given controllers dynamically.
     *
     * @param handlers handlers
     */
    void addHandlers(Collection<Object> handlers);

    /**
     * Register a controller dynamically.
     *
     * @param clazz     clazz
     * @param singleton singleton or not.
     */
    void addHandler(Class<?> clazz, boolean singleton);

    /**
     * Register controllers dynamically.
     *
     * @param classes   classes
     * @param singleton singleton or not.
     */
    void addHandlers(Collection<Class<?>> classes, boolean singleton);

    /**
     * Deregister the given controller.
     *
     * @param handler handler
     */
    void removeHandler(Object handler);

    /**
     * Deregister the given controllers.
     *
     * @param handlers handlers
     */
    void removeHandlers(Collection<Object> handlers);

    /**
     * Register the handler mapping.
     *
     * @param mapping mapping
     */
    void addHandlerMapping(HandlerMapping mapping);

    /**
     * Register the given handler mappings.
     *
     * @param mappings mappings
     */
    void addHandlerMappings(Collection<HandlerMapping> mappings);
}

