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
package io.esastack.restlight.core.resolver;

import esa.commons.Result;
import io.esastack.restlight.server.context.RequestContext;

/**
 * This resolver will deserialize the {@link RequestEntity} to an instance.
 */
public interface RequestEntityResolver extends Resolver {

    /**
     * Deserialize the given {@code entity} to result.
     *
     * @param entity  entity
     * @param context context
     * @return resolved value, which must not be null.
     * @throws Exception any exception
     */
    Result<?, Void> readFrom(RequestEntity entity, RequestContext context) throws Exception;

}

