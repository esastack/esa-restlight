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
package esa.restlight.ext.filter.cors;

import esa.restlight.core.method.HttpMethod;

import java.util.HashSet;
import java.util.Set;

import static esa.restlight.ext.filter.cors.CorsOptions.*;

public final class CorsOptionsConfigure {
    private boolean anyOrigin = true;
    private Set<String> origins = new HashSet<>();
    private Set<String> exposeHeaders = new HashSet<>(DEFAULT_EXPOSE_HEADERS);
    private boolean allowCredentials = true;
    private Set<HttpMethod> allowMethods = new HashSet<>(DEFAULT_ALLOW_METHODS);
    private Set<String> allowHeaders = new HashSet<>(DEFAULT_ALLOW_HEADERS);
    private long maxAge = 24L * 60L * 60L;

    private CorsOptionsConfigure() {
    }

    public static CorsOptions defaultOpts() {
        return new CorsOptionsConfigure().configured();
    }

    public static CorsOptionsConfigure newOpts() {
        return new CorsOptionsConfigure();
    }

    public CorsOptionsConfigure anyOrigin(boolean anyOrigin) {
        this.anyOrigin = anyOrigin;
        return this;
    }

    public CorsOptionsConfigure origins(Set<String> origins) {
        this.origins = origins;
        return this;
    }

    public CorsOptionsConfigure exposeHeaders(Set<String> exposeHeaders) {
        this.exposeHeaders = exposeHeaders;
        return this;
    }

    public CorsOptionsConfigure allowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
        return this;
    }

    public CorsOptionsConfigure allowMethods(Set<HttpMethod> allowMethods) {
        this.allowMethods = allowMethods;
        return this;
    }

    public CorsOptionsConfigure allowHeaders(Set<String> allowHeaders) {
        this.allowHeaders = allowHeaders;
        return this;
    }

    public CorsOptionsConfigure maxAge(long maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public CorsOptions configured() {
        CorsOptions corsOptions = new CorsOptions();
        corsOptions.setAnyOrigin(anyOrigin);
        corsOptions.setOrigins(origins);
        corsOptions.setExposeHeaders(exposeHeaders);
        corsOptions.setAllowCredentials(allowCredentials);
        corsOptions.setAllowMethods(allowMethods);
        corsOptions.setAllowHeaders(allowHeaders);
        corsOptions.setMaxAge(maxAge);
        return corsOptions;
    }
}
