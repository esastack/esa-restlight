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

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.interceptor.RouteInterceptor;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.util.RouteUtils;
import io.esastack.restlight.ext.interceptor.annotation.IgnoreSignValidation;
import io.esastack.restlight.ext.interceptor.annotation.SignValidation;
import io.esastack.restlight.ext.interceptor.config.SignatureOptions;
import io.esastack.restlight.server.route.Routing;

import java.util.Optional;

public abstract class AbstractSignatureRouteInterceptor extends AbstractSignatureInterceptor
        implements RouteInterceptor {

    public static final String SIGN = "sign";

    public AbstractSignatureRouteInterceptor(SignatureOptions options, SecretProvider secretProvider) {
        super(options, secretProvider);
    }

    @Override
    public boolean match(DeployContext ctx, Routing routing) {
        Optional<HandlerMethod> handlerMethod = RouteUtils.extractHandlerMethod(routing);
        if (!handlerMethod.isPresent()) {
            return false;
        }
        if (ctx.options()
                .extOption(SIGN + ".verify-all")
                .map(Boolean::valueOf)
                .orElse(Boolean.FALSE)) {
            return !handlerMethod.get().hasMethodAnnotation(IgnoreSignValidation.class);
        } else {
            return handlerMethod.get().hasMethodAnnotation(SignValidation.class);
        }
    }
}
