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

package io.esastack.restlight.core.resolver.ret;

import esa.commons.Checks;
import esa.commons.Result;
import io.esastack.restlight.core.exception.WebServerException;
import io.esastack.restlight.core.resolver.ResolverExecutor;

public class ReturnValueResolverExecutor implements ResolverExecutor<ReturnValueResolverContext> {

    private final ReturnValueResolverContext context;
    private final ReturnValueResolver[] resolvers;
    private final ReturnValueResolverAdvice[] advices;
    private final int advicesSize;
    private int index;
    private final String unSupportMsg;

    public ReturnValueResolverExecutor(ReturnValueResolverContext context, ReturnValueResolver[] resolvers,
                                       ReturnValueResolverAdvice[] advices, String unSupportMsg) {
        Checks.checkNotNull(context, "context");
        Checks.checkNotNull(resolvers, "resolvers");
        Checks.checkNotNull(unSupportMsg, "unSupportMsg");
        this.context = context;
        this.resolvers = resolvers;
        this.advices = advices;
        this.advicesSize = (advices == null ? 0 : advices.length);
        this.unSupportMsg = unSupportMsg;
    }

    @Override
    public ReturnValueResolverContext context() {
        return context;
    }

    @Override
    public Object proceed() throws Exception {
        if (advices == null || index >= advicesSize) {
            Result<?, ?> handled;
            for (ReturnValueResolver resolver : resolvers) {
                handled = resolver.resolve(context);
                if (handled.isOk()) {
                    return handled.get();
                }
            }
            throw WebServerException.notSupported(unSupportMsg);
        }

        return advices[index++].aroundResolve(this);
    }
}
