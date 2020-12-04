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
package esa.restlight.core.resolver.result;

import esa.commons.StringUtils;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.resolver.ReturnValueResolver;
import esa.restlight.core.serialize.Serializers;
import esa.restlight.core.util.MediaType;
import esa.restlight.server.route.predicate.ProducesPredicate;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.internal.InternalThreadLocalMap;

import java.util.Collections;
import java.util.List;

abstract class AbstractDetectableReturnValueResolver implements ReturnValueResolver {

    /**
     * Whether it is needed to serialize the return value by detecting type of the return value.
     */
    private final boolean detect;

    AbstractDetectableReturnValueResolver(boolean detect) {
        this.detect = detect;
    }

    @Override
    public byte[] resolve(Object returnValue,
                          AsyncRequest request,
                          AsyncResponse response) throws Exception {
        if (returnValue == null) {
            return null;
        }

        final List<MediaType> mediaTypes = getMediaTypes(request);
        if (detect) {
            // try to serialize simple type by return value instance.
            final MediaType mediaType = getMediaType(mediaTypes);
            byte[] ret = Serializers.serializeIfPossible(returnValue, response, mediaType);
            if (ret == null) {
                return resolve0(returnValue, mediaTypes, request, response);
            } else {
                return ret;
            }
        } else {
            return resolve0(returnValue, mediaTypes, request, response);
        }
    }

    static MediaType getMediaType(List<MediaType> mediaTypes) {
        final MediaType mediaType;
        if (mediaTypes.isEmpty()) {
            mediaType = null;
        } else {
            mediaType = mediaTypes.get(0);
        }
        return mediaType;
    }


    protected List<MediaType> getMediaTypes(AsyncRequest request) {
        List<MediaType> compatibleTypes =
                request.getUncheckedAttribute(ProducesPredicate.COMPATIBLE_MEDIA_TYPES);
        if (compatibleTypes == null) {
            String accept = request.getHeader(HttpHeaderNames.ACCEPT);
            if (!StringUtils.isEmpty(accept)) {
                List<MediaType> ret = InternalThreadLocalMap.get().arrayList();
                MediaType.valuesOf(accept, ret);
                return ret;
            }
        } else {
            return compatibleTypes;
        }
        return Collections.emptyList();
    }

    protected abstract byte[] resolve0(Object returnValue,
                                       List<MediaType> mediaTypes,
                                       AsyncRequest request,
                                       AsyncResponse response) throws Exception;
}
