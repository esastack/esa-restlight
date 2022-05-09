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

import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.param.ParamResolver;
import io.esastack.restlight.core.resolver.param.ParamResolverAdvice;
import io.esastack.restlight.core.resolver.param.ParamResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.param.ParamResolverAdviceFactory;
import io.esastack.restlight.core.resolver.param.ParamResolverContext;
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolverAdvice;
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolverContext;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverAdvice;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverAdviceFactory;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverContext;
import io.esastack.restlight.integration.springmvc.entity.UserData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
