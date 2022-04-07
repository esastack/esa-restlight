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
package io.esastack.restlight.core.handler.impl;

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.resolver.Resolver;
import io.esastack.restlight.server.context.RequestContext;

interface ResolverWrap extends Resolver {

    /**
     * Resolves the param by given {@link DeployContext} and {@link RequestContext}.
     *
     * @param deployContext deploy context
     * @param context       context
     * @return resolved value
     * @throws Exception exception
     */
    Object resolve(DeployContext deployContext, RequestContext context) throws Exception;

}
