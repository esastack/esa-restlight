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

class AdvisedArgumentResolver implements ArgumentResolver {

    private final ArgumentResolver resolver;
    private final List<ArgumentResolverAdvice> advices;

    AdvisedArgumentResolver(ArgumentResolver resolver, List<ArgumentResolverAdvice> advices) {
        Checks.checkNotNull(resolver, "resolver");
        Checks.checkNotEmptyArg(advices, "advices");
        this.resolver = resolver;
        this.advices = Collections.unmodifiableList(advices);
    }

    @Override
    public Object resolve(AsyncRequest request, AsyncResponse response) throws Exception {
        for (ArgumentResolverAdvice advice : advices) {
            advice.beforeResolve(request, response);
        }

        Object resolved = resolver.resolve(request, response);

        for (ArgumentResolverAdvice advice : advices) {
            resolved = advice.afterResolved(resolved, request, response);
        }
        return resolved;
    }
}
