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
package io.esastack.restlight.jaxrs.util;

import esa.commons.ClassUtils;
import esa.commons.collection.LinkedMultiValueMap;
import esa.commons.collection.MultiValueMap;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.restlight.jaxrs.impl.core.ResponseImpl;
import io.esastack.restlight.server.core.HttpResponse;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class RuntimeDelegateUtils {

    public static String toString(Object object) {
        if (object == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        RuntimeDelegate.HeaderDelegate<Object> delegate = (RuntimeDelegate.HeaderDelegate<Object>)
                RuntimeDelegate.getInstance().createHeaderDelegate(ClassUtils.getUserType(object));
        if (delegate != null) {
            return delegate.toString(object);
        }
        return object.toString();
    }

    public static void addHeadersToMap(HttpHeaders headers, MultivaluedMap<String, Object> values) {
        if (values == null || headers == null) {
            return;
        }
        for (String name : headers.names()) {
            values.addAll(name, new ArrayList<>(headers.getAll(name)));
        }
    }

    public static void addHeadersFromMap(HttpHeaders headers,
                                         MultivaluedMap<String, Object> values,
                                         boolean clearDest) {
        if (values == null || values.isEmpty()) {
            return;
        }
        if (clearDest) {
            headers.clear();
        }
        for (Map.Entry<String, List<Object>> entry : values.entrySet()) {
            List<Object> value = entry.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }
            headers.add(entry.getKey(), value.stream().map(RuntimeDelegateUtils::toString)
                    .collect(Collectors.toList()));
        }
    }

    public static void addMetadataToJakarta(HttpResponse from, ResponseImpl to) {
        if (from == null || to == null) {
            return;
        }

        // when the from.entity instanceof Response which means the to is generated from from.entity,
        // we should avoid endless loop setting.
        if (!(from.entity() instanceof Response)) {
            to.setEntity(from.entity());
            to.setStatus(from.status());
        }

        MultiValueMap<String, Object> headers = new LinkedMultiValueMap<>();
        for (String name : from.headers().names()) {
            headers.addAll(name, from.headers().getAll(name));
        }
        to.getHeaders().putAll(headers);
    }

    public static void addMetadataToNetty(ResponseImpl from, HttpResponse to, boolean clearSource) {
        if (from == null || to == null) {
            return;
        }
        to.status(from.getStatus());

        // when the from.entity instanceof Response which means the to is generated from from.entity,
        // we should avoid endless loop setting.
        if (!(to.entity() instanceof Response)) {
            to.entity(from.getEntity());
        }

        if (clearSource) {
            to.headers().clear();
        }
        MultivaluedMap<String, String> headers = from.getStringHeaders();
        for (String name : headers.keySet()) {
            to.headers().add(name, headers.get(name));
        }
    }

    public static <K, V1, V2> boolean equalsIgnoreValueOrder(MultivaluedMap<K, V1> m1, MultivaluedMap<K, V2> m2) {
        if (m2 == null) {
            return false;
        }
        if (m1 == m2) {
            return true;
        }

        if (m1.keySet().size() != m2.keySet().size()) {
            return false;
        }
        for (Map.Entry<K, List<V1>> e : m1.entrySet()) {
            List<V2> olist = m2.get(e.getKey());
            if (e.getValue().size() != olist.size()) {
                return false;
            }
            for (V1 v : e.getValue()) {
                if (!olist.contains(v)) {
                    return false;
                }
            }
        }
        return true;
    }

    private RuntimeDelegateUtils() {
    }
}

