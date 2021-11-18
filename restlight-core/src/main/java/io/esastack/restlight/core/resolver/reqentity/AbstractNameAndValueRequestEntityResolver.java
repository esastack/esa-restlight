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
package io.esastack.restlight.core.resolver.reqentity;

import io.esastack.httpserver.core.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandledValue;
import io.esastack.restlight.core.resolver.RequestEntity;
import io.esastack.restlight.core.resolver.RequestEntityResolver;
import io.esastack.restlight.core.resolver.nav.AbstractNameAndValueResolver;
import io.esastack.restlight.core.resolver.nav.NameAndValue;

public abstract class AbstractNameAndValueRequestEntityResolver extends AbstractNameAndValueResolver
        implements RequestEntityResolver {

    public AbstractNameAndValueRequestEntityResolver(Param param) {
        super(param);
    }

    public AbstractNameAndValueRequestEntityResolver(Param param, NameAndValue nav) {
        super(param, nav);
    }

    @Override
    public HandledValue<Object> readFrom(Param param, RequestEntity entity, RequestContext context) throws Exception {
        return readFrom0(nav.name, param, entity);
    }

    /**
     * Try to read object from the given {@link RequestEntity}.
     *
     * @param name      name
     * @param param     param
     * @param entity    request entity
     * @return          resolved value
     * @throws Exception    any exception
     */
    protected abstract HandledValue<Object> readFrom0(String name, Param param, RequestEntity entity) throws Exception;

}

