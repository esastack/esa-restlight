/*
 * Copyright 2022 OPPO ESA Stack Project
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
package io.esastack.restlight.jaxrs.resolver;

import esa.commons.ClassUtils;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.HandlerMethodImpl;

import java.util.Map;
import java.util.stream.Collectors;

public class ResolverUtils {

    private ResolverUtils() {

    }

    public static Map<String, HandlerMethod> extractHandlerMethods(Object target) {
        return ClassUtils.userDeclaredMethods(target.getClass())
                .stream()
                .map(method -> HandlerMethodImpl.of(ClassUtils.getUserType(target), method))
                .collect(Collectors.toMap(h -> h.method().getName(), hm -> hm));
    }

}

