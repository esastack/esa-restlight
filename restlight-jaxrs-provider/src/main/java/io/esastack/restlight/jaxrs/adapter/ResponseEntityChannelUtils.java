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
package io.esastack.restlight.jaxrs.adapter;

import io.esastack.restlight.core.resolver.ResponseEntityChannel;
import io.esastack.restlight.core.resolver.ResponseEntityChannelImpl;
import io.esastack.restlight.server.context.RequestContext;

final class ResponseEntityChannelUtils extends ResponseEntityChannelImpl {

    static ResponseEntityChannel get(RequestContext context) {
        ResponseEntityChannel channel = context.attr(RESPONSE_ENTITY_CHANNEL).get();
        if (channel != null) {
            return channel;
        }
        return new ResponseEntityChannelUtils(context);
    }

    private ResponseEntityChannelUtils(RequestContext context) {
        super(context);
    }
}

