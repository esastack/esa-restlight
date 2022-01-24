/*
 * Copyright 2020 OPPO ESA Stack Project
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
package io.esastack.restlight.server.route.impl;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.UrlUtils;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restlight.server.route.Mapping;
import io.esastack.restlight.server.route.predicate.HeadersPredicate;
import io.esastack.restlight.server.route.predicate.ParamsPredicate;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A simple implementation of {@link Mapping} which just hold the fields given by constructor.
 */
public class MappingImpl implements Mapping {

    private final String name;
    private final String[] path;
    private final HttpMethod[] method;
    private final String[] params;
    private final String[] headers;
    private final String[] consumes;
    private final String[] produces;

    private String str;

    public MappingImpl(Mapping composite) {
        this(composite.name(),
                composite.path(),
                composite.method(),
                composite.params(),
                composite.headers(),
                composite.consumes(),
                composite.produces());
    }

    public MappingImpl() {
        this(null,
                new String[0],
                new HttpMethod[0],
                new String[0],
                new String[0],
                new String[0],
                new String[0]);
    }

    public MappingImpl(String name,
                       String[] path,
                       HttpMethod[] method,
                       String[] params,
                       String[] headers,
                       String[] consumes,
                       String[] produces) {
        Checks.checkNotNull(path, "path");
        Checks.checkNotNull(method, "method");
        Checks.checkNotNull(params, "params");
        Checks.checkNotNull(headers, "headers");
        Checks.checkNotNull(consumes, "consumes");
        Checks.checkNotNull(produces, "produces");
        this.name = name;
        this.path = path;
        this.method = method;
        this.params = params;
        this.headers = headers;
        this.consumes = consumes;
        this.produces = produces;
    }

    /**
     * Sets the {@link #name} to given value.
     *
     * @param name name
     * @return current instance
     */
    public MappingImpl name(String name) {
        return new MappingImpl(name,
                this.path,
                this.method,
                this.params,
                this.headers,
                this.consumes,
                this.produces);
    }

    /**
     * Sets the {@link #path} to given single path
     *
     * @param path path
     * @return builder
     */
    public MappingImpl path(String path) {
        return path(Collections.singletonList(path).toArray(new String[0]));
    }

    /**
     * Sets the {@link #path} to given paths
     *
     * @param path path
     * @return builder
     */
    public MappingImpl path(String... path) {
        UrlUtils.prependLeadingSlash(path);
        if (path.length > 0) {
            return new MappingImpl(this.name,
                    path,
                    this.method,
                    this.params,
                    this.headers,
                    this.consumes,
                    this.produces);
        }
        return this;
    }

    /**
     * Sets the {@link #method} to a single value of {@link HttpMethod#GET}.
     *
     * @return builder
     */
    public MappingImpl get() {
        return method(HttpMethod.GET);
    }

    /**
     * Sets the {@link #method} to a single value of {@link HttpMethod#GET} and sets the {@link #path} to the given
     * value.
     *
     * @param path path
     * @return builder
     */
    public MappingImpl get(String path) {
        return path(path)
                .method(HttpMethod.GET);
    }

    /**
     * Sets the {@link #method} to a single value of {@link HttpMethod#POST}.
     *
     * @return builder
     */
    public MappingImpl post() {
        return method(HttpMethod.POST);
    }

    /**
     * Sets the {@link #method} to a single value of {@link HttpMethod#POST} and sets the {@link #path} to the given
     * value.
     *
     * @param path path
     * @return builder
     */
    public MappingImpl post(String path) {
        return path(path)
                .method(HttpMethod.POST);
    }

    /**
     * Sets the {@link #method} to a single value of {@link HttpMethod#DELETE}.
     *
     * @return builder
     */
    public MappingImpl delete() {
        return method(HttpMethod.DELETE);
    }

    /**
     * Sets the {@link #method} to a single value of {@link HttpMethod#DELETE} and sets the {@link #path} to the given
     * value.
     *
     * @param path path
     * @return builder
     */
    public MappingImpl delete(String path) {
        return path(path)
                .method(HttpMethod.DELETE);
    }

    /**
     * Sets the {@link #method} to a single value of {@link HttpMethod#PUT}.
     *
     * @return builder
     */
    public MappingImpl put() {
        return method(HttpMethod.PUT);
    }

    /**
     * Sets the {@link #method} to a single value of {@link HttpMethod#PUT} and sets the {@link #path} to the given
     * value.
     *
     * @param path path
     * @return builder
     */
    public MappingImpl put(String path) {
        return path(path)
                .method(HttpMethod.PUT);
    }

    /**
     * Sets the {@link #method} to a single value of {@link HttpMethod#PATCH}.
     *
     * @return builder
     */
    public MappingImpl patch() {
        return method(HttpMethod.PATCH);
    }

    /**
     * Sets the {@link #method} to a single value of {@link HttpMethod#PATCH} and sets the {@link #path} to the given
     * value.
     *
     * @param path path
     * @return builder
     */
    public MappingImpl patch(String path) {
        return path(path)
                .method(HttpMethod.PATCH);
    }

    /**
     * Sets the {@link #method} to a single value of {@link HttpMethod#HEAD}.
     *
     * @return builder
     */
    public MappingImpl head() {
        return method(HttpMethod.HEAD);
    }

    /**
     * Sets the {@link #method} to a single value of {@link HttpMethod#HEAD} and sets the {@link #path} to the given
     * value.
     *
     * @param path path
     * @return builder
     */
    public MappingImpl head(String path) {
        return path(path)
                .method(HttpMethod.HEAD);
    }

    /**
     * Sets the {@link #method} to a single value of {@link HttpMethod#OPTIONS}.
     *
     * @return builder
     */
    public MappingImpl options() {
        return method(HttpMethod.OPTIONS);
    }

    /**
     * Sets the {@link #method} to a single value of {@link HttpMethod#OPTIONS} and sets the {@link #path} to the given
     * value.
     *
     * @param path path
     * @return builder
     */
    public MappingImpl options(String path) {
        return path(path)
                .method(HttpMethod.OPTIONS);
    }

    /**
     * Sets the {@link #method} to a single value of {@link HttpMethod#TRACE}.
     *
     * @return builder
     */
    public MappingImpl trace() {
        return method(HttpMethod.TRACE);
    }

    /**
     * Sets the {@link #method} to a single value of {@link HttpMethod#TRACE} and sets the {@link #path} to the given
     * value.
     *
     * @param path path
     * @return builder
     */
    public MappingImpl trace(String path) {
        return path(path)
                .method(HttpMethod.TRACE);
    }

    /**
     * Sets the {@link #method} to a single value of {@link HttpMethod#CONNECT}.
     *
     * @return builder
     */
    public MappingImpl connect() {
        return method(HttpMethod.CONNECT);
    }

    /**
     * Sets the {@link #method} to a single value of {@link HttpMethod#CONNECT} and sets the {@link #path} to the given
     * value.
     *
     * @param path path
     * @return builder
     */
    public MappingImpl connect(String path) {
        return path(path)
                .method(HttpMethod.CONNECT);
    }

    /**
     * Sets the {@link #method} to given value.
     *
     * @param method method
     * @return builder
     */
    public MappingImpl method(String method) {
        Checks.checkNotNull(method, "method");
        return method(HttpMethod.valueOf(method.toUpperCase()));
    }

    /**
     * Sets the {@link #method} to given value.
     *
     * @param method method
     * @return builder
     */
    public MappingImpl method(HttpMethod method) {
        Checks.checkNotNull(method, "method");
        return method(Collections.singletonList(method).toArray(new HttpMethod[0]));
    }

    /**
     * Sets the {@link #method} to given methods.
     *
     * @param method method
     * @return builder
     */
    public MappingImpl method(HttpMethod... method) {
        if (method != null && method.length > 0) {
            return new MappingImpl(this.name,
                    this.path,
                    method,
                    this.params,
                    this.headers,
                    this.consumes,
                    this.produces);
        }
        return this;
    }

    /**
     * Adds a parameter predicate which indicates that a request must have a parameter named by the given name.
     *
     * @param name parameter name
     * @return builder
     */
    public MappingImpl hasParam(String name) {
        return hasParam(name, null);
    }

    /**
     * Adds a parameter predicate which indicates that a request must have a parameter who's name and value must be the
     * same with the given name value.
     *
     * @param name parameter name
     * @return builder
     */
    public MappingImpl hasParam(String name, String value) {
        return hasParam(name, value, false);
    }

    /**
     * Adds a parameter predicate which indicates that request must not have a parameter named by given name.
     *
     * @param name parameter name
     * @return builder
     */
    public MappingImpl noneParam(String name) {
        return noneParam(name, null);
    }

    /**
     * Adds a parameter predicate which indicates that request must not have a parameter who's name and value are same
     * with the given name and value.
     *
     * @param name parameter name
     * @return builder
     */
    public MappingImpl noneParam(String name, String value) {
        return hasParam(name, value, true);
    }

    private MappingImpl hasParam(String name, String value, boolean isNegated) {
        Checks.checkNotEmptyArg(name, "name");
        return params(buildExpr(name, value, isNegated));
    }

    private String buildExpr(String name, String value, boolean isNegated) {
        String expr = name;
        if (StringUtils.isEmpty(value)) {
            if (isNegated) {
                expr = '!' + expr;
            }
        } else {
            if (isNegated) {
                expr = expr + "!=" + value;
            } else {
                expr = expr + '=' + value;
            }
        }
        return expr;
    }

    public MappingImpl params(String... params) {
        if (params != null && params.length > 0) {
            Set<String> tmp;
            if (this.params != null && this.params.length > 0) {
                tmp = new LinkedHashSet<>(this.params.length + params.length);
                tmp.addAll(Arrays.asList(this.params));
            } else {
                tmp = new LinkedHashSet<>(params.length);
            }
            for (String param : params) {
                tmp.add(ParamsPredicate.normaliseExpression(param));
            }
            return new MappingImpl(this.name,
                    this.path,
                    this.method,
                    tmp.toArray(new String[0]),
                    this.headers,
                    this.consumes,
                    this.produces);
        }
        return this;
    }

    /**
     * Adds a header predicate which indicates that request must have a header named by the given header name.
     *
     * @param name header name
     * @return builder
     */
    public MappingImpl hasHeader(String name) {
        return hasHeader(name, null);
    }

    /**
     * Adds a header predicate which indicates that request must have a header named by the given header name and this
     * header must have a value which is same with the given header value.
     *
     * @param name header name
     * @return builder
     */
    public MappingImpl hasHeader(String name, String value) {
        return hasHeader(name, value, false);
    }

    public MappingImpl noneHeader(String name) {
        return noneHeader(name, null);
    }

    /**
     * Adds a header predicate which indicates that request must not have a header that is named by the given header
     * name and have a value same with the given header value.
     *
     * @param name header name
     * @return builder
     */
    public MappingImpl noneHeader(String name, String value) {
        return hasHeader(name, value, true);
    }

    private MappingImpl hasHeader(String name, String value, boolean isNegated) {
        return headers(buildExpr(name, value, isNegated));
    }

    /**
     * Adds some header predicates.
     *
     * @param headers headers
     * @return builder
     */
    public MappingImpl headers(String... headers) {
        if (headers != null && headers.length > 0) {
            Set<String> tmp;
            if (this.headers != null && this.headers.length > 0) {
                tmp = new LinkedHashSet<>(this.headers.length + headers.length);
                tmp.addAll(Arrays.asList(this.headers));
            } else {
                tmp = new LinkedHashSet<>(headers.length);
            }
            for (String header : headers) {
                tmp.add(HeadersPredicate.normaliseExpression(header));
            }
            return new MappingImpl(this.name,
                    this.path,
                    this.method,
                    this.params,
                    tmp.toArray(new String[0]),
                    this.consumes,
                    this.produces);
        }
        return this;
    }

    /**
     * Adds a single consumes predicate
     *
     * @param mediaType mediaType
     * @return builder
     */
    public MappingImpl consumes(MediaType... mediaType) {
        return consumes(Arrays.stream(mediaType)
                .map(MediaType::value)
                .toArray(String[]::new));
    }

    /**
     * Adds some consumes predicates
     *
     * @param consumes consumes
     * @return builder
     */
    public MappingImpl consumes(String... consumes) {
        if (consumes != null && consumes.length > 0) {
            Set<String> tmp;
            if (this.consumes != null && this.consumes.length > 0) {
                tmp = new LinkedHashSet<>(this.consumes.length + consumes.length);
                tmp.addAll(Arrays.asList(this.consumes));
            } else {
                tmp = new LinkedHashSet<>(consumes.length);
            }
            for (String c : consumes) {
                tmp.add(MediaTypeUtil.parseMediaType(c).value());
            }
            return new MappingImpl(this.name,
                    this.path,
                    this.method,
                    this.params,
                    this.headers,
                    tmp.toArray(new String[0]),
                    this.produces);
        }
        return this;
    }

    /**
     * Adds a single produces predicate
     *
     * @param mediaType mediaType
     * @return builder
     */
    public MappingImpl produces(MediaType... mediaType) {
        return produces(Arrays.stream(mediaType)
                .map(MediaType::value)
                .toArray(String[]::new));
    }

    /**
     * Adds some produces predicates
     *
     * @param produces produces
     * @return builder
     */
    public MappingImpl produces(String... produces) {
        if (produces != null && produces.length > 0) {
            Set<String> tmp;
            if (this.produces != null && this.produces.length > 0) {
                tmp = new LinkedHashSet<>(this.produces.length + produces.length);
                tmp.addAll(Arrays.asList(this.produces));
            } else {
                tmp = new LinkedHashSet<>(produces.length);
            }
            for (String p : produces) {
                tmp.add(MediaTypeUtil.parseMediaType(p).value());
            }
            return new MappingImpl(this.name,
                    this.path,
                    this.method,
                    this.params,
                    this.headers,
                    this.consumes,
                    tmp.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String[] path() {
        return path;
    }

    @Override
    public HttpMethod[] method() {
        return method;
    }

    @Override
    public String[] params() {
        return params;
    }

    @Override
    public String[] headers() {
        return headers;
    }

    @Override
    public String[] consumes() {
        return consumes;
    }

    @Override
    public String[] produces() {
        return produces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MappingImpl mapping = (MappingImpl) o;
        return Objects.equals(name, mapping.name) &&
                Arrays.equals(path, mapping.path) &&
                Arrays.equals(method, mapping.method) &&
                Arrays.equals(params, mapping.params) &&
                Arrays.equals(headers, mapping.headers) &&
                Arrays.equals(consumes, mapping.consumes) &&
                Arrays.equals(produces, mapping.produces);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(path);
        result = 31 * result + Arrays.hashCode(method);
        result = 31 * result + Arrays.hashCode(params);
        result = 31 * result + Arrays.hashCode(headers);
        result = 31 * result + Arrays.hashCode(consumes);
        result = 31 * result + Arrays.hashCode(produces);
        return result;
    }

    @Override
    public String toString() {
        if (str == null) {
            List<String> fragments = new LinkedList<>();
            if (StringUtils.isNotEmpty(name)) {
                fragments.add("name=" + name);
            }
            if (path != null && path.length > 0) {
                fragments.add("path=" + Arrays.asList(path));
            }
            if (method != null && method.length > 0) {
                fragments.add("method=" + Arrays.asList(method));
            }

            if (params != null && params.length > 0) {
                fragments.add("params=" + Arrays.asList(params));
            }

            if (headers != null && headers.length > 0) {
                fragments.add("headers=" + Arrays.asList(headers));
            }

            if (consumes != null && consumes.length > 0) {
                fragments.add("consumes=" + Arrays.asList(consumes));
            }

            if (produces != null && produces.length > 0) {
                fragments.add("produces=" + Arrays.asList(produces));
            }
            str = String.join(",", fragments);
        }
        return str;
    }
}
