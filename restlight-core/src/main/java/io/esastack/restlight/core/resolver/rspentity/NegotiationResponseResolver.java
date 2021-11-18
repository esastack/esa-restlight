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

import esa.commons.StringUtils;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.core.serialize.ProtoBufHttpBodySerializer;
import io.esastack.restlight.core.util.Constants;
import io.netty.util.internal.InternalThreadLocalMap;

import java.util.List;

public abstract class NegotiationResponseResolver extends FlexibleResponseEntityResolver {

    private final String paramName;

    protected NegotiationResponseResolver(String paramName, List<? extends HttpResponseSerializer> serializers) {
        super(serializers);
        this.paramName = StringUtils.nonEmptyOrElse(paramName, Constants.DEFAULT_NEGOTIATION_FORMAT_PARAMETER);
    }

    @Override
    protected List<MediaType> getMediaTypes(HttpRequest request) {
        // judge by parameter
        final String format = request.getParameter(paramName);
        if (Constants.NEGOTIATION_JSON_FORMAT.equals(format)) {
            List<MediaType> ret = InternalThreadLocalMap.get().arrayList(1);
            ret.add(MediaTypeUtil.APPLICATION_JSON);
            return ret;
        } else if (Constants.NEGOTIATION_PROTO_BUF_FORMAT.equals(format)) {
            List<MediaType> ret = InternalThreadLocalMap.get().arrayList(1);
            ret.add(ProtoBufHttpBodySerializer.PROTOBUF);
            return ret;
        } else {
            // fallback to default
            return super.getMediaTypes(request);
        }
    }
}

