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
package esa.restlight.core.handler.locate;

import esa.restlight.server.route.Mapping;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * This is used to locate a {@link Mapping} from a {@link Method} if necessary.
 */
public interface MappingLocator {

    /**
     * Locates an optional instance of {@link Mapping} by given target {@link Method} and type if necessary.
     *
     * @param userType type of given method
     * @param method   target method
     *
     * @return mapping
     */
    Optional<Mapping> getMapping(Class<?> userType, Method method);

}
