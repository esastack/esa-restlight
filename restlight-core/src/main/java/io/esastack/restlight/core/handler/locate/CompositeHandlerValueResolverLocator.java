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
package io.esastack.restlight.core.handler.locate;

import esa.commons.Checks;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.HandlerValueResolver;

import java.util.Collection;
import java.util.Optional;

public class CompositeHandlerValueResolverLocator implements HandlerValueResolverLocator {

    private final Collection<? extends HandlerValueResolverLocator> locators;

    private CompositeHandlerValueResolverLocator(Collection<? extends HandlerValueResolverLocator> locators) {
        Checks.checkNotEmptyArg(locators, "locators");
        this.locators = locators;
    }

    public static HandlerValueResolverLocator wrapIfNecessary(Collection<? extends HandlerValueResolverLocator>
                                                                      locators) {
        if (locators.isEmpty()) {
            return null;
        }
        if (locators.size() == 1) {
            return locators.iterator().next();
        } else {
            return new CompositeHandlerValueResolverLocator(locators);
        }
    }

    @Override
    public Optional<HandlerValueResolver> getHandlerValueResolver(HandlerMapping mapping) {
        Optional<HandlerValueResolver> handlerValueResolver = Optional.empty();
        for (HandlerValueResolverLocator locator : locators) {
            handlerValueResolver = locator.getHandlerValueResolver(mapping);
            if (handlerValueResolver.isPresent()) {
                break;
            }
        }
        return handlerValueResolver;
    }

}

