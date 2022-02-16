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
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static jakarta.ws.rs.core.Link.REL;

public class LinkBuilderImpl implements Link.Builder {

    private final Map<String, String> params = new HashMap<>();
    private UriBuilder uriBuilder;
    private URI baseUri;

    @Override
    public Link.Builder link(Link link) {
        this.uriBuilder = UriBuilder.fromLink(link);
        this.params.putAll(link.getParams());
        return this;
    }

    @Override
    public Link.Builder link(String link) {
        Link l = RuntimeDelegate.getInstance().createHeaderDelegate(Link.class).fromString(link);
        if (l != null) {
            this.uriBuilder = l.getUriBuilder();
            this.params.putAll(l.getParams());
        }
        return this;
    }

    @Override
    public Link.Builder uri(URI uri) {
        Checks.checkArg(uri != null, "uri is null");
        this.uriBuilder = UriBuilder.fromUri(uri);
        return this;
    }

    @Override
    public Link.Builder uri(String uri) {
        this.uriBuilder = UriBuilder.fromUri(uri);
        return this;
    }

    @Override
    public Link.Builder baseUri(URI uri) {
        this.baseUri = uri;
        return this;
    }

    @Override
    public Link.Builder baseUri(String uri) {
        this.baseUri = URI.create(uri);
        return this;
    }

    @Override
    public Link.Builder uriBuilder(UriBuilder uriBuilder) {
        Checks.checkArg(uriBuilder != null, "uriBuilder is null");
        this.uriBuilder = uriBuilder;
        return this;
    }

    @Override
    public Link.Builder rel(String rel) {
        if (rel == null) {
            throw new IllegalArgumentException("rel is null");
        }
        String rels = this.params.get(REL);
        param(REL, rels == null ? rel : rels + " " + rel);
        return this;
    }

    @Override
    public Link.Builder title(String title) {
        if (title == null) {
            throw new IllegalArgumentException("title is null");
        }
        param(Link.TITLE, title);
        return this;
    }

    @Override
    public Link.Builder type(String type) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        param(Link.TYPE, type);
        return this;
    }

    @Override
    public Link.Builder param(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }
        this.params.put(name, value);
        return this;
    }

    @Override
    public Link build(Object... values) {
        return new LinkImpl(getResolvedUri(values), new HashMap<>(this.params));
    }

    @Override
    public Link buildRelativized(URI uri, Object... values) {
        URI resolved = getResolvedUri(values);
        return new LinkImpl(UriInfoImpl.relativize(uri, resolved), new HashMap<>(params));
    }

    private URI getResolvedUri(Object... values) {
        URI uri = uriBuilder.build(values);

        if (baseUri != null) {
            UriBuilder linkUriBuilder = UriBuilder.fromUri(baseUri);
            String scheme = uri.getScheme();
            if (scheme != null && scheme.startsWith("http")) {
                if (!uri.isAbsolute()) {
                    return linkUriBuilder.build().resolve(uri);
                } else {
                    return uri;
                }
            } else {
                String theUri = uri.toString();
                return linkUriBuilder.path(theUri).build();
            }
        } else {
            return uri;
        }
    }
}

