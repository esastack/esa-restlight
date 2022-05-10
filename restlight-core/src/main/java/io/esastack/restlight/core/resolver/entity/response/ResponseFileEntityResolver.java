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
package io.esastack.restlight.core.resolver.entity.response;

import esa.commons.Result;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.context.ResponseEntity;
import io.esastack.restlight.core.context.ResponseEntityChannel;
import io.esastack.restlight.core.util.ResponseEntityUtils;
import io.esastack.restlight.core.context.RequestContext;

import java.io.File;

public class ResponseFileEntityResolver implements ResponseEntityResolverAdapter {

    @Override
    public Result<Void, Void> writeTo(ResponseEntity entity,
                                      ResponseEntityChannel channel,
                                      RequestContext context) throws Exception {
        if (!ResponseEntityUtils.isAssignableFrom(entity, File.class)) {
            return Result.err();
        }
        channel.end((File) entity.response().entity());
        return Result.ok();
    }

    @Override
    public boolean alsoApplyWhenMissingHandler() {
        return true;
    }

    @Override
    public boolean supports(HandlerMethod method) {
        return true;
    }

    @Override
    public int getOrder() {
        return 500;
    }

}

