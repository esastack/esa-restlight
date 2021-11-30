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

import java.util.Set;

/**
 * You can use this {@link Handlers} to get the view of current registered classes and singletons. Be note that,
 * when a class or singleton is added or removed by {@link HandlerRegistry} dynamically, you can know the modification
 * by {@link #getClasses()} or {@link #getSingletons()}.
 */
public interface Handlers {

    /**
     * Obtains all classes which have been registered.
     *
     * @return  immutable classes, which must not be {@code null}.
     */
    Set<Class<?>> getClasses();

    /**
     * Obtains all singletons which have been registered.
     *
     * @return  immutable singletons, which must not be {@code null}.
     */
    Set<Object> getSingletons();

}

