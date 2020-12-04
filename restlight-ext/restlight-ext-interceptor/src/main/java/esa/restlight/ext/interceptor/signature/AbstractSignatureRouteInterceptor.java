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
package esa.restlight.ext.interceptor.signature;

import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.interceptor.RouteInterceptor;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.ext.interceptor.annotation.IgnoreSignValidation;
import esa.restlight.ext.interceptor.annotation.SignValidation;
import esa.restlight.ext.interceptor.config.SignatureOptions;
import esa.restlight.server.route.Route;

public abstract class AbstractSignatureRouteInterceptor extends AbstractSignatureInterceptor
        implements RouteInterceptor {

    public static final String SIGN = "sign";

    public AbstractSignatureRouteInterceptor(SignatureOptions options, SecretProvider secretProvider) {
        super(options, secretProvider);
    }

    @Override
    public boolean match(DeployContext<? extends RestlightOptions> ctx, Route route) {

        if (!route.handler().isPresent()) {
            return false;
        }
        Object handler = route.handler().get();
        if (handler instanceof InvocableMethod) {
            InvocableMethod method = (InvocableMethod) handler;
            if (ctx.options()
                    .extOption(SIGN + ".verify-all")
                    .map(Boolean::valueOf)
                    .orElse(Boolean.FALSE)) {
                return !method.hasMethodAnnotation(IgnoreSignValidation.class);
            } else {
                return method.hasMethodAnnotation(SignValidation.class);
            }
        } else {
            return false;
        }

    }
}
