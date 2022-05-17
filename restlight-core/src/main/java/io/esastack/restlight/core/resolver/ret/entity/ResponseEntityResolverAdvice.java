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
package io.esastack.restlight.core.resolver.ret.entity;

import io.esastack.restlight.core.resolver.ResolverExecutor;
import io.esastack.restlight.core.resolver.ret.ReturnValueResolverAdvice;

/**
 * Allows customizing the return value of handler before resolving it to byte array.
 */
public interface ResponseEntityResolverAdvice extends ReturnValueResolverAdvice<ResponseEntityResolverContext> {

    /**
     * This method will be called around
     * {@link ResponseEntityResolver#resolve(ResponseEntityResolverContext)} .
     *
     * @param executor executor
     * @throws Exception exception
     */
    @Override
    default Void aroundResolve(ResolverExecutor<ResponseEntityResolverContext> executor) throws Exception {
        aroundResolve0(executor);
        return null;
    }

    void aroundResolve0(ResolverExecutor<ResponseEntityResolverContext> executor) throws Exception;
}
