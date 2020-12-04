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
package esa.restlight.ext.interceptor.starter.spi;

import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.interceptor.Interceptor;
import esa.restlight.core.interceptor.InterceptorFactory;
import esa.restlight.ext.interceptor.config.SignatureOptions;
import esa.restlight.ext.interceptor.config.SignatureOptionsConfigure;
import esa.restlight.ext.interceptor.signature.AbstractSignatureRouteInterceptor;
import esa.restlight.ext.interceptor.signature.HmacSha1SignatureRouteInterceptor;
import esa.restlight.ext.interceptor.signature.SecretProvider;
import esa.restlight.server.route.Route;

import java.util.Optional;

import static esa.restlight.ext.interceptor.signature.HmacSha1SignatureRouteInterceptor.SIGN;

public class SignValidationInterceptorFactory implements InterceptorFactory {

    private final SecretProvider distributor;
    private AbstractSignatureRouteInterceptor instance;

    public SignValidationInterceptorFactory(SecretProvider distributor) {
        this.distributor = distributor;
    }

    @Override
    public Optional<Interceptor> create(DeployContext<? extends RestlightOptions> ctx, Route route) {
        return InterceptorFactory
                .of(instance == null ? instance = doCreate(buildSignOptions(ctx.options()), distributor) : instance)
                .create(ctx, route);
    }

    protected AbstractSignatureRouteInterceptor doCreate(SignatureOptions options, SecretProvider distributor) {
        return new HmacSha1SignatureRouteInterceptor(options, distributor);
    }

    private SignatureOptions buildSignOptions(RestlightOptions options) {
        SignatureOptionsConfigure configure = SignatureOptionsConfigure.newOpts();
        options.extOption(SIGN + ".app-id-name").ifPresent(configure::appId);
        options.extOption(SIGN + ".secret-version-name").ifPresent(configure::secretVersion);
        options.extOption(SIGN + ".timestamp-name").ifPresent(configure::timestamp);
        options.extOption(SIGN + ".signature-name").ifPresent(configure::signature);
        options.extOption(SIGN + ".expire-seconds").ifPresent(
                (s) -> configure.expireSeconds(Integer.valueOf(s)));
        return configure.configured();
    }
}
