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
package io.esastack.restlight.core.handler.locate;

import esa.commons.Checks;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.server.route.Mapping;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;

/**
 * Shows multiple {@link MappingLocator}s as a single {@link MappingLocator}.
 */
public class CompositeMappingLocator implements MappingLocator {

    private final Collection<? extends MappingLocator> locators;

    private CompositeMappingLocator(Collection<? extends MappingLocator> locators) {
        Checks.checkNotEmptyArg(locators, "locators");
        this.locators = locators;
    }

    public static MappingLocator wrapIfNecessary(Collection<? extends MappingLocator> locators) {
        if (locators.isEmpty()) {
            return null;
        }
        if (locators.size() == 1) {
            return locators.iterator().next();
        } else {
            return new CompositeMappingLocator(locators);
        }
    }

    @Override
    public Optional<Mapping> getMapping(HandlerMapping parent, Class<?> userType, Method method) {
        for (MappingLocator locator : locators) {
            Optional<Mapping> mapping = locator.getMapping(parent, userType, method);
            if (mapping.isPresent()) {
                return mapping;
            }
        }
        return Optional.empty();
    }
}
