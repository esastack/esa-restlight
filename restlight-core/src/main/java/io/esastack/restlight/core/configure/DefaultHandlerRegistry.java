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
package io.esastack.restlight.core.configure;

import esa.commons.Checks;
import esa.commons.ClassUtils;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DefaultHandlerRegistry extends HandlerRegistryImpl {

    private final HandlersImpl handlers;

    public DefaultHandlerRegistry(DeployContext<? extends RestlightOptions> context, HandlersImpl handlers) {
        super(context);
        Checks.checkNotNull(handlers, "handlers");
        this.handlers = handlers;
    }

    @Override
    public void addHandlers(Collection<Object> handlers) {
        if (handlers != null && !handlers.isEmpty()) {
            this.handlers.singletons.addAll(handlers);
        }
        super.addHandlers(handlers);
    }

    @Override
    public void addHandlers(Collection<Class<?>> classes, boolean singleton) {
        if (classes != null && !classes.isEmpty() && !singleton) {
            this.handlers.classes.addAll(classes);
        }
        super.addHandlers(classes, singleton);
    }

    @Override
    public void removeHandlers(Collection<Object> handlers) {
        super.removeHandlers(handlers);
        if (handlers != null && !handlers.isEmpty()) {
            Set<Class<?>> removableClasses = new HashSet<>();
            Set<Object> removableSingletons = new HashSet<>();
            for (Object handler : handlers) {
                for (Class<?> clazz : this.handlers.classes) {
                    if (clazz.equals(handler)) {
                        removableClasses.add(clazz);
                    }
                }

                Class<?> userType = ClassUtils.getUserType(handler);
                for (Object singleton : this.handlers.singletons) {
                    if (singleton.equals(handler) || ClassUtils.getUserType(singleton).equals(userType)) {
                        removableSingletons.add(singleton);
                    }
                }
            }

            this.handlers.singletons.removeAll(removableSingletons);
            this.handlers.classes.removeAll(removableClasses);
        }
    }

}

