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

package io.esastack.restlight.core.resolver;

import esa.commons.annotation.Internal;

/**
 * {@link Resolver} provide the ability to resolving the context to a target object.
 * mainly provide two types implement:
 * 1.advised resolver to bridge the work flow and {@link ResolverExecutor}
 * 2.real resolver to resolve to the target object.
 *
 * @param <C> context
 */
@Internal
public interface Resolver<C extends ResolverContext> {

    /**
     * resolve the context to a target object.
     */
    Object resolve(C context) throws Exception;
}
