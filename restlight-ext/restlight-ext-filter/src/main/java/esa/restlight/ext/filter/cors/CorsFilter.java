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

import esa.commons.Checks;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.server.handler.Filter;
import esa.restlight.server.handler.FilterChain;
import esa.restlight.server.util.Futures;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

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
    public CompletableFuture<Void> doFilter(AsyncRequest request, AsyncResponse response, FilterChain chain) {
        final String origin = request.getHeader(HttpHeaderNames.ORIGIN);
        final CachedOpt opt = forOrigin(origin);
        if (isPreflightRequest(request, origin)) {
            String set = setOrigin(response, origin, opt);
            if (set != null) {
                setAllowMethods(response, opt);
                setAllowHeaders(response, opt);
                setAllowCredentials(response, opt, origin);
                setMaxAge(response, opt);
            }
            response.sendResult(HttpResponseStatus.OK.code());
            return Futures.completedFuture();
        } else if (origin != null && opt == null) {
            // origin present in request but missing cors options
            response.sendResult(HttpResponseStatus.FORBIDDEN.code());
            return Futures.completedFuture();
        } else {
            String set = setOrigin(response, origin, opt);
            if (set != null) {
                setAllowCredentials(response, opt, origin);
                setExposeHeaders(response, opt);
            }
            return chain.doFilter(request, response);
        }
    }

    private static void setOrigin(AsyncResponse response, String origin) {
        response.setHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
    }

    private static String setOrigin(AsyncResponse response, String origin, CachedOpt c) {
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

    private static void setExposeHeaders(AsyncResponse response, CachedOpt opt) {
        if (opt.exposeHeadersStr != null) {
            response.setHeader(HttpHeaderNames.ACCESS_CONTROL_EXPOSE_HEADERS, opt.exposeHeadersStr);
        }
    }

    private static void setAllowHeaders(AsyncResponse response, CachedOpt opt) {
        if (opt.allowHeadersStr != null) {
            response.setHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, opt.allowHeadersStr);
        }
    }

    private static void setAllowMethods(AsyncResponse response, CachedOpt opt) {
        response.setHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, opt.allowMethodsStr);
    }

    private static void setAllowCredentials(AsyncResponse response, CachedOpt opt, String origin) {
        if (opt.theOpt.isAllowCredentials()
                && !origin.equals(CorsOptions.ANY_ORIGIN)) {
            response.setHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }
    }

    private static void setMaxAge(AsyncResponse response, CachedOpt opt) {
        if (opt.maxAgeStr != null) {
            response.setHeader(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE, opt.maxAgeStr);
        }
    }

    private static void setVaryHeader(AsyncResponse response) {
        response.setHeader(HttpHeaderNames.VARY, HttpHeaderNames.ORIGIN.toString());
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

    private static boolean isPreflightRequest(AsyncRequest request, String origin) {
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
                    .map(esa.restlight.core.method.HttpMethod::name)
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
