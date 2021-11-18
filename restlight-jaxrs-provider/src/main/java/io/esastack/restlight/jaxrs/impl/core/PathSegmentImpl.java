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
package io.esastack.restlight.jaxrs.impl.core;

import esa.commons.Checks;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;

import java.util.Arrays;
import java.util.Collections;

public class PathSegmentImpl implements PathSegment {

    private final String segment;
    private String path;
    private MultivaluedMap<String, String> params;

    public PathSegmentImpl(String segment) {
        Checks.checkNotEmptyArg(segment, "segment");
        this.segment = segment;
    }

    @Override
    public String getPath() {
        if (path == null) {
            path = segment.substring(0, segment.indexOf(";"));
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
        }
        return path;
    }

    @Override
    public MultivaluedMap<String, String> getMatrixParameters() {
        if (params == null) {
            String[] subSegments = segment.split(";");
            MultivaluedMap<String, String> values = new MultivaluedHashMap<>(subSegments.length - 1);

            // NOTE: the subSegments[0] is extracted to path
            for (int i = 1; i < subSegments.length; i++) {
                String[] nav = subSegments[i].split("=");
                String[] vs = nav[1].split(",");
                values.put(nav[0], Collections.unmodifiableList(Arrays.asList(vs)));
            }
            params = new UnmodifiableMultivaluedMap<>(values);
        }

        return params;
    }
}
