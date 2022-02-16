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
import esa.commons.ClassUtils;
import esa.commons.StringUtils;
import esa.commons.reflect.AnnotationUtils;
import io.esastack.restlight.jaxrs.util.UriUtils;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriBuilderException;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class UriBuilderImpl extends UriBuilder {

    private static final Predicate<Method> annotatedWithPath = method ->
            AnnotationUtils.hasAnnotation(method, Path.class);

    private String scheme;
    private String userInfo;
    private String host;
    private int port;
    private String path;
    private String query;
    private String fragment;

    public UriBuilderImpl() {
    }

    private UriBuilderImpl(String scheme,
                           String userInfo,
                           String host,
                           int port,
                           String path,
                           String query,
                           String fragment) {
        this.scheme = scheme;
        this.userInfo = userInfo;
        this.host = host;
        this.port = port;
        this.path = path;
        this.query = query;
        this.fragment = fragment;
    }

    @Override
    public UriBuilder clone() {
        return new UriBuilderImpl(scheme, userInfo, host, port, path, query, fragment);
    }

    @Override
    public UriBuilder uri(URI uri) {
        Checks.checkArg(uri != null, "uri must not be null");
        if (uri.getScheme() != null) {
            this.scheme = uri.getScheme();
        }

        if (uri.getRawUserInfo() != null) {
            this.userInfo = uri.getRawUserInfo();
        }

        if (uri.getHost() != null) {
            this.host = uri.getHost();
        }

        if (uri.getPort() > 0) {
            this.port = uri.getPort();
        }

        if (uri.getRawPath() != null) {
            this.path = uri.getRawPath();
        }

        if (uri.getRawQuery() != null) {
            this.query = uri.getRawQuery();
        }

        if (uri.getRawFragment() != null) {
            this.fragment = uri.getRawFragment();
        }

        return this;
    }

    @Override
    public UriBuilder uri(String uriTemplate) {
        Checks.checkArg(uriTemplate != null, "uriTemplate must not be null");
        try {
            return uri(new URI(uriTemplate));
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Error occurred when parsing:[" + uriTemplate + "] to URI", ex);
        }
    }

    @Override
    public UriBuilder scheme(String scheme) {
        if (scheme == null) {
            scheme = "http";
        }
        if ("http".equals(scheme) || "https".equals(scheme)) {
            this.scheme = scheme;
            return this;
        }

        throw new IllegalArgumentException("illegal scheme: " + scheme);
    }

    @Override
    public UriBuilder schemeSpecificPart(String ssp) {
        Checks.checkArg(ssp != null, "ssp must not be null");
        if (scheme == null) {
            scheme = "http";
        }
        StringBuilder sb = new StringBuilder(this.scheme);
        if (!ssp.startsWith("://")) {
            sb.append("://");
        }
        sb.append(ssp);
        if (StringUtils.isNotEmpty(fragment)) {
            sb.append("#").append(fragment);
        }
        return uri(URI.create(sb.toString()));
    }

    @Override
    public UriBuilder userInfo(String ui) {
        this.userInfo = ui;
        return this;
    }

    @Override
    public UriBuilder host(String host) {
        Checks.checkNotEmptyArg(host, "host must be null or empty");
        this.host = host;
        return this;
    }

    @Override
    public UriBuilder port(int port) {
        Checks.checkArg(port > 0 && port <= 65535, "illegal port: " + port);
        this.port = port;
        return this;
    }

    @Override
    public UriBuilder replacePath(String path) {
        if (path != null && !path.startsWith("/")) {
            path = "/" + path;
        }
        this.path = path;
        return this;
    }

    @Override
    public UriBuilder path(String path) {
        Checks.checkArg(path != null, "path must not be null");
        if (StringUtils.isEmpty(this.path)) {
            this.path = path;
        } else {
            if (this.path.endsWith("/")) {
                if (path.startsWith("/")) {
                    this.path += path.substring(1);
                } else {
                    this.path += path;
                }
            } else {
                if (path.startsWith("/")) {
                    this.path += path;
                } else {
                    this.path += "/" + path;
                }
            }
        }
        return this;
    }

    @Override
    public UriBuilder path(Class resource) {
        Checks.checkArg(resource != null, "resource must not be null");

        Path path = AnnotationUtils.findAnnotation(resource, Path.class);
        if (path == null) {
            throw new IllegalArgumentException("@Path must be annotated on class: " + resource.getSimpleName());
        }

        return path(path.value());
    }

    @Override
    public UriBuilder path(Class resource, String method) {
        Checks.checkArg(resource != null, "resource must not be null");
        Checks.checkNotEmptyArg(method, "method must not be null or empty");

        List<Method> methods = selectMethods(resource, method);
        if (methods.size() != 1) {
            throw new IllegalArgumentException("Found " + methods.size() + " methods which" +
                    " named:[" + method + "] and annotated with @Path");
        }

        return path(methods.get(0));
    }

    @Override
    public UriBuilder path(Method method) {
        Checks.checkArg(method != null, "method must not be null");
        Path path = AnnotationUtils.findAnnotation(method, Path.class);
        if (path == null) {
            throw new IllegalArgumentException("@Path must be annotated on method: " + method.getName());
        }
        return path(path.value());
    }

    @Override
    public UriBuilder segment(String... segments) {
        Checks.checkArg(segments != null, "segments must not be null");
        for (String segment : segments) {
            Checks.checkArg(segment != null, "segment item must not be null");
        }

        for (String segment : segments) {
            if (StringUtils.isEmpty(segment)) {
                continue;
            }
            path(UriUtils.encode(segment));
        }

        return this;
    }

    @Override
    public UriBuilder replaceMatrix(String matrix) {
        matrix = checkNotNull(matrix);
        if (StringUtils.isNotEmpty(matrix) && !matrix.startsWith(";")) {
            matrix = ";" + matrix;
        }
        matrix = UriUtils.encode(matrix);
        if (StringUtils.isEmpty(this.path)) {
            this.path = matrix;
        } else {
            int start = Math.max(0, path.lastIndexOf("/"));
            int matrixIndex = this.path.indexOf(";", start);
            if (matrixIndex > -1) {
                this.path = this.path.substring(0, matrixIndex) + matrix;
            } else {
                this.path += matrix;
            }
        }

        return this;
    }

    @Override
    public UriBuilder matrixParam(String name, Object... values) {
        Checks.checkArg(name != null, "name must not be null");
        Checks.checkArg(values != null, "values must not be null");

        StringBuilder path = new StringBuilder(checkNotNull(this.path));
        for (Object value : values) {
            if (value == null) {
                continue;
            }

            path.append(";").append(UriUtils.encode(name)).append("=").append(UriUtils.encode(value.toString()));
        }
        this.path = path.toString();
        return this;
    }

    @Override
    public UriBuilder replaceMatrixParam(String name, Object... values) {
        Checks.checkArg(name != null, "name must not be null");

        replaceMatrix(null);
        if (values != null) {
            matrixParam(name, values);
        }
        return this;
    }

    @Override
    public UriBuilder replaceQuery(String query) {
        if (StringUtils.isEmpty(query)) {
            this.query = null;
        } else {
            this.query = UriUtils.encode(query);
        }
        return this;
    }

    @Override
    public UriBuilder queryParam(String name, Object... values) {
        Checks.checkArg(name != null, "name must not be null");
        Checks.checkArg(values != null, "values must not be null");

        StringBuilder sb = new StringBuilder(checkNotNull(this.query));
        String prefix = StringUtils.isEmpty(this.query) ? "" : "&";
        for (Object value : values) {
            sb.append(prefix).append(UriUtils.encode(name))
                    .append("=")
                    .append(UriUtils.encode(value.toString()));
            prefix = "&";
        }

        this.query = sb.toString();
        return this;
    }

    @Override
    public UriBuilder replaceQueryParam(String name, Object... values) {
        Checks.checkArg(name != null, "name must not be null");

        if (values == null || values.length == 0) {
            this.query = null;
        } else {
            StringBuilder sb = new StringBuilder();
            String prefix = "";
            for (Object value : values) {
                if (value == null) {
                    continue;
                }
                sb.append(prefix).append(UriUtils.encode(name)).append("=").append(UriUtils.encode(value.toString()));
                prefix = "&";
            }
            this.query = sb.toString();
        }

        return this;
    }

    @Override
    public UriBuilder fragment(String fragment) {
        if (StringUtils.isEmpty(fragment)) {
            this.fragment = null;
        } else {
            this.fragment = UriUtils.encode(fragment);
        }
        return this;
    }

    @Override
    public UriBuilder resolveTemplate(String name, Object value) {
        Checks.checkArg(name != null, "name must not be null");
        Checks.checkArg(value != null, "value must not be null");
        return resolveTemplates(Collections.singletonMap(name, value), true,
                false, false);
    }

    @Override
    public UriBuilder resolveTemplate(String name, Object value, boolean encodeSlashInPath) {
        Checks.checkArg(name != null, "name must not be null");
        Checks.checkArg(value != null, "value must not be null");
        return resolveTemplates(Collections.singletonMap(name, value), encodeSlashInPath,
                false, false);
    }

    @Override
    public UriBuilder resolveTemplateFromEncoded(String name, Object value) {
        Checks.checkArg(name != null, "name must not be null");
        Checks.checkArg(value != null, "value must not be null");
        return resolveTemplates(Collections.singletonMap(name, value), true,
                false, true);
    }

    @Override
    public UriBuilder resolveTemplates(Map<String, Object> templateValues) {
        checkTemplateMaps(templateValues);
        return resolveTemplates(templateValues, true,
                false, false);
    }

    @Override
    public UriBuilder resolveTemplates(Map<String, Object> templateValues, boolean encodeSlashInPath)
            throws IllegalArgumentException {
        checkTemplateMaps(templateValues);
        return resolveTemplates(templateValues, encodeSlashInPath,
                false, false);
    }

    @Override
    public UriBuilder resolveTemplatesFromEncoded(Map<String, Object> templateValues) {
        checkTemplateMaps(templateValues);
        return resolveTemplates(templateValues, true,
                false, true);
    }

    @Override
    public URI buildFromMap(Map<String, ?> values) {
        checkEntryValues(values);
        resolveTemplates(values, true, true, false);
        return doBuild();
    }

    @Override
    public URI buildFromMap(Map<String, ?> values, boolean encodeSlashInPath) throws IllegalArgumentException,
            UriBuilderException {
        checkEntryValues(values);
        resolveTemplates(values, encodeSlashInPath, true, false);
        return doBuild();
    }

    @Override
    public URI buildFromEncodedMap(Map<String, ?> values) throws IllegalArgumentException, UriBuilderException {
        checkEntryValues(values);
        resolveTemplates(values, true, false, true);
        return doBuild();
    }

    @Override
    public URI build(Object... values) throws IllegalArgumentException, UriBuilderException {
        checkValues(values);
        resolveTemplates(values, true, true, false);
        return doBuild();
    }

    @Override
    public URI build(Object[] values, boolean encodeSlashInPath) throws IllegalArgumentException, UriBuilderException {
        checkValues(values);
        resolveTemplates(values, encodeSlashInPath, true, false);
        return doBuild();
    }

    @Override
    public URI buildFromEncoded(Object... values) throws IllegalArgumentException, UriBuilderException {
        checkValues(values);
        resolveTemplates(values, true, false, true);
        return doBuild();
    }

    @Override
    public String toTemplate() {
        final StringBuilder sb = new StringBuilder();
        if (scheme != null) {
            sb.append(scheme).append("://");
        }
        if (userInfo != null) {
            sb.append(userInfo).append("@");
        }
        if (StringUtils.isEmpty(host)) {
            if (sb.length() > 0) {
                throw new UriBuilderException("host must not be absent");
            }
        } else {
            sb.append(host);
        }

        if (port > 0) {
            sb.append(":").append(port);
        }

        if (StringUtils.isNotEmpty(path)) {
            if (!path.startsWith("/")) {
                sb.append("/");
            }
            sb.append(path);
        }

        if (query != null) {
            sb.append("?").append(query);
        }

        if (fragment != null) {
            sb.append("#").append(fragment);
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UriBuilderImpl{");
        sb.append("scheme='").append(scheme).append('\'');
        sb.append(", userInfo='").append(userInfo).append('\'');
        sb.append(", host='").append(host).append('\'');
        sb.append(", port=").append(port);
        sb.append(", path='").append(path).append('\'');
        sb.append(", query='").append(query).append('\'');
        sb.append(", fragment='").append(fragment).append('\'');
        sb.append('}');
        return sb.toString();
    }

    private URI doBuild() {
        String uri = null;
        try {
            uri = toTemplate();
            return new URI(uri);
        } catch (URISyntaxException ex) {
            throw new UriBuilderException("Failed to convert :[" + uri + "] to URI", ex);
        }
    }

    private UriBuilder resolveTemplates(Map<String, ?> templateValues, boolean encodeSlashInPath,
                                        boolean encodePercentAnyway, boolean encodePercentCondition) {
        if (templateValues == null || templateValues.isEmpty()) {
            return this;
        }

        this.userInfo = doResolveTemplate((template, index) -> templateValues.get(template), this.userInfo,
                false, encodePercentAnyway, encodePercentCondition);
        this.host = doResolveTemplate((template, index) -> templateValues.get(template), this.host,
                false, encodePercentAnyway, encodePercentCondition);
        this.path = doResolveTemplate((template, index) -> templateValues.get(template), this.path,
                encodeSlashInPath, encodePercentAnyway, encodePercentCondition);
        this.query = doResolveTemplate((template, index) -> templateValues.get(template), this.query,
                false, encodePercentAnyway, encodePercentCondition);
        this.fragment = doResolveTemplate((template, index) -> templateValues.get(template), this.fragment,
                false, encodePercentAnyway, encodePercentCondition);

        return this;
    }

    private void resolveTemplates(Object[] values, boolean encodeSlashInPath,
                                  boolean encodePercentAnyway, boolean encodePercentCondition) {
        if (values == null || values.length == 0) {
            return;
        }

        final Map<String, String> templateValues = new HashMap<>();
        final BiFunction<String, Integer, ?> valueFunction = (template, index) -> {
            Object value = templateValues.get(template);
            if (value != null) {
                return value;
            } else {
                value = values[index];
                templateValues.put(template, value.toString());
                return templateValues.get(template);
            }
        };

        this.userInfo = doResolveTemplate(valueFunction, this.userInfo,
                false, encodePercentAnyway, encodePercentCondition);
        this.host = doResolveTemplate(valueFunction, this.host,
                false, encodePercentAnyway, encodePercentCondition);
        this.path = doResolveTemplate(valueFunction, this.path,
                encodeSlashInPath, encodePercentAnyway, encodePercentCondition);
        this.query = doResolveTemplate(valueFunction, this.query,
                false, encodePercentAnyway, encodePercentCondition);
        this.fragment = doResolveTemplate(valueFunction, this.fragment,
                false, encodePercentAnyway, encodePercentCondition);
    }

    private static String doResolveTemplate(BiFunction<String, Integer, ?> valueFunction,
                                            String target, boolean encodeSlashInPath,
                                            boolean encodePercentAnyway, boolean encodePercentCondition) {
        if (StringUtils.isEmpty(target)) {
            return target;
        }
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (int i = 0; i < target.length(); i++) {
            if (target.charAt(i) == '{') {
                for (int j = i; j < target.length(); j++) {
                    if (target.charAt(j) == '}') {
                        String template = target.substring(i + 1, j);
                        Object value = valueFunction.apply(template, index);
                        if (value == null) {
                            sb.append(target, i, Math.min(target.length(), j + 1));
                        } else {
                            sb.append(UriUtils.encode(value.toString(), encodeSlashInPath,
                                    encodePercentAnyway, encodePercentCondition));
                        }
                        i = j;
                        index++;
                        break;
                    }
                }
            } else {
                sb.append(target.charAt(i));
            }
        }

        return sb.toString();
    }

    private static void checkTemplateMaps(Map<String, ?> templateValues) {
        Checks.checkArg(templateValues != null, "templateValues must not be null");
        for (Map.Entry<String, ?> item : templateValues.entrySet()) {
            Checks.checkArg(item.getKey() != null, "null key in templateValues is illegal");
            Checks.checkArg(item.getValue() != null, "null value in templateValues is illegal");
        }
    }

    private static void checkEntryValues(Map<String, ?> values) {
        if (values == null) {
            return;
        }

        for (Map.Entry<String, ?> item : values.entrySet()) {
            Checks.checkArg(item.getValue() != null, "null value in values is illegal");
        }
    }

    private static void checkValues(Object... values) {
        if (values == null) {
            return;
        }

        for (Object item : values) {
            Checks.checkArg(item != null, "null value in values is illegal");
        }
    }

    private static String checkNotNull(String value) {
        return value == null ? "" : value;
    }

    private static List<Method> selectMethods(Class<?> resource, String method) {
        List<Method> methods = new LinkedList<>();
        ClassUtils.doWithUserDeclaredMethods(resource, methods::add,
                m -> m.getName().equals(method) && annotatedWithPath.test(m));
        return methods;
    }
}

