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
import io.esastack.restlight.core.resolver.ret.ReturnValueResolverContext;

/**
 * {@link ResolverContext} provide work context for the {@link Resolver} and {@link ResolverExecutor},
 * Here are three types context: {@link io.esastack.restlight.core.resolver.param.ParamResolverContext},
 *    {@link ReturnValueResolverContext} and {@link io.esastack.restlight.core.resolver.context.ContextResolverContext}
 *    : mainly for provide the context for real resolvers.
 */
@Internal
public interface ResolverContext {
}
