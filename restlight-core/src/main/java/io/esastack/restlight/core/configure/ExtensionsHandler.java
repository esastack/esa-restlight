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

import esa.commons.annotation.Internal;
import io.esastack.restlight.core.Deployments;

import java.util.Collection;
import java.util.List;

/**
 * This {@link ExtensionsHandler} allows user to handle the custom extensions added
 * {@link Deployments#addExtensions(Collection)} and {@link Deployments#addExtension(Object)}.
 */
@Internal
@FunctionalInterface
public interface ExtensionsHandler {

    /**
     * Handles the given {@code extensions}.
     *
     * @param extensions unmodifiable extensions
     */
    void handle(List<Object> extensions);

}

