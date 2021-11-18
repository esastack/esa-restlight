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
package io.esastack.restlight.server.route.impl;

import esa.commons.Checks;
import io.esastack.restlight.server.route.ExecutionHandlerFactory;
import io.esastack.restlight.server.route.Mapping;
import io.esastack.restlight.server.route.Route;
import io.esastack.restlight.server.schedule.Scheduler;

import java.util.Optional;

public class RouteImpl implements Route {

    protected final Scheduler scheduler;
    protected final Mapping mapping;
    protected final Object handler;
    protected final ExecutionHandlerFactory executionFactory;
    private String strVal;

    public RouteImpl(Scheduler scheduler,
                     Mapping mapping,
                     ExecutionHandlerFactory executionFactory,
                     Object handler) {
        this.scheduler = scheduler;
        this.mapping = (mapping == null ? Mapping.mapping() : mapping);
        this.executionFactory = executionFactory;
        this.handler = handler;
    }

    public RouteImpl(Route underlying) {
        this(underlying.scheduler(), underlying.mapping(), underlying.executionFactory(),
                underlying.handler().orElse(null));
    }

    public RouteImpl executionFactory(ExecutionHandlerFactory executionFactory) {
        Checks.checkNotNull(executionFactory, "executionFactory");
        return new RouteImpl(scheduler, mapping, executionFactory, this.handler);
    }

    public RouteImpl mapping(Mapping mapping) {
        Checks.checkNotNull(mapping, "mapping");
        return new RouteImpl(scheduler, mapping, executionFactory, handler);
    }

    public RouteImpl scheduler(Scheduler scheduler) {
        Checks.checkNotNull(scheduler, "scheduler");
        return new RouteImpl(scheduler, mapping, executionFactory, this.handler);
    }

    public RouteImpl handler(Object handler) {
        Checks.checkNotNull(handler, "handler");
        return new RouteImpl(scheduler, mapping, executionFactory, handler);
    }

    @Override
    public ExecutionHandlerFactory executionFactory() {
        return executionFactory;
    }

    @Override
    public Mapping mapping() {
        return mapping;
    }

    @Override
    public Scheduler scheduler() {
        return scheduler;
    }

    @Override
    public Optional<Object> handler() {
        return Optional.ofNullable(handler);
    }

    @Override
    public String toString() {
        if (strVal == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Route(mapping(").append(mapping).append(")");
            if (scheduler != null) {
                sb.append(",scheduler(").append(scheduler.name()).append(')');
            }
            handler().ifPresent(h -> sb.append(",handler(").append(handler).append(")"));
            sb.append(")");
            strVal = sb.toString();
        }
        return strVal;
    }

}

