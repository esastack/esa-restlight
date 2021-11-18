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

    private UriBuilderImpl(String scheme, String userInfo, String host, int port,
                           String path, String query, String fragment) {
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
        try {
            if (scheme == null) {
                scheme = "http";
            }
            return uri(new URI(scheme, ssp, fragment));
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Error occurred when parsing:[" + ssp + "] to URI", ex);
        }
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
        Checks.checkArg(port < -1 || port > 65535, "illegal port: " + port);
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
        return this;
    }

    @Override
    public UriBuilder path(Class resource) {
        if (resource == null) {
            throw new IllegalArgumentException("resource must not be null");
        }

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
            throw new IllegalArgumentException("Has found " + methods.size() + " methods which" +
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
            if (segment == null) {
                continue;
            }
            path(UriUtils.encode(segment, true, false, false));
        }

        return this;
    }

    @Override
    public UriBuilder replaceMatrix(String matrix) {
        matrix = checkNotNull(matrix);
        if (StringUtils.isNotEmpty(matrix)) {
            matrix = matrix.startsWith(";") ? matrix : ";" + matrix;
        }

        if (this.path == null) {
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
        Checks.checkArg(values != null, "values must not be nulls");

        StringBuilder path = new StringBuilder(checkNotNull(this.path));
        for (Object value : values) {
            if (value == null) {
                continue;
            }

            path.append(";").append(name).append(";").append(value);
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
        this.query = query;
        return this;
    }

    @Override
    public UriBuilder queryParam(String name, Object... values) {
        Checks.checkArg(name != null, "name must not be null");
        Checks.checkArg(values != null, "values must not be null");

        StringBuilder sb = new StringBuilder(checkNotNull(this.query));
        String prefix = StringUtils.isEmpty(this.query) ? "" : "&";
        for (Object value : values) {
            sb.append(prefix).append(name).append("=").append(value);
            prefix = "&";
        }

        this.query = sb.toString();
        return this;
    }

    @Override
    public UriBuilder replaceQueryParam(String name, Object... values) {
        Checks.checkArg(name != null, "name must not be null");

        if (values == null) {
            this.query = null;
        } else {
            StringBuilder sb = new StringBuilder();
            String prefix = "";
            for (Object value : values) {
                if (value == null) {
                    continue;
                }
                sb.append(prefix).append(name).append("=").append(value);
                prefix = "&";
            }
            this.query = sb.toString();
        }

        return this;
    }

    @Override
    public UriBuilder fragment(String fragment) {
        this.fragment = fragment;
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
        checkTemplate(templateValues);
        return resolveTemplates(templateValues, true,
                false, true);
    }

    @Override
    public UriBuilder resolveTemplates(Map<String, Object> templateValues, boolean encodeSlashInPath)
            throws IllegalArgumentException {
        checkTemplate(templateValues);
        return resolveTemplates(templateValues, encodeSlashInPath,
                false, true);
    }

    @Override
    public UriBuilder resolveTemplatesFromEncoded(Map<String, Object> templateValues) {
        checkTemplate(templateValues);
        return resolveTemplates(templateValues, true,
                false, true);
    }

    @Override
    public URI buildFromMap(Map<String, ?> values) {
        checkEntryValues(values);
        resolveTemplates(values, true, true, false);
        checkUnResolvedTemplate();
        return doBuild();
    }

    @Override
    public URI buildFromMap(Map<String, ?> values, boolean encodeSlashInPath) throws IllegalArgumentException,
            UriBuilderException {
        checkEntryValues(values);
        resolveTemplates(values, encodeSlashInPath, true, false);
        checkUnResolvedTemplate();
        return doBuild();
    }

    @Override
    public URI buildFromEncodedMap(Map<String, ?> values) throws IllegalArgumentException, UriBuilderException {
        checkEntryValues(values);
        resolveTemplates(values, true, false, true);
        checkUnResolvedTemplate();
        return doBuild();
    }

    @Override
    public URI build(Object... values) throws IllegalArgumentException, UriBuilderException {
        checkValues(values);
        resolveTemplates(values, true, true, false);
        checkUnResolvedTemplate();
        return doBuild();
    }

    @Override
    public URI build(Object[] values, boolean encodeSlashInPath) throws IllegalArgumentException, UriBuilderException {
        checkValues(values);
        resolveTemplates(values, encodeSlashInPath, true, false);
        checkUnResolvedTemplate();
        return doBuild();
    }

    @Override
    public URI buildFromEncoded(Object... values) throws IllegalArgumentException, UriBuilderException {
        checkValues(values);
        resolveTemplates(values, true, false, true);
        checkUnResolvedTemplate();
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

        if (path != null) {
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

    private URI doBuild() {
        String uri = null;
        try {
            uri = toTemplate();
            return new URI(uri);
        } catch (URISyntaxException ex) {
            throw new UriBuilderException("Failed to convert :[" + uri + "] to URI", ex);
        }
    }

    private void checkUnResolvedTemplate() {
        if (StringUtils.isEmpty(path)) {
            return;
        }
        if (!path.contains("{")) {
            return;
        }

        List<String> unResolvedTemplates = new LinkedList<>();
        StringBuilder unresolved = null;
        for (int i = 0; i < path.length(); i++) {
            switch (path.charAt(i)) {
                case '{':
                    unresolved = new StringBuilder();
                    break;
                case '}':
                    if (unresolved != null) {
                        unResolvedTemplates.add(unresolved.toString());
                    }
                    break;
                default:
                    if (unresolved != null) {
                        unresolved.append(path.charAt(i));
                    }
            }
        }

        throw new UriBuilderException("The URI templates: " + unResolvedTemplates + " haven't supplied value");
    }

    private UriBuilder resolveTemplates(Map<String, ?> templateValues, boolean encodeSlashInPath,
                                        boolean encodePercentAnyway, boolean encodePercentCondition) {
        if (StringUtils.isEmpty(path) || templateValues.isEmpty()) {
            return this;
        }

        return resolveTemplates0((template, index) -> templateValues.get(template), encodeSlashInPath,
                encodePercentAnyway, encodePercentCondition);
    }

    private void resolveTemplates(Object[] values, boolean encodeSlashInPath,
                                  boolean encodePercentAnyway, boolean encodePercentCondition) {
        if (StringUtils.isEmpty(path) || values == null || values.length == 0) {
            return;
        }

        final Map<String, String> templateValues = new HashMap<>();

        resolveTemplates0((template, index) -> {
            Object value = templateValues.get(template);
            if (value != null) {
                return value;
            } else {
                value = values[index];
                templateValues.put(template, value.toString());
                return templateValues.get(template);
            }
        }, encodeSlashInPath, encodePercentAnyway, encodePercentCondition);
    }

    private UriBuilder resolveTemplates0(BiFunction<String, Integer, ?> valueFunction, boolean encodeSlashInPath,
                                         boolean encodePercentAnyway, boolean encodePercentCondition) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (int i = 0; i < path.length(); i++) {
            if (path.indexOf(i) == '{') {
                for (int j = i; j < path.length(); j++) {
                    if (path.indexOf(j) == '}') {
                        String template = path.substring(i + 1, j);
                        Object value = valueFunction.apply(template, index);
                        if (value == null) {
                            sb.append(path, i + 1, j);
                        } else {
                            sb.append(value);
                        }
                        i = j;
                        index++;
                        break;
                    }
                }
            } else {
                sb.append(path.indexOf(i));
            }
        }

        this.path = UriUtils.encode(sb.toString(), encodeSlashInPath, encodePercentAnyway, encodePercentCondition);
        return this;
    }

    private static void checkTemplate(Map<String, ?> templateValues) {
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
        if (value == null) {
            return "";
        } else {
            return value;
        }
    }

    private static List<Method> selectMethods(Class<?> resource, String method) {
        List<Method> methods = new LinkedList<>();
        for (Method m : resource.getMethods()) {
            if (m.getName().equals(method) && annotatedWithPath.test(m)) {
                methods.add(m);
            }
        }
        return methods;
    }
}

