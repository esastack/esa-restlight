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
import esa.commons.StringUtils;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LinkImpl extends Link {

    private final URI uri;
    private final Map<String, String> params;
    private String strVal;

    public LinkImpl(URI uri, Map<String, String> params) {
        Checks.checkNotNull(uri, "uri");
        Checks.checkNotNull(params, "params");
        this.uri = uri;
        this.params = params.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(params);
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public UriBuilder getUriBuilder() {
        return UriBuilder.fromUri(uri);
    }

    @Override
    public String getRel() {
        return params.get(REL);
    }

    @Override
    public List<String> getRels() {
        final String rels = params.get(REL);
        return StringUtils.isEmpty(rels) ? Collections.emptyList() : Arrays.asList(rels.split(" +"));
    }

    @Override
    public String getTitle() {
        return params.get(TITLE);
    }

    @Override
    public String getType() {
        return params.get(TYPE);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LinkImpl link = (LinkImpl) o;
        return Objects.equals(uri, link.uri) && Objects.equals(params, link.params)
                && Objects.equals(strVal, link.strVal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, params, strVal);
    }

    @Override
    public String toString() {
        if (strVal != null) {
            return strVal;
        }
        RuntimeDelegate.HeaderDelegate<Link> delegate = RuntimeDelegate.getInstance()
                .createHeaderDelegate(Link.class);
        if (delegate != null) {
            strVal = delegate.toString(this);
        } else {
            final StringBuilder sb = new StringBuilder("LinkImpl{");
            sb.append("uri=").append(uri);
            sb.append(", params=").append(params);
            sb.append(", strVal='").append(strVal).append('\'');
            sb.append('}');
            strVal = sb.toString();
        }
        return strVal;
    }
}

