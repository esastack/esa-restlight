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
package io.esastack.restlight.ext.filter.cors;

import esa.commons.Checks;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.commons.net.http.HttpStatus;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.httpserver.core.HttpResponse;
import io.esastack.restlight.server.context.FilterContext;
import io.esastack.restlight.server.handler.FilterChain;
import io.esastack.restlight.server.spi.Filter;
import io.esastack.restlight.server.util.Futures;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CorsFilter implements Filter {

    private final CachedOpt[] options;

    public CorsFilter(List<CorsOptions> options) {
        Checks.checkNotEmptyArg(options);
        this.options = options.stream().map(CachedOpt::new).toArray(CachedOpt[]::new);
    }

    @Override
    public CompletableFuture<Void> doFilter(FilterContext context, FilterChain<FilterContext> chain) {
        final HttpRequest request = context.request();
        final HttpResponse response = context.response();
        final String origin = request.headers().get(HttpHeaderNames.ORIGIN);
        final CachedOpt opt = forOrigin(origin);
        if (isPreflightRequest(request, origin)) {
            String set = setOrigin(response, origin, opt);
            if (set != null) {
                setAllowMethods(response, opt);
                setAllowHeaders(response, opt);
                setAllowCredentials(response, opt, origin);
                setMaxAge(response, opt);
            }
            response.sendResult(HttpStatus.OK.code());
            return Futures.completedFuture();
        } else if (origin != null && opt == null) {
            // origin present in request but missing cors options
            response.sendResult(HttpStatus.FORBIDDEN.code());
            return Futures.completedFuture();
        } else {
            String set = setOrigin(response, origin, opt);
            if (set != null) {
                setAllowCredentials(response, opt, origin);
                setExposeHeaders(response, opt);
            }
            return chain.doFilter(context);
        }
    }

    private static void setOrigin(HttpResponse response, String origin) {
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
    }

    private static String setOrigin(HttpResponse response, String origin, CachedOpt c) {
        if (origin != null && c != null) {
            final CorsOptions opt = c.theOpt;
            if (opt.isAnyOrigin()) {
                if (opt.isAllowCredentials()) {
                    setOrigin(response, origin);
                    setVaryHeader(response);
                    return origin;
                } else {
                    setOrigin(response, CorsOptions.ANY_ORIGIN);
                    return CorsOptions.ANY_ORIGIN;
                }
            } else if (opt.getOrigins().contains(origin)) {
                setOrigin(response, origin);
                setVaryHeader(response);
                return origin;
            }
            // miss
        }
        return null;
    }

    private static void setExposeHeaders(HttpResponse response, CachedOpt opt) {
        if (opt.exposeHeadersStr != null) {
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_EXPOSE_HEADERS, opt.exposeHeadersStr);
        }
    }

    private static void setAllowHeaders(HttpResponse response, CachedOpt opt) {
        if (opt.allowHeadersStr != null) {
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, opt.allowHeadersStr);
        }
    }

    private static void setAllowMethods(HttpResponse response, CachedOpt opt) {
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, opt.allowMethodsStr);
    }

    private static void setAllowCredentials(HttpResponse response, CachedOpt opt, String origin) {
        if (opt.theOpt.isAllowCredentials()
                && !origin.equals(CorsOptions.ANY_ORIGIN)) {
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }
    }

    private static void setMaxAge(HttpResponse response, CachedOpt opt) {
        if (opt.maxAgeStr != null) {
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE, opt.maxAgeStr);
        }
    }

    private static void setVaryHeader(HttpResponse response) {
        response.headers().set(HttpHeaderNames.VARY, HttpHeaderNames.ORIGIN);
    }

    private CachedOpt forOrigin(String origin) {
        for (CachedOpt opt : options) {
            if (opt.theOpt.isAnyOrigin()) {
                return opt;
            }
            if (opt.theOpt.getOrigins().contains(origin)) {
                return opt;
            }
        }
        return null;
    }

    private static boolean isPreflightRequest(HttpRequest request, String origin) {
        return HttpMethod.OPTIONS.equals(request.method()) &&
                origin != null &&
                request.headers().contains(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 500;
    }

    private static class CachedOpt {
        private final CorsOptions theOpt;
        private final String allowMethodsStr;
        private final String allowHeadersStr;
        private final String exposeHeadersStr;
        private final String maxAgeStr;

        private CachedOpt(CorsOptions theOpt) {
            Checks.checkNotNull(theOpt);
            this.theOpt = theOpt;
            this.allowMethodsStr = theOpt.getAllowMethods().stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(","));

            if (theOpt.getAllowHeaders() == null || theOpt.getAllowHeaders().isEmpty()) {
                this.allowHeadersStr = null;
            } else {
                this.allowHeadersStr = String.join(",", theOpt.getAllowHeaders());
            }
            if (theOpt.getExposeHeaders() == null || theOpt.getExposeHeaders().isEmpty()) {
                this.exposeHeadersStr = null;
            } else {
                this.exposeHeadersStr = String.join(",", theOpt.getExposeHeaders());
            }
            if (theOpt.getMaxAge() < 0L) {
                this.maxAgeStr = null;
            } else {
                this.maxAgeStr = String.valueOf(theOpt.getMaxAge());
            }
        }
    }
}
