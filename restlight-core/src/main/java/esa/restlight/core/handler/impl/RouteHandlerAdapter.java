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
package esa.restlight.core.handler.impl;

import esa.commons.collection.MultiValueMap;
import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.handler.RouteHandler;
import esa.restlight.core.interceptor.Interceptor;
import esa.restlight.core.interceptor.InterceptorPredicate;
import esa.restlight.core.interceptor.InternalInterceptor;
import esa.restlight.core.resolver.ExceptionResolver;
import esa.restlight.core.resolver.HandlerResolverFactory;
import esa.restlight.core.util.Ordered;
import esa.restlight.core.util.OrderedComparator;
import esa.restlight.core.util.RouteUtils;
import esa.restlight.server.route.RouteExecution;
import esa.restlight.server.route.predicate.RequestPredicate;
import io.netty.util.concurrent.FastThreadLocal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An adapter of {@link RouteHandler} which is allowed to handle request with {@link Interceptor}('s) and {@link
 * ExceptionResolver}('s).
 */
public class RouteHandlerAdapter extends HandlerAdapter<RouteHandler> implements RouteHandler {

    private final RouteExecutionFactory executionFactory;
    private final ExceptionResolver<Throwable> exceptionResolver;
    private final Matcher interceptorMatcher;

    public RouteHandlerAdapter(RouteHandler handler,
                               HandlerResolverFactory factory,
                               MultiValueMap<InterceptorPredicate, Interceptor> interceptors,
                               ExceptionResolver<Throwable> exceptionResolver) {
        super(handler, factory);
        this.interceptorMatcher = maybeMatchable(interceptors);
        this.executionFactory = RouteUtils.routeExecutionFactory(handler.handler().method().getReturnType());
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    public boolean intercepted() {
        return handler.intercepted();
    }

    @Override
    public String scheduler() {
        return handler.scheduler();
    }

    public RouteExecution toExecution(AsyncRequest request) {
        return executionFactory.getRouteExecution(this, getMatchingInterceptors(request));
    }

    List<InternalInterceptor> getMatchingInterceptors(AsyncRequest request) {
        if (handler.intercepted()) {
            return interceptorMatcher.match(request);
        }
        return null;
    }

    ExceptionResolver<Throwable> exceptionResolver() {
        return exceptionResolver;
    }

    /**
     * Converts the given {@link Interceptor} to {@link Matcher}.
     */
    private Matcher maybeMatchable(MultiValueMap<InterceptorPredicate, Interceptor> interceptorLookup) {
        final List<InterceptorMapping> mappings = new ArrayList<>();
        boolean hasComplexMapping = false;
        if (interceptorLookup != null && !interceptorLookup.isEmpty()) {
            // flat map
            for (Map.Entry<InterceptorPredicate, List<Interceptor>> entry : interceptorLookup.entrySet()) {
                final InterceptorPredicate predicate = entry.getKey();
                List<Interceptor> interceptors = entry.getValue();

                if (interceptors != null && !interceptors.isEmpty()) {
                    // compute the affinity
                    int affinity = 0;
                    for (Interceptor i : interceptors) {
                        affinity += i.affinity();
                    }

                    if (interceptors.size() > 1 && affinity > 20) {
                        hasComplexMapping = true;
                        final FastThreadLocal<Boolean> shared = new FastThreadLocal<>();
                        for (InternalInterceptor interceptor : interceptors) {
                            // there's no need to match the predicate repeatedly, we can only match it first time and
                            // make a cache of match result so that the next matching can just use it.
                            mappings.add(new CachedInterceptorMapping(predicate, interceptor, shared));
                        }
                    } else {
                        for (InternalInterceptor interceptor : interceptors) {
                            mappings.add(new InterceptorMapping(predicate, interceptor));
                        }
                    }
                }
            }
        }

        // keep in sort
        OrderedComparator.sort(mappings);
        if (hasComplexMapping) {
            return new CachedMatcher(mappings);
        } else {
            return new Matcher(mappings);
        }
    }

    private static class Matcher {
        final List<InterceptorMapping> interceptorMappings;

        private Matcher(List<InterceptorMapping> interceptorMappings) {
            this.interceptorMappings = interceptorMappings;
        }

        List<InternalInterceptor> match(AsyncRequest request) {
            //if lookup map is empty -> just return the all mapping interceptors
            if (interceptorMappings.isEmpty()) {
                return null;
            }

            // there's no need to sort this result set again, because the interceptorMappings is already kept in sort
            // and we had just matched these in order.
            return doMatch(request);
        }

        protected List<InternalInterceptor> doMatch(AsyncRequest request) {
            // match interceptors by order
            final List<InternalInterceptor> matchedInterceptors =
                    new LinkedList<>();
            //no way but to search from the whole collection
            for (InterceptorMapping mapping : interceptorMappings) {
                if (mapping.test(request)) {
                    matchedInterceptors.add(mapping.interceptor);
                }
            }
            return matchedInterceptors;
        }
    }

    private static class CachedMatcher extends Matcher {

        private final FastThreadLocal[] locals;

        private CachedMatcher(List<InterceptorMapping> interceptorMappings) {
            super(interceptorMappings);
            Set<FastThreadLocal<Boolean>> locals = new HashSet<>();
            for (InterceptorMapping mapping : interceptorMappings) {
                if (mapping instanceof CachedInterceptorMapping) {
                    locals.add(((CachedInterceptorMapping) mapping).matched);
                }
            }
            this.locals = locals.toArray(new FastThreadLocal[0]);
        }

        @Override
        protected List<InternalInterceptor> doMatch(AsyncRequest request) {
            try {
                return super.doMatch(request);
            } finally {
                // clear flags
                for (FastThreadLocal local : locals) {
                    local.remove();
                }
            }
        }
    }

    private static class InterceptorMapping implements Ordered, RequestPredicate {
        private final InterceptorPredicate predicate;
        private final InternalInterceptor interceptor;

        private InterceptorMapping(InterceptorPredicate predicate, InternalInterceptor interceptor) {
            this.predicate = predicate;
            this.interceptor = interceptor;
        }

        @Override
        public boolean test(AsyncRequest request) {
            return predicate.test(request);
        }

        @Override
        public int getOrder() {
            return interceptor.getOrder();
        }

    }

    private static class CachedInterceptorMapping extends InterceptorMapping {

        private final FastThreadLocal<Boolean> matched;

        private CachedInterceptorMapping(
                InterceptorPredicate condition,
                InternalInterceptor interceptor,
                FastThreadLocal<Boolean> local) {
            super(condition, interceptor);
            this.matched = local;
        }

        @Override
        public boolean test(AsyncRequest request) {
            Boolean isMatched = matched.getIfExists();
            if (isMatched == null) {
                boolean matchResult = super.test(request);
                this.matched.set(matchResult);
                return matchResult;
            } else {
                return isMatched;
            }
        }
    }
}
