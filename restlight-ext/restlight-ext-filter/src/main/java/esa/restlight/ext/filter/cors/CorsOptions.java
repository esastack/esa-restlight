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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CorsOptions {

    static final String ANY_ORIGIN = "*";
    static final Set<HttpMethod> DEFAULT_ALLOW_METHODS =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(HttpMethod.GET,
                    HttpMethod.POST,
                    HttpMethod.PUT,
                    HttpMethod.DELETE,
                    HttpMethod.HEAD)));
    static final Set<String> DEFAULT_EXPOSE_HEADERS =
            Collections.singleton(ANY_ORIGIN);
    static final Set<String> DEFAULT_ALLOW_HEADERS =
            Collections.singleton(ANY_ORIGIN);

    private boolean anyOrigin = true;
    private Set<String> origins = new HashSet<>();
    private Set<String> exposeHeaders = new HashSet<>(DEFAULT_EXPOSE_HEADERS);
    private boolean allowCredentials = true;
    private Set<HttpMethod> allowMethods = new HashSet<>(DEFAULT_ALLOW_METHODS);
    private Set<String> allowHeaders = new HashSet<>(DEFAULT_ALLOW_HEADERS);
    private long maxAge = 24L * 60L * 60L;

    public boolean isAnyOrigin() {
        return anyOrigin;
    }

    public void setAnyOrigin(boolean anyOrigin) {
        this.anyOrigin = anyOrigin;
    }

    public Set<String> getOrigins() {
        return origins;
    }

    public void setOrigins(Set<String> origins) {
        this.origins = origins;
    }

    public Set<String> getExposeHeaders() {
        return exposeHeaders;
    }

    public void setExposeHeaders(Set<String> exposeHeaders) {
        this.exposeHeaders = exposeHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public Set<HttpMethod> getAllowMethods() {
        return allowMethods;
    }

    public void setAllowMethods(Set<HttpMethod> allowMethods) {
        this.allowMethods = allowMethods;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

    public Set<String> getAllowHeaders() {
        return allowHeaders;
    }

    public void setAllowHeaders(Set<String> allowHeaders) {
        this.allowHeaders = allowHeaders;
    }
}
