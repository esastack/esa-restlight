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
package io.esastack.restlight.ext.filter.ipwhitelist;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import esa.commons.StringUtils;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.httpserver.core.HttpResponse;
import io.esastack.restlight.server.context.FilterContext;
import io.esastack.restlight.server.handler.FilterChain;
import io.esastack.restlight.server.spi.Filter;
import io.esastack.restlight.server.util.ErrorDetail;
import io.esastack.restlight.server.util.Futures;
import io.esastack.restlight.server.util.LoggerUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class IpWhiteListFilter implements Filter {

    /**
     * Regex prefix
     */
    private static final String REGEX_PREFIX = "regex:";
    private static final int EFFECTIVE_EXPIRE = 500;

    /**
     * Hold a cache
     */
    private final LoadingCache<String, Boolean> cache;
    private final Predicate<String> predicate;

    public IpWhiteListFilter(IpWhiteListOptions options) {
        List<Predicate<String>> predicates = buildPredicates(options.getIps());
        if (predicates.isEmpty()) {
            throw new IllegalArgumentException("Ip predicates must not be empty!");
        }
        this.predicate = ip -> predicates.stream().anyMatch(p -> p.test(ip));
        //init cache
        cache = buildCache(options.getCacheSize(), options.getExpire());
    }

    private LoadingCache<String, Boolean> buildCache(long cacheSize, long expire) {
        if (cacheSize > 0) {
            if (expire > EFFECTIVE_EXPIRE) {
                return CacheBuilder.newBuilder()
                        .maximumSize(cacheSize)
                        .expireAfterAccess(expire, TimeUnit.MILLISECONDS)
                        .build(new CacheLoader<String, Boolean>() {
                            @Override
                            public Boolean load(String key) {
                                return predicate.test(key);
                            }
                        });
            } else {
                return CacheBuilder.newBuilder()
                        .maximumSize(cacheSize)
                        .build(new CacheLoader<String, Boolean>() {
                            @Override
                            public Boolean load(String key) {
                                return predicate.test(key);
                            }
                        });
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<Void> doFilter(FilterContext context, FilterChain<FilterContext> chain) {
        final HttpRequest request = context.request();
        final HttpResponse response = context.response();

        String ip = getRemoteAddr(request);
        boolean valid = false;
        if (ip != null) {
            if (cache == null) {
                valid = predicate.test(ip);
            } else {
                valid = cache.getUnchecked(ip);
            }
        }
        if (!valid && !response.isCommitted()) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, MediaType.TEXT_PLAIN.value());
            response.sendResult(HttpResponseStatus.UNAUTHORIZED.code(),
                    ErrorDetail.buildErrorMsg(request.path(),
                            HttpResponseStatus.UNAUTHORIZED.reasonPhrase(),
                            HttpResponseStatus.UNAUTHORIZED.reasonPhrase(), HttpResponseStatus.UNAUTHORIZED.code()));
            LoggerUtils.logger().warn("Unauthorized client ip address: {}", ip);
            return Futures.completedFuture();
        }
        return chain.doFilter(context);
    }

    /**
     * Get remote ip address.
     *
     * @param request request
     *
     * @return ip
     */
    protected String getRemoteAddr(HttpRequest request) {
        return request.remoteAddr();
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 100;
    }

    private List<Predicate<String>> buildPredicates(Collection<String> ipPredicates) {
        List<Predicate<String>> predicates = new ArrayList<>();
        if (ipPredicates == null || ipPredicates.isEmpty()) {
            return predicates;
        }

        for (String ipPredicate : ipPredicates) {
            if (StringUtils.isEmpty(ipPredicate)) {
                continue;
            }
            if (ipPredicate.startsWith(REGEX_PREFIX)) {
                //resolve regex ip
                predicates.add(new RegexPredicate(ipPredicate.substring(REGEX_PREFIX.length())));
            } else {
                //resolve real ip
                predicates.add(ipPredicate::equals);
            }
        }
        return predicates;
    }

    /**
     * Predicate for the regex condition.
     */
    private static class RegexPredicate implements Predicate<String> {

        private final Pattern pattern;

        private RegexPredicate(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        @Override
        public boolean test(String s) {
            return pattern.matcher(s).matches();
        }
    }
}
