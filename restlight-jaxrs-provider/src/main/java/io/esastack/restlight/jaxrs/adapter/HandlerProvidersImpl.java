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
package io.esastack.restlight.jaxrs.adapter;

import io.esastack.restlight.core.method.HandlerMethod;
import jakarta.ws.rs.container.ContainerResponseFilter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class HandlerProvidersImpl implements HandlerProviders {

    private final Map<Method, ContainerResponseFilter[]> responseFilters = new HashMap<>(64);

    public HandlerProvidersImpl(ContainerResponseFilter[] globalDefault) {
        this.responseFilters.put(null, globalDefault);
    }

    @Override
    public ContainerResponseFilter[] getResponseFilters(HandlerMethod method) {
        if (method == null) {
            return responseFilters.get(null);
        } else {
            return responseFilters.get(method.method());
        }
    }

    void addResponseFilters(HandlerMethod method, ContainerResponseFilter[] filters) {
        this.responseFilters.put(method.method(), filters);
    }

}

