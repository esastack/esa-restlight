/*
 * Copyright 2022 OPPO ESA Stack Project
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
package io.esastack.restlight.core.resolver.entity.response;

import io.esastack.restlight.core.handler.HandlerPredicate;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.context.HttpRequest;

import java.util.List;

/**
 * This predicate is designed to estimate whether the given {@link ResponseEntityResolver} and
 * {@link ResponseEntityResolverAdvice} can be bound to given {@link HandlerMethod} at start up time.
 * Besides this, the {@link #alsoApplyWhenMissingHandler()} is also defined to decide whether current
 * resolver or advice should be used when routed failed, in this case, {@link HandlerMethod} is
 * always {@code null}.
 *
 * @see ResponseEntityResolverAdapter
 * @see ResponseEntityResolverFactory
 * @see ResponseEntityResolverAdviceAdapter
 * @see ResponseEntityResolverAdviceFactory
 */
public interface ResponseEntityResolverPredicate extends HandlerPredicate {

    /**
     * The result of this method is used to decides whether current component
     * can bind to {@link HttpRequest} which failed to route to a handler.
     *
     * !NOTE: if returns {@code true}, {@link HandlerMethod} in
     * {@link ResponseEntityResolverFactory#createResolver(HandlerMethod, List)} and
     * {@link ResponseEntityResolverAdviceFactory#createResolverAdvice(HandlerMethod)} may be {@code null}.
     *
     * @return  {@code true} means that current component can be use when failed to route to a handler,
     *          otherwise {@code false}.
     */
    default boolean alsoApplyWhenMissingHandler() {
        return false;
    }

}

