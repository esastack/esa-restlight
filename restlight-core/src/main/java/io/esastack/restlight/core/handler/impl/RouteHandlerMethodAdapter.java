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
package io.esastack.restlight.core.handler.impl;

import esa.commons.Checks;
import esa.commons.collection.MultiValueMap;
import io.esastack.restlight.core.handler.HandlerMapping;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.handler.RouteFilter;
import io.esastack.restlight.core.interceptor.Interceptor;
import io.esastack.restlight.core.interceptor.InterceptorPredicate;
import io.esastack.restlight.core.interceptor.InternalInterceptor;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.RouteHandlerMethod;
import io.esastack.restlight.core.resolver.ExceptionResolver;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.core.util.OrderedComparator;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.route.RouteExecution;
import io.esastack.restlight.server.route.predicate.RequestPredicate;
import io.netty.util.concurrent.FastThreadLocal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An adapter of {@link RouteHandlerMethod} which is allowed to handle request with {@link Interceptor}('s) and {@link
 * ExceptionResolver}('s).
 */
public abstract class RouteHandlerMethodAdapter extends HandlerMethodAdapter<RouteHandlerMethod> {

    private final HandlerMapping mapping;
    private final RouteFilter[] filters;
    private final HandlerValueResolver handlerResolver;
    private final ExceptionResolver<Throwable> exceptionResolver;
    private final Matcher interceptorMatcher;

    RouteHandlerMethodAdapter(HandlerMapping mapping,
                              HandlerContext context,
                              HandlerValueResolver handlerResolver,
                              MultiValueMap<InterceptorPredicate, Interceptor> interceptors,
                              ExceptionResolver<Throwable> exceptionResolver) {
        super(context, mapping.methodInfo().handlerMethod());
        Checks.checkNotNull(handlerResolver, "handlerResolver");
        Checks.checkState(context.resolverFactory().isPresent(), "resolverFactory is null");
        this.mapping = mapping;
        this.filters = getMatchingFilters(context.resolverFactory().get(), handlerMethod()).toArray(new RouteFilter[0]);
        this.interceptorMatcher = maybeMatchable(interceptors);
        this.exceptionResolver = exceptionResolver;
        this.handlerResolver = handlerResolver;
    }

    /**
     * Builds a {@link RouteExecution} to handle the given {@link RequestContext}.
     *
     * @param context context
     * @return execution
     */
    public abstract RouteExecution toExecution(RequestContext context);

    List<RouteFilter> getMatchingFilters(HandlerResolverFactory handlerFactory, HandlerMethod method) {
        return handlerFactory.getRouteFilters(method);
    }

    List<InternalInterceptor> getMatchingInterceptors(RequestContext context) {
        if (handlerMethod().intercepted()) {
            return interceptorMatcher.match(context);
        }
        return null;
    }

    ExceptionResolver<Throwable> exceptionResolver() {
        return this.exceptionResolver;
    }

    HandlerValueResolver handlerResolver() {
        return handlerResolver;
    }

    HandlerMapping mapping() {
        return mapping;
    }

    RouteFilter[] filters() {
        return filters;
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

        List<InternalInterceptor> match(RequestContext context) {
            //if lookup map is empty -> just return the all mapping interceptors
            if (interceptorMappings.isEmpty()) {
                return null;
            }

            // there's no need to sort this result set again, because the interceptorMappings is already kept in sort
            // and we had just matched these in order.
            return doMatch(context);
        }

        protected List<InternalInterceptor> doMatch(RequestContext context) {
            // match interceptors by order
            final List<InternalInterceptor> matchedInterceptors =
                    new LinkedList<>();
            //no way but to search from the whole collection
            for (InterceptorMapping mapping : interceptorMappings) {
                if (mapping.test(context)) {
                    matchedInterceptors.add(mapping.interceptor);
                }
            }
            return matchedInterceptors;
        }
    }

    private static class CachedMatcher extends Matcher {

        private final FastThreadLocal<?>[] locals;

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
        protected List<InternalInterceptor> doMatch(RequestContext context) {
            try {
                return super.doMatch(context);
            } finally {
                // clear flags
                for (FastThreadLocal<?> local : locals) {
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
        public boolean test(RequestContext context) {
            return predicate.test(context);
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
        public boolean test(RequestContext context) {
            Boolean isMatched = matched.getIfExists();
            if (isMatched == null) {
                boolean matchResult = super.test(context);
                this.matched.set(matchResult);
                return matchResult;
            } else {
                return isMatched;
            }
        }
    }
}
