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
package io.esastack.restlight.ext.interceptor.signature;

import esa.commons.Checks;
import io.esastack.restlight.core.interceptor.HandlerInterceptor;
import io.esastack.restlight.ext.interceptor.config.SignatureOptions;

public abstract class AbstractSignatureHandlerInterceptor extends AbstractSignatureInterceptor
        implements HandlerInterceptor {

    private final SignValidationScope scope;

    public AbstractSignatureHandlerInterceptor(SignatureOptions options, SecretProvider secretProvider,
                                               SignValidationScope scope) {
        super(options, secretProvider);
        Checks.checkNotNull(scope, "scope");
        this.scope = scope;
    }

    @Override
    public String[] includes() {
        return scope.includes();
    }

    @Override
    public String[] excludes() {
        return scope.excludes();
    }
}
