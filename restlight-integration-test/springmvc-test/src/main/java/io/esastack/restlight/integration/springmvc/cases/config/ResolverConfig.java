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

import esa.commons.Result;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.param.ParamResolver;
import io.esastack.restlight.core.resolver.param.ParamResolverFactory;
import io.esastack.restlight.core.context.RequestEntity;
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolver;
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolverFactory;
import io.esastack.restlight.core.context.ResponseEntity;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolver;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverFactory;
import io.esastack.restlight.core.resolver.converter.StringConverterProvider;
import io.esastack.restlight.core.resolver.entity.response.AbstractResponseEntityResolver;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.integration.springmvc.cases.annotation.CustomFieldParam;
import io.esastack.restlight.integration.springmvc.cases.annotation.CustomRequestBean;
import io.esastack.restlight.integration.springmvc.cases.annotation.CustomRequestBody;
import io.esastack.restlight.integration.springmvc.cases.annotation.CustomResponseBody;
import io.esastack.restlight.integration.springmvc.entity.UserData;
import io.esastack.restlight.core.context.RequestContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class ResolverConfig {

    @Bean
    public ParamResolverFactory paramResolverFactory() {
        return new ParamResolverFactory() {
            @Override
            public ParamResolver createResolver(Param param, StringConverterProvider converters,
                                                List<? extends HttpRequestSerializer> serializers) {
                return context -> UserData.Builder.anUserData()
                        .name(context.request().getParam("name"))
                        .build();
            }

            @Override
            public boolean supports(Param param) {
                return param.hasAnnotation(CustomRequestBean.class);
            }
        };
    }

    @Bean
    public RequestEntityResolverFactory requestEntityResolverFactory() {
        return new RequestEntityResolverFactory() {
            @Override
            public RequestEntityResolver createResolver(Param param, StringConverterProvider converters,
                                                        List<? extends HttpRequestSerializer> serializers) {
                return new Resolver(serializers.get(0));
            }

            @Override
            public int getOrder() {
                return RequestEntityResolverFactory.super.getOrder();
            }

            @Override
            public boolean supports(Param param) {
                return param.hasAnnotation(CustomRequestBody.class);
            }

            class Resolver implements RequestEntityResolver {

                private final HttpRequestSerializer serializer;

                private Resolver(HttpRequestSerializer serializer) {
                    this.serializer = serializer;
                }

                @Override
                public Result<?, Void> readFrom(RequestEntity entity, RequestContext context) throws Exception {
                    Result<?, Void> result = serializer.deserialize(entity);
                    if (result.isOk()) {
                        UserData userData = (UserData) result.get();
                        userData.setName("test");
                    }
                    return result;
                }
            }
        };
    }

    @Bean
    public ResponseEntityResolverFactory responseEntityResolverFactory() {
        return new ResponseEntityResolverFactory() {
            @Override
            public ResponseEntityResolver createResolver(HandlerMethod method,
                                                         List<? extends HttpResponseSerializer> serializers) {
                return new AbstractResponseEntityResolver() {
                    @Override
                    protected byte[] serialize(ResponseEntity entity, List<MediaType> mediaTypes,
                                               RequestContext context) {
                        String name = (String) entity.response().entity();
                        entity.response().headers()
                                .set(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON.value());
                        return ("{\"name\":\"" + name + "\"}").getBytes(StandardCharsets.UTF_8);
                    }
                };
            }

            @Override
            public boolean supports(HandlerMethod method) {
                return method.hasMethodAnnotation(CustomResponseBody.class, true);
            }
        };
    }

    @Bean
    public ParamResolverFactory customParamResolverFactory() {
        return new ParamResolverFactory() {
            @Override
            public ParamResolver createResolver(Param param, StringConverterProvider converters,
                                                List<? extends HttpRequestSerializer> serializers) {
                return context -> context.request().getParam("name");
            }

            @Override
            public boolean supports(Param param) {
                return param.hasAnnotation(CustomFieldParam.class);
            }
        };
    }
}
