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
        this.uriBuilder = UriBuilder.fromUri(link.getUri());
        this.params.clear();
        this.params.putAll(link.getParams());
        return this;
    }

    @Override
    public Link.Builder link(String link) {
        Link l = LinkImpl.valueOf(link);
        return link(l);
    }

    @Override
    public Link.Builder uri(URI uri) {
        Checks.checkNotNull(uri, "uri");
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
        Checks.checkNotNull(uri, "uri");
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
        Checks.checkNotNull(uriBuilder, "uriBuilder");
        this.uriBuilder = uriBuilder.clone();
        return this;
    }

    @Override
    public Link.Builder rel(String rel) {
        if (rel == null) {
            throw new IllegalArgumentException("rel is null");
        }
        String rels = this.params.get(REL);
        param(REL, rels == null ? rel : rels + " " + rel);
        return null;
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
        param(Link.TITLE, type);
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
        if (values == null) {
            throw new IllegalArgumentException("values is null");
        }
        URI target;
        if (uriBuilder == null) {
            target = baseUri;
        } else {
            target = this.uriBuilder.build(values);
        }
        if (!target.isAbsolute() && baseUri != null) {
            target = baseUri.resolve(target);
        }
        return new LinkImpl(target, this.params);
    }

    @Override
    public Link buildRelativized(URI uri, Object... values) {
        if (uri == null) {
            throw new IllegalArgumentException("uri is null");
        }
        if (values == null) {
            throw new IllegalArgumentException("values is null");
        }
        URI target = uriBuilder.build(values);
        if (baseUri != null) {
            return new LinkImpl(uri.relativize(baseUri.resolve(target)), this.params);
        } else {
            return new LinkImpl(uri.relativize(target), this.params);
        }
    }
}

