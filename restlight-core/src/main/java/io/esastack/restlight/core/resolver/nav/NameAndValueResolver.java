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
package io.esastack.restlight.core.resolver.nav;

import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.server.context.RequestContext;

public interface NameAndValueResolver {

    /**
     * Resolves method parameter into an argument value.
     *
     * @param name name of parameter
     * @param ctx  context of request
     * @return resolved
     */
    Object resolve(String name, RequestContext ctx);

    /**
     * Create an instance of {@link NameAndValue} for the parameter.
     *
     * @param param parameter
     * @return name and value
     */
    NameAndValue<?> createNameAndValue(Param param);
}
