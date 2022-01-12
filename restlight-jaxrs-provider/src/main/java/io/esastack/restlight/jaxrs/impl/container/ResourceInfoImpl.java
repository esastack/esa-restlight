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
package io.esastack.restlight.jaxrs.impl.container;

import esa.commons.Checks;
import jakarta.ws.rs.container.ResourceInfo;

import java.lang.reflect.Method;

public class ResourceInfoImpl implements ResourceInfo {

    private final Class<?> userType;
    private final Method method;

    public ResourceInfoImpl(Class<?> userType, Method method) {
        Checks.checkNotNull(userType, "userType");
        Checks.checkNotNull(method, "method");
        this.userType = userType;
        this.method = method;
    }

    @Override
    public Method getResourceMethod() {
        return method;
    }

    @Override
    public Class<?> getResourceClass() {
        return userType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ResourceInfoImpl{");
        sb.append("userType=").append(userType);
        sb.append(", method=").append(method);
        sb.append('}');
        return sb.toString();
    }
}

