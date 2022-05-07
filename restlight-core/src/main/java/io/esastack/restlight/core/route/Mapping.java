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
package io.esastack.restlight.core.route;

import io.esastack.commons.net.http.HttpMethod;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.route.impl.MappingImpl;
import io.esastack.restlight.core.util.MappingUtils;

/**
 * A Mapping maintains some predicates which will be used to determine whether a request should be routed to a {@link
 * Route}.
 */
public interface Mapping {

    /**
     * Creates an instance of {@link MappingImpl} for building a new {@link Mapping}.
     *
     * @return builder
     */
    static MappingImpl mapping() {
        return new MappingImpl();
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#path()} to given path for building a new
     * {@link Mapping}.
     *
     * @param path path
     * @return builder
     */
    static MappingImpl mapping(String path) {
        return mapping().path(path);
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#path()} and {@link Mapping#method()} to
     * given path and method for building a new {@link Mapping}.
     *
     * @param path   path
     * @param method method
     * @return builder
     */
    static MappingImpl mapping(String path, HttpMethod method) {
        return mapping().path(path)
                .method(method);
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#method()}} to {@link HttpMethod#GET} for
     * building a new {@link Mapping}.
     *
     * @return builder
     */
    static MappingImpl get() {
        return mapping().get();
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#method()}} to {@link HttpMethod#GET} and
     * set the {@link Mapping#path()} to given path for building a new {@link Mapping}.
     *
     * @param path path
     * @return builder
     */
    static MappingImpl get(String path) {
        return mapping().get(path);
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#method()}} to {@link HttpMethod#POST} for
     * building a new {@link Mapping}.
     *
     * @return builder
     */
    static MappingImpl post() {
        return mapping().post();
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#method()}} to {@link HttpMethod#POST} and
     * set the {@link Mapping#path()} to given path for building a new {@link Mapping}.
     *
     * @param path path
     * @return builder
     */
    static MappingImpl post(String path) {
        return mapping().post(path);
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#method()}} to {@link HttpMethod#PUT} for
     * building a new {@link Mapping}.
     *
     * @return builder
     */
    static MappingImpl put() {
        return mapping().put();
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#method()}} to {@link HttpMethod#PUT} and
     * set the {@link Mapping#path()} to given path for building a new {@link Mapping}.
     *
     * @param path path
     * @return builder
     */
    static MappingImpl put(String path) {
        return mapping().put(path);
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#method()}} to {@link HttpMethod#DELETE} for
     * building a new {@link Mapping}.
     *
     * @return builder
     */
    static MappingImpl delete() {
        return mapping().delete();
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#method()}} to {@link HttpMethod#DELETE} and
     * set the {@link Mapping#path()} to given path for building a new {@link Mapping}.
     *
     * @param path path
     * @return builder
     */
    static MappingImpl delete(String path) {
        return mapping().delete(path);
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#method()}} to {@link HttpMethod#PATCH} for
     * building a new {@link Mapping}.
     *
     * @return builder
     */
    static MappingImpl patch() {
        return mapping().patch();
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#method()}} to {@link HttpMethod#PATCH} and
     * set the {@link Mapping#path()} to given path for building a new {@link Mapping}.
     *
     * @param path path
     * @return builder
     */
    static MappingImpl patch(String path) {
        return mapping().patch(path);
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#method()}} to {@link HttpMethod#HEAD} for
     * building a new {@link Mapping}.
     *
     * @return builder
     */
    static MappingImpl head() {
        return mapping().head();
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#method()}} to {@link HttpMethod#HEAD} and
     * set the {@link Mapping#path()} to given path for building a new {@link Mapping}.
     *
     * @param path path
     * @return builder
     */
    static MappingImpl head(String path) {
        return mapping().head(path);
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#method()}} to {@link HttpMethod#OPTIONS}
     * for building a new {@link Mapping}.
     *
     * @return builder
     */
    static MappingImpl options() {
        return mapping().options();
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#method()}} to {@link HttpMethod#OPTIONS}
     * and set the {@link Mapping#path()} to given path for building a new {@link Mapping}.
     *
     * @param path path
     * @return builder
     */
    static MappingImpl options(String path) {
        return mapping().options(path);
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#method()}} to {@link HttpMethod#TRACE} for
     * building a new {@link Mapping}.
     *
     * @return builder
     */
    static MappingImpl trace() {
        return mapping().trace();
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#method()}} to {@link HttpMethod#OPTIONS}
     * and set the {@link Mapping#path()} to given path for building a new {@link Mapping}.
     *
     * @param path path
     * @return builder
     */
    static MappingImpl trace(String path) {
        return mapping().trace(path);
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#method()}} to {@link HttpMethod#CONNECT}
     * for building a new {@link Mapping}.
     *
     * @return builder
     */
    static MappingImpl connect() {
        return mapping().connect();
    }

    /**
     * Creates an instance of {@link MappingImpl} and set the {@link Mapping#method()}} to {@link HttpMethod#CONNECT}
     * and set the {@link Mapping#path()} to given path for building a new {@link Mapping}.
     *
     * @param path path
     * @return builder
     */
    static MappingImpl connect(String path) {
        return mapping().connect(path);
    }

    /**
     * Returns the name of current {@link Mapping}.
     *
     * @return name
     */
    default String name() {
        return null;
    }

    /**
     * Return the path of current {@link Mapping}, which will be used to determine the routing by {@link
     * HttpRequest#path()}
     *
     * @return path, should not be {@code null}
     */
    String[] path();

    /**
     * Return the http method of current {@link Mapping}, which will be used to determine the routing by {@link
     * HttpRequest#method()}
     *
     * @return http method, should not be {@code null}
     */
    HttpMethod[] method();

    /**
     * Return the parameter predicates of current {@link Mapping}, which will be used to determine the routing by {@link
     * HttpRequest#getParam(String)}
     *
     * @return http parameters, should not be {@code null}
     */
    String[] params();

    /**
     * Return the header predicates of current {@link Mapping}, which will be used to determine the routing by {@link
     * HttpRequest#headers()}
     *
     * @return http headers, should not be {@code null}
     */
    String[] headers();

    /**
     * Return the consumes predicates of current {@link Mapping}, which will be used to determine the routing by
     * Content-Type of {@link HttpRequest}.
     *
     * @return http headers, should not be {@code null}
     */
    String[] consumes();

    /**
     * Return the produces predicates of current {@link Mapping}, which will be used to determine the routing by Accept
     * of {@link HttpRequest}.
     *
     * @return http headers, should not be {@code null}
     */
    String[] produces();

    /**
     * Combines current {@link Mapping} with another {@link Mapping}, and current {@link Mapping} is regarded as parent
     * {@link Mapping}, given {@link Mapping} is regarded as child {@link Mapping} on the other hand.
     * <p>
     * Node that entries of {@link Mapping#headers()} may be removed if the key of the entry is a 'Content-Type' or
     * 'Accept'.
     *
     * @param child child mapping
     * @return combined
     */
    default Mapping combine(Mapping child) {
        return MappingUtils.combine(this, child);
    }

}
