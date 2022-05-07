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
package io.esastack.restlight.core.handler.method;

import esa.commons.annotation.Internal;
import esa.commons.spi.SPI;
import io.esastack.restlight.core.resolver.context.ContextResolver;
import io.esastack.restlight.core.resolver.param.ParamResolver;

/**
 * Generally speaking, not all {@link Param}s are expected to resolved by {@link ParamResolver}
 * or {@link ContextResolver}, such as a field which has the initial value when declaring it. In this case, we
 * shouldn't try to bind a resolver with. And this interface is designed to test whether the given {@link Param}
 * should be resolved by custom resolver.
 * <p>
 * !NOTE: This is only used internally.
 */
@Internal
@SPI
@FunctionalInterface
public interface ResolvableParamPredicate {

    /**
     * Whether the given {@code param} should be resolved by custom resolvers or not.
     *
     * @param param param
     * @return {@code true} when the param should be resolved by custom resolvers, otherwise {@code false}.
     */
    boolean test(Param param);

}

