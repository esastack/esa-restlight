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
package io.esastack.restlight.core.handler.impl;

import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.handler.HandlerContextProvider;
import io.esastack.restlight.core.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerContexts implements HandlerContextProvider {

    private final ConcurrentHashMap<Method, HandlerContext<?>> contexts = new ConcurrentHashMap<>();

    @Override
    public HandlerContext<? extends RestlightOptions> getContext(HandlerMethod method) {
        return this.contexts.get(method.method());
    }

    void addContext(HandlerMethod method, HandlerContext<? extends RestlightOptions> context) {
        this.contexts.putIfAbsent(method.method(), context);
    }

}

