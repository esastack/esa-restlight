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

import io.esastack.restlight.jaxrs.configure.Applications;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Variant;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * This class is designed as non-thread safe.
 */
public class ResponseBuilderImpl extends Response.ResponseBuilder {

    private static final URI baseUri = Applications.baseUri();

    private final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

    private Response.Status status;
    private String reasonPhrase;
    private Object entity;
    private Annotation[] annotations;

    public ResponseBuilderImpl() {
    }

    private ResponseBuilderImpl(ResponseBuilderImpl from) {
        from.headers.forEach(this.headers::addAll);
        this.status = from.status;
        this.reasonPhrase = from.reasonPhrase;
        this.entity = from.entity;
        this.annotations = from.annotations;
    }

    @Override
    public Response build() {
        if (status == null) {
            if (entity == null) {
                status = Response.Status.NO_CONTENT;
            } else {
                status = Response.Status.OK;
            }
        }
        Response response = new ResponseImpl(new ResponseBuilderImpl(this));
        reset();
        return response;
    }

    @Override
    public Response.ResponseBuilder clone() {
        return new ResponseBuilderImpl(this);
    }

    @Override
    public Response.ResponseBuilder status(int status) {
        if (status < 100 || status > 599) {
            throw new IllegalArgumentException("illegal status: " + status + " (expected:[100, 600))");
        }
        this.status = Response.Status.fromStatusCode(status);
        return this;
    }

    @Override
    public Response.ResponseBuilder status(int status, String reasonPhrase) {
        if (status < 100 || status > 599) {
            throw new IllegalArgumentException("illegal status: " + status + " (expected:[100, 600))");
        }
        this.status = Response.Status.fromStatusCode(status);
        this.reasonPhrase = reasonPhrase;
        return this;
    }

    @Override
    public Response.ResponseBuilder entity(Object entity) {
        this.entity = entity;
        return this;
    }

    @Override
    public Response.ResponseBuilder entity(Object entity, Annotation[] annotations) {
        this.entity = entity;
        this.annotations = annotations;
        return this;
    }

    @Override
    public Response.ResponseBuilder allow(String... methods) {
        Set<String> methodsSet = null;
        if (methods != null) {
            methodsSet = new HashSet<>();
            for (String method : methods) {
                methodsSet.add(method.toUpperCase());
            }
        }

        return setHeader(HttpHeaders.ALLOW, methodsSet);
    }

    @Override
    public Response.ResponseBuilder allow(Set<String> methods) {
        final Set<Object> methodsSet;
        if (methods == null) {
            methodsSet = null;
        } else {
            methodsSet = new HashSet<>();
            methods.forEach(m -> methodsSet.add(m.toUpperCase()));
        }

        return setHeader(HttpHeaders.ALLOW, methodsSet);
    }

    @Override
    public Response.ResponseBuilder cacheControl(CacheControl cacheControl) {
        return setHeader(HttpHeaders.CACHE_CONTROL, cacheControl);
    }

    @Override
    public Response.ResponseBuilder encoding(String encoding) {
        return setHeader(HttpHeaders.CONTENT_ENCODING, encoding);
    }

    @Override
    public Response.ResponseBuilder header(String name, Object value) {
        return addHeader(name, value);
    }

    @Override
    public Response.ResponseBuilder replaceAll(MultivaluedMap<String, Object> headers) {
        this.headers.clear();
        if (headers == null) {
            return this;
        } else {
            headers.forEach(this.headers::put);
        }

        return this;
    }

    @Override
    public Response.ResponseBuilder language(String language) {
        return setHeader(HttpHeaders.CONTENT_LANGUAGE, language);
    }

    @Override
    public Response.ResponseBuilder language(Locale language) {
        return setHeader(HttpHeaders.CONTENT_LANGUAGE, language);
    }

    @Override
    public Response.ResponseBuilder type(MediaType type) {
        return setHeader(HttpHeaders.CONTENT_TYPE, type);
    }

    @Override
    public Response.ResponseBuilder type(String type) {
        return setHeader(HttpHeaders.CONTENT_TYPE, type);
    }

    @Override
    public Response.ResponseBuilder variant(Variant variant) {
        if (variant == null) {
            setHeader(HttpHeaders.CONTENT_ENCODING, null);
            setHeader(HttpHeaders.CONTENT_LANGUAGE, null);
            return setHeader(HttpHeaders.CONTENT_TYPE, null);
        } else {
            setHeader(HttpHeaders.CONTENT_ENCODING, variant.getEncoding());
            setHeader(HttpHeaders.CONTENT_LANGUAGE, variant.getLanguage());
            return setHeader(HttpHeaders.CONTENT_TYPE, variant.getMediaType());
        }
    }

    @Override
    public Response.ResponseBuilder contentLocation(URI location) {
        return setHeader(HttpHeaders.CONTENT_LOCATION, location);
    }

    @Override
    public Response.ResponseBuilder cookie(NewCookie... cookies) {
        List<Object> cookiesList = null;
        if (cookies != null) {
            cookiesList = new LinkedList<>();
            Collections.addAll(cookiesList, cookies);
        }

        return addHeaders(HttpHeaders.SET_COOKIE, cookiesList);
    }

    @Override
    public Response.ResponseBuilder expires(Date expires) {
        return setHeader(HttpHeaders.EXPIRES, expires);
    }

    @Override
    public Response.ResponseBuilder lastModified(Date lastModified) {
        return setHeader(HttpHeaders.LAST_MODIFIED, lastModified);
    }

    @Override
    public Response.ResponseBuilder location(URI location) {
        if (location == null) {
            return setHeader(HttpHeaders.LOCATION, null);
        } else {
            if (location.isAbsolute()) {
                return setHeader(HttpHeaders.LOCATION, UriBuilder
                        .fromUri(baseUri)
                        .path(location.getRawPath())
                        .replaceQuery(location.getRawQuery())
                        .fragment(location.getRawFragment())
                        .build());
            } else {
                return setHeader(HttpHeaders.LOCATION, location);
            }
        }
    }

    @Override
    public Response.ResponseBuilder tag(EntityTag tag) {
        return setHeader(HttpHeaders.ETAG, tag);
    }

    @Override
    public Response.ResponseBuilder tag(String tag) {
        return tag(tag == null ? null : new EntityTag(tag));
    }

    @Override
    public Response.ResponseBuilder variants(Variant... variants) {
        List<Object> variantList = null;
        if (variants != null) {
            variantList = new LinkedList<>();
            Collections.addAll(variantList, variants);
        }

        return addHeaders(HttpHeaders.VARY, variantList);
    }

    @Override
    public Response.ResponseBuilder variants(List<Variant> variants) {
        return addHeaders(HttpHeaders.VARY, variants);
    }

    @Override
    public Response.ResponseBuilder links(Link... links) {
        List<Link> linksList = null;
        if (links != null) {
            linksList = new LinkedList<>();
            Collections.addAll(linksList, links);
        }

        return addHeaders(HttpHeaders.LINK, linksList);
    }

    @Override
    public Response.ResponseBuilder link(URI uri, String rel) {
        return addHeader(HttpHeaders.LINK, Link.fromUri(uri).rel(rel).build());
    }

    @Override
    public Response.ResponseBuilder link(String uri, String rel) {
        return addHeader(HttpHeaders.LINK, Link.fromUri(uri).rel(rel).build());
    }

    MultivaluedMap<String, Object> headers() {
        return headers;
    }

    Object entity() {
        return entity;
    }

    Response.Status status() {
        return status;
    }

    Annotation[] annotations() {
        return annotations;
    }

    String reasonPhrase() {
        return reasonPhrase;
    }

    private void reset() {
        this.headers.clear();
        this.entity = null;
        this.annotations = null;
        this.reasonPhrase = null;
        status(Response.Status.OK);
    }

    private Response.ResponseBuilder setHeader(String name, Object value) {
        if (value == null) {
            this.headers.remove(name);
        } else {
            this.headers.putSingle(name, value);
        }

        return this;
    }

    private Response.ResponseBuilder addHeader(String name, Object value) {
        if (value == null) {
            this.headers.remove(name);
        } else {
            this.headers.add(name, value);
        }
        return this;
    }

    private Response.ResponseBuilder addHeaders(String name, List<?> values) {
        if (values == null) {
            this.headers.remove(name);
        } else {
            this.headers.addAll(name, values);
        }
        return this;
    }
}
