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
package io.esastack.restlight.core.resolver.ret.entity;

import esa.commons.Checks;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.ResponseEntity;
import io.esastack.restlight.core.context.ResponseEntityChannel;

public class ResponseEntityResolverContextImpl implements ResponseEntityResolverContext {

    private final RequestContext context;
    private final ResponseEntity entity;
    private final ResponseEntityChannel channel;

    public ResponseEntityResolverContextImpl(RequestContext context,
                                             ResponseEntity entity,
                                             ResponseEntityChannel channel) {
        Checks.checkNotNull(entity, "entity");
        Checks.checkNotNull(channel, "channel");
        this.channel = channel;
        this.context = context;
        this.entity = entity;
    }

    @Override
    public RequestContext requestContext() {
        return context;
    }

    @Override
    public ResponseEntity httpEntity() {
        return entity;
    }

    @Override
    public ResponseEntityChannel channel() {
        return channel;
    }
}

