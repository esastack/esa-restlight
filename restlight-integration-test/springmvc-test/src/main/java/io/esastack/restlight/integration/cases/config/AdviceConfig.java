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

package io.esastack.restlight.integration.cases.config;

import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.ParamResolverAdvice;
import io.esastack.restlight.core.resolver.ParamResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.ParamResolverAdviceFactory;
import io.esastack.restlight.core.resolver.ParamResolverContext;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdvice;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.RequestEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.RequestEntityResolverContext;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdvice;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.ResponseEntityResolverContext;
import io.esastack.restlight.integration.entity.UserData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author chenglu
 */
@Configuration
public class AdviceConfig {

    @Bean
    public ParamResolverAdviceFactory paramResolverAdviceFactory() {
        return new ParamResolverAdviceFactory() {

            @Override
            public ParamResolverAdvice createResolverAdvice(Param param, ParamResolver resolver) {
                return context -> context.proceed() + "-advice-factory";
            }

            @Override
            public boolean supports(Param param) {
                return param.methodParam().method().getName().equals("customParamAdviceByFactory");
            }
        };
    }

    @Bean
    public ParamResolverAdviceAdapter paramResolverAdviceAdapter() {
        return new ParamResolverAdviceAdapter() {
            @Override
            public boolean supports(Param param) {
                return param.methodParam().method().getName().equals("customParamAdviceByAdaptor");
            }

            @Override
            public Object aroundResolve(ParamResolverContext context) throws Exception {
                return context.proceed() + "-advice-adaptor";
            }
        };
    }

    @Bean
    public RequestEntityResolverAdviceFactory requestEntityResolverAdviceFactory() {
        return new RequestEntityResolverAdviceFactory() {
            @Override
            public RequestEntityResolverAdvice createResolverAdvice(Param param) {
                return context -> {
                    UserData user = (UserData) context.proceed();
                    user.setName(user.getName() + "-advice-factory");
                    return user;
                };
            }

            @Override
            public boolean supports(Param param) {
                return param.methodParam().method().getName().equals("customEntityAdviceByFactory");
            }
        };
    }

    @Bean
    public RequestEntityResolverAdviceAdapter requestEntityResolverAdviceAdapter() {
        return new RequestEntityResolverAdviceAdapter() {

            @Override
            public boolean supports(Param param) {
                return param.methodParam().method().getName().equals("customEntityAdviceByAdaptor");
            }

            @Override
            public Object aroundRead(RequestEntityResolverContext context) throws Exception {
                UserData user = (UserData) context.proceed();
                user.setName(user.getName() + "-advice-adaptor");
                return user;
            }
        };
    }

    @Bean
    public ResponseEntityResolverAdviceFactory responseEntityResolverAdviceFactory() {
        return new ResponseEntityResolverAdviceFactory() {
            @Override
            public ResponseEntityResolverAdvice createResolverAdvice(HandlerMethod method) {
                return context -> {
                    UserData user = (UserData) context.httpEntity().response().entity();
                    user.setName(user.getName() + "-advice-factory");
                    context.proceed();
                };
            }

            @Override
            public boolean supports(HandlerMethod method) {
                return method.method().getName().equals("customResponseEntityAdviceByFactory");
            }
        };
    }

    @Bean
    public ResponseEntityResolverAdviceAdapter responseEntityResolverAdviceAdapter() {
        return new ResponseEntityResolverAdviceAdapter() {
            @Override
            public void aroundWrite(ResponseEntityResolverContext context) throws Exception {
                UserData user = (UserData) context.httpEntity().response().entity();
                user.setName(user.getName() + "-advice-adaptor");
                context.proceed();
            }

            @Override
            public boolean supports(HandlerMethod method) {
                return method.method().getName().equals("customResponseEntityAdviceByAdaptor");
            }
        };
    }
}
