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
package io.esastack.restlight.ext.interceptor.starter.autoconfigurer;

import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.ext.interceptor.signature.AbstractSignatureHandlerInterceptor;
import io.esastack.restlight.ext.interceptor.signature.AbstractSignatureRouteInterceptor;
import io.esastack.restlight.ext.interceptor.signature.SecretProvider;
import io.esastack.restlight.ext.interceptor.starter.spi.SignValidationInterceptorFactory;
import io.esastack.restlight.starter.ServerStarter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.esastack.restlight.ext.interceptor.signature.HmacSha1SignatureRouteInterceptor.SIGN;
import static io.esastack.restlight.starter.autoconfigure.AutoRestlightServerOptions.PREFIX;

@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnClass({ServerStarter.class})
public class RestlightExtInterceptorAutoConfiguration {

    private static final String EXT = PREFIX + ".ext.";

    @Bean
    @ConditionalOnProperty(prefix = EXT + SIGN, name = "enable", havingValue = "true")
    @ConditionalOnMissingBean(value = {AbstractSignatureHandlerInterceptor.class,
            AbstractSignatureRouteInterceptor.class,
            SignValidationInterceptorFactory.class})
    public SignValidationInterceptorFactory signatureInterceptor(ObjectProvider<SecretProvider> distributor) {
        return new SignValidationInterceptorFactory(distributor.getIfUnique());
    }
}
