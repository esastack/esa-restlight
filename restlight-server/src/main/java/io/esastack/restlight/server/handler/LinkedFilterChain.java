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
package io.esastack.restlight.server.handler;

import esa.commons.Checks;
import io.esastack.restlight.server.context.FilterContext;
import io.esastack.restlight.server.internal.InternalFilter;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Implementation of {@link FilterChain} which maintains a reference of {@link InternalFilter} and a reference of the
 * next {@link FilterChain} which would be passed to the {@link InternalFilter#doFilter(FilterContext, FilterChain)}
 * function of {@link #current} as the third argument.
 */
public class LinkedFilterChain<FCTX extends FilterContext> implements FilterChain<FCTX> {

    private final InternalFilter<FCTX> current;
    private final FilterChain<FCTX> next;

    private LinkedFilterChain(InternalFilter<FCTX> current,
                              FilterChain<FCTX> next) {
        Checks.checkNotNull(current, "current");
        Checks.checkNotNull(next, "next");
        this.current = current;
        this.next = next;
    }

    /**
     * Return a immutable filter chain, all the instances of {@link LinkedFilterChain} will be instantiated ahead.
     *
     * @param filters filters
     *
     * @return filter chain
     */
    public static <FC extends FilterContext> LinkedFilterChain<FC> immutable(List<InternalFilter<FC>> filters,
                                                                             Function<FC, CompletableFuture<Void>>
                                                                                     action) {
        Checks.checkNotEmptyArg(filters, "filters must not be empty");
        // link all the filter and the given action(last)
        FilterChain<FC> next = action::apply;
        LinkedFilterChain<FC> chain;
        int i = filters.size() - 1;
        do {
            chain = new LinkedFilterChain<>(filters.get(i), next);
            next = chain;
        } while (--i >= 0);
        return chain;
    }

    @Override
    public CompletableFuture<Void> doFilter(FCTX context) {
        return current.doFilter(context, next);
    }
}
