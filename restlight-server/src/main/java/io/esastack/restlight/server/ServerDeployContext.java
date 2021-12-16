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

import esa.commons.collection.Attributes;
import io.esastack.restlight.server.bootstrap.DispatcherHandler;
import io.esastack.restlight.server.config.ServerOptions;
import io.esastack.restlight.server.route.RouteRegistry;
import io.esastack.restlight.server.schedule.Scheduler;

import java.util.Map;
import java.util.Optional;

/**
 * ServerDeployContext is a container that holds the contexts of a Restlight such as {@link BaseRestlightServer}, {@link
 * Restlite}. Some of the methods in this interface will return an {@link Optional} value which means this value maybe a
 * {@code null} or this value may be instantiate later.
 *
 * @param <O> type of options
 */
public interface ServerDeployContext<O extends ServerOptions> extends Attributes {

    /**
     * Name of the Restlight server.
     *
     * @return name
     */
    String name();

    /**
     * Returns options of current server.
     *
     * @return options
     */
    O options();

    /**
     * Gets schedulers map who's key is {@link Scheduler#name()}.
     *
     * @return schedulers
     */
    Map<String, Scheduler> schedulers();

    /**
     * Gets the instance of {@link RouteRegistry}. It should be instantiate when server is about to starting and
     * initializing.
     *
     * @return optional value
     */
    Optional<RouteRegistry> routeRegistry();

    /**
     * Gets the instance of {@link DispatcherHandler}. It should be instantiate when server is about to starting and
     * initializing.
     *
     * @return optional value
     */
    Optional<DispatcherHandler> dispatcherHandler();

}
