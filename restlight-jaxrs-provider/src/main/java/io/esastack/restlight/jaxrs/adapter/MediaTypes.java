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

import io.esastack.restlight.jaxrs.util.MediaTypeUtils;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

final class MediaTypes {

    static io.esastack.commons.net.http.MediaType[] covert(List<MediaType> target) {
        if (target == null || target.isEmpty()) {
            return new io.esastack.commons.net.http.MediaType[0];
        }
        io.esastack.commons.net.http.MediaType[] mediaTypes =
                new io.esastack.commons.net.http.MediaType[target.size()];
        int i = 0;
        for (MediaType mediaType : target) {
            mediaTypes[i++] = (MediaTypeUtils.convert(mediaType));
        }
        return mediaTypes;
    }

    static boolean isCompatibleWith(io.esastack.commons.net.http.MediaType[] mediaTypes,
                                    List<io.esastack.commons.net.http.MediaType> tests) {
        if (mediaTypes.length == 0) {
            return true;
        }
        for (io.esastack.commons.net.http.MediaType target : mediaTypes) {
            for (io.esastack.commons.net.http.MediaType current : tests) {
                if (current.isCompatibleWith(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    private MediaTypes() {
    }

}

