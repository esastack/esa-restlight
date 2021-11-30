/*
 * Copyright 2020 OPPO ESA Stack Project
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
package io.esastack.restlight.core.serialize;

import io.esastack.restlight.core.resolver.HandledValue;
import io.esastack.restlight.core.resolver.RequestEntity;

public interface HttpRequestSerializer extends BaseHttpSerializer {

    /**
     * Deserialize the data from byte array to the object.
     *
     * @param entity    entity
     * @param <T>       generic type
     * @return  handled value
     * @throws Exception any exception
     */
    <T> HandledValue<T> deserialize(RequestEntity entity) throws Exception;

}
