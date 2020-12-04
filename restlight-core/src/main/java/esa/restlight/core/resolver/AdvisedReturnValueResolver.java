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
package esa.restlight.core.resolver;

import esa.commons.Checks;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;

import java.util.Collections;
import java.util.List;

/**
 * AdvisedArgumentResolver
 */
class AdvisedReturnValueResolver implements ReturnValueResolver {

    private final ReturnValueResolver resolver;
    private final List<ReturnValueResolverAdvice> advices;

    AdvisedReturnValueResolver(ReturnValueResolver resolver, List<ReturnValueResolverAdvice> advices) {
        Checks.checkNotNull(resolver, "resolver");
        Checks.checkNotEmptyArg(advices, "advices");
        this.resolver = resolver;
        this.advices = Collections.unmodifiableList(advices);
    }

    @Override
    public byte[] resolve(Object returnValue, AsyncRequest request, AsyncResponse response) throws Exception {
        for (ReturnValueResolverAdvice advice : advices) {
            returnValue = advice.beforeResolve(returnValue, request, response);
        }

        return resolver.resolve(returnValue, request, response);
    }
}
