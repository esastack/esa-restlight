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
import io.esastack.restlight.core.handler.RouteMethodInfo;

import java.util.Optional;

public class MatchedResource {

    private final RouteMethodInfo method;
    private final Object bean;

    MatchedResource(RouteMethodInfo method, Object bean) {
        Checks.checkNotNull(method, "method");
        this.method = method;
        this.bean = bean;
    }

    public RouteMethodInfo method() {
        return method;
    }

    public Optional<Object> bean() {
        return Optional.ofNullable(bean);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MatchedResource{");
        sb.append("method=").append(method);
        bean().ifPresent(b -> sb.append(", bean=").append(bean.getClass()));
        sb.append('}');
        return sb.toString();
    }
}

