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
import io.esastack.httpserver.core.RequestContext;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.jaxrs.configure.RouteTracking;
import io.esastack.restlight.server.route.predicate.PatternsPredicate;
import io.esastack.restlight.server.util.LoggerUtils;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UriInfoImpl implements UriInfo {

    private final RequestContext context;
    private URI baseUri;

    public UriInfoImpl(URI baseUri, RequestContext context) {
        Checks.checkNotNull(baseUri, "baseUri");
        Checks.checkNotNull(context, "context");
        this.baseUri = baseUri;
        this.context = context;
    }

    @Override
    public String getPath() {
        return getPath(true);
    }

    @Override
    public String getPath(boolean decode) {
        return context.request().path();
    }

    @Override
    public List<PathSegment> getPathSegments() {
        return getPathSegments(true);
    }

    @Override
    public List<PathSegment> getPathSegments(boolean decode) {
        String path = getPath(decode);
        if (path.isEmpty()) {
            return Collections.emptyList();
        }

        String[] subPaths = path.split("/");
        List<PathSegment> segments = new LinkedList<>();
        for (String subPath : subPaths) {
            if (StringUtils.isNotEmpty(subPath)) {
                segments.add(new PathSegmentImpl(subPath));
            }
        }

        return Collections.unmodifiableList(segments);
    }

    @Override
    public URI getRequestUri() {
        return URI.create(context.request().uri());
    }

    @Override
    public UriBuilder getRequestUriBuilder() {
        return RuntimeDelegate.getInstance().createUriBuilder().uri(getBaseUri());
    }

    @Override
    public URI getAbsolutePath() {
        String path = getPath();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        /*
         * eg:
         * ("http://127.0.0.1:8080/abc/def").resolve("mn/xyz/def") to "http://127.0.0.1:8080/abc/mn/xyz/def"
         * ("http://127.0.0.1:8080/abc/def/").resolve("mn/xyz/def") to "http://127.0.0.1:8080/abc/def/mn/xyz/def"
         * ("http://127.0.0.1:8080/abc/def/").resolve("/mn/xyz/def") to "http://127.0.0.1:8080/mn/xyz/def"
         */
        return baseUri.resolve(path);
    }

    @Override
    public UriBuilder getAbsolutePathBuilder() {
        return RuntimeDelegate.getInstance().createUriBuilder().uri(getAbsolutePath());
    }

    @Override
    public URI getBaseUri() {
        return baseUri;
    }

    @Override
    public UriBuilder getBaseUriBuilder() {
        return RuntimeDelegate.getInstance().createUriBuilder().uri(getBaseUri());
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters() {
        return getPathParameters(true);
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
        Map<String, String> variables = context.getUncheckedAttribute(PatternsPredicate.TEMPLATE_VARIABLES);
        if (variables == null || variables.isEmpty()) {
            return new UnmodifiableMultivaluedMap<>(new MultivaluedHashMap<>());
        } else {
            MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                params.add(entry.getKey(), decode ? decode(entry.getValue()) : entry.getValue());
            }
            return new UnmodifiableMultivaluedMap<>(params);
        }
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return getQueryParameters(true);
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
        parameters.putAll(context.request().parameterMap());
        return parameters;
    }

    @Override
    public List<String> getMatchedURIs() {
        return getMatchedURIs(true);
    }

    @Override
    public List<String> getMatchedURIs(boolean decode) {
        List<HandlerMapping> mappings = RouteTracking.tracking(context);

        if (mappings.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> uris = new ArrayList<>(mappings.size());
        for (HandlerMapping mapping : mappings) {
            uris.add(mapping.mapping().toString());
        }
        return Collections.unmodifiableList(uris);
    }

    @Override
    public List<Object> getMatchedResources() {
        List<HandlerMapping> mappings = RouteTracking.tracking(context);

        if (mappings.isEmpty()) {
            return Collections.emptyList();
        }
        List<MatchedResource> resources = new ArrayList<>(mappings.size());
        for (HandlerMapping mapping : mappings) {
            resources.add(new MatchedResource(mapping.methodInfo(), mapping.bean().orElse(null)));
        }
        return Collections.unmodifiableList(resources);
    }

    @Override
    public URI resolve(URI uri) {
        if (uri.isAbsolute()) {
            return uri;
        }

        return getBaseUri().resolve(uri);
    }

    @Override
    public URI relativize(URI uri) {
        URI resolved = resolve(uri);
        return relativize(getRequestUri(), resolved);
    }

    public void baseUri(URI baseUri) {
        this.baseUri = baseUri;
    }

    private static String decode(String value) {
        try {
            value = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            LoggerUtils.logger().warn("Failed to decode {} by UTF-8", value);
        }

        return value;
    }

    private static URI relativize(URI from, URI to) {
        if (!from.isAbsolute() || !to.isAbsolute()) {
            return to;
        }

        if (from.isOpaque() || to.isOpaque()) {
            return to;
        }

        // from is absolute and to is absolute
        if (isEquals(from.getScheme(), to.getScheme())
                && isEquals(from.getRawAuthority(), to.getRawAuthority())) {
            String resolved = to.toString().replace(
                    to.getScheme() + "://" + to.getRawAuthority(), "");
            to = URI.create(resolved);
        }

        return from.relativize(to);
    }

    private static boolean isEquals(String from, String to) {
        if (from == null) {
            return to == null;
        }

        return from.equals(to);
    }
}
