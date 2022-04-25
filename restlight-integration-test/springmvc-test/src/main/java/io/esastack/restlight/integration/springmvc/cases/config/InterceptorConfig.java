/*
 * Copyright 2022 OPPO ESA Stack Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.esastack.restlight.integration.springmvc.cases.config;

import esa.commons.collection.AttributeKey;
import io.esastack.restlight.core.interceptor.InterceptorFactory;
import io.esastack.restlight.core.interceptor.InternalInterceptor;
import io.esastack.restlight.server.context.RequestContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletionStage;

@Configuration
public class InterceptorConfig {

    @Bean
    public InterceptorFactory initAttributeInterceptor() {
        return InterceptorFactory.of(new InternalInterceptor() {
            @Override
            public CompletionStage<Boolean> preHandle(RequestContext context, Object handler) {
                context.attrs().attr(AttributeKey.stringKey("name")).set("test");
                return InternalInterceptor.super.preHandle(context, handler);
            }
        });
    }
}
