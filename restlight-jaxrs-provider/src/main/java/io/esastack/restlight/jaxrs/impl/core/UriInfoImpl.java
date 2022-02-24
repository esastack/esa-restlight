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
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.esastack.restlight.jaxrs.impl.core;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.spi.impl.RouteTracking;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.util.PathVariableUtils;
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

/**
 * The {@link #relativize(URI, URI)} copies from org.apache.cxf.jaxrs.utils.HttpUtils which is
 * created by Apache CXF Copyright 2006-2014 The Apache Software Foundation.
 */
public class UriInfoImpl implements UriInfo {

    private static final Logger logger = LoggerFactory.getLogger(UriInfoImpl.class);

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
        return RuntimeDelegate.getInstance().createUriBuilder().uri(getRequestUri());
    }

    @Override
    public URI getAbsolutePath() {
        return getBaseUri().resolve(getPath(false));
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
        Map<String, String> variables = PathVariableUtils.getPathVariables(context);
        if (variables == null || variables.isEmpty()) {
            return new MultivaluedHashMap<>();
        } else {
            MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                params.add(entry.getKey(), decode ? decode(entry.getValue()) : entry.getValue());
            }
            return params;
        }
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return getQueryParameters(true);
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
        if (StringUtils.isEmpty(context.request().query())) {
            return parameters;
        }
        for (String item : context.request().query().split("&")) {
            if (StringUtils.isEmpty(item)) {
                continue;
            }
            String[] pairs = item.split("=");
            if (pairs.length != 2) {
                throw new IllegalStateException("Failed to split query pair from [" + item + "]");
            }
            parameters.add(pairs[0], pairs[1]);
        }
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
        Collections.reverse(uris);
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
        Collections.reverse(resources);
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
        return relativize(getRequestUri(), resolve(uri));
    }

    public void baseUri(URI baseUri) {
        this.baseUri = baseUri;
    }

    private static String decode(String value) {
        try {
            value = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            logger.warn("Failed to decode {} by UTF-8", value);
        }
        return value;
    }

    static URI relativize(URI base, URI uri) {
        // quick bail-out
        if (!(base.isAbsolute()) || !(uri.isAbsolute())) {
            return uri;
        }
        if (base.isOpaque() || uri.isOpaque()) {
            // Unlikely case of an URN which can't deal with
            // relative path, such as urn:isbn:0451450523
            return uri;
        }
        // Check for common root
        URI root = base.resolve("/");
        if (!(root.equals(uri.resolve("/")))) {
            // Different protocol/auth/host/port, return as is
            return uri;
        }

        // Ignore hostname bits for the following , but add "/" in the beginning
        // so that in worst case we'll still return "/fred" rather than
        // "http://example.com/fred".
        URI baseRel = URI.create("/").resolve(root.relativize(base));
        URI uriRel = URI.create("/").resolve(root.relativize(uri));

        // Is it same path?
        if (baseRel.getPath().equals(uriRel.getPath())) {
            return baseRel.relativize(uriRel);
        }

        // Direct siblings? (ie. in same folder)
        URI commonBase = baseRel.resolve("./");
        if (commonBase.equals(uriRel.resolve("./"))) {
            return commonBase.relativize(uriRel);
        }

        // No, then just keep climbing up until we find a common base.
        URI relative = URI.create("");
        while (!(uriRel.getPath().startsWith(commonBase.getPath())) && !(commonBase.getPath().equals("/"))) {
            commonBase = commonBase.resolve("../");
            relative = relative.resolve("../");
        }

        // Now we can use URI.relativize
        URI relToCommon = commonBase.relativize(uriRel);
        // and prepend the needed ../
        return relative.resolve(relToCommon);
    }
}
