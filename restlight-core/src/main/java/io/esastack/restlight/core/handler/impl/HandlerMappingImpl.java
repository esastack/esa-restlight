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
package io.esastack.restlight.core.handler.impl;

import esa.commons.Checks;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.RouteMethodInfo;
import io.esastack.restlight.core.route.Mapping;

import java.util.Objects;
import java.util.Optional;

public class HandlerMappingImpl implements HandlerMapping {

    private final Object bean;
    private final Mapping mapping;
    private final RouteMethodInfo methodInfo;
    private final HandlerMapping parent;

    private String strVal;

    public HandlerMappingImpl(Mapping mapping,
                              RouteMethodInfo methodInfo,
                              Object bean,
                              HandlerMapping parent) {
        Checks.checkNotNull(mapping, "mapping");
        Checks.checkNotNull(methodInfo, "methodInfo");
        this.mapping = mapping;
        this.methodInfo = methodInfo;
        this.bean = bean;
        this.parent = parent;
    }

    @Override
    public Mapping mapping() {
        return mapping;
    }

    @Override
    public RouteMethodInfo methodInfo() {
        return methodInfo;
    }

    @Override
    public Optional<Object> bean() {
        return Optional.ofNullable(bean);
    }

    @Override
    public Optional<HandlerMapping> parent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HandlerMappingImpl that = (HandlerMappingImpl) o;
        return Objects.equals(bean, that.bean) && Objects.equals(mapping, that.mapping)
                && Objects.equals(methodInfo, that.methodInfo)
                && Objects.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bean, mapping, methodInfo, parent);
    }

    @Override
    public String toString() {
        if (strVal == null) {
            final StringBuilder sb = new StringBuilder("HandlerMappingImpl{");
            sb.append("mapping=").append(mapping);
            sb.append(", methodInfo=").append(methodInfo);
            parent().ifPresent(p -> sb.append(", parent=").append(p));
            bean().ifPresent(b -> sb.append(", bean=").append(b));
            sb.append('}');
            strVal = sb.toString();
        }
        return strVal;
    }
}
