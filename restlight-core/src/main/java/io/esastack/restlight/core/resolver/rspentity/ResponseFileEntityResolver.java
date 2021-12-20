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
package io.esastack.restlight.core.resolver.rspentity;

import io.esastack.restlight.core.resolver.HandledValue;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.resolver.ResponseEntityChannel;
import io.esastack.restlight.core.resolver.ResponseEntityResolver;
import io.esastack.restlight.core.util.ResponseEntityUtils;
import io.esastack.restlight.server.context.RequestContext;

import java.io.File;

public class ResponseFileEntityResolver implements ResponseEntityResolver {

    @Override
    public HandledValue<Void> writeTo(ResponseEntity entity,
                                      ResponseEntityChannel channel,
                                      RequestContext context) throws Exception {
        if (ResponseEntityUtils.isAssignableFrom(entity, File.class)) {
            return HandledValue.failed();
        }
        channel.writeThenEnd((File) entity.response().entity());
        return HandledValue.succeed(null);
    }

}

