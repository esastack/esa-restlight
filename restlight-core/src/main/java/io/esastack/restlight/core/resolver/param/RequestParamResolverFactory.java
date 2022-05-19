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
package io.esastack.restlight.core.resolver.param;

import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.converter.StringConverterProvider;
import io.esastack.restlight.core.resolver.factory.HandlerResolverFactory;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;

import java.util.List;

/**
 * Binds the {@link HttpRequest} object directly.
 */
public class RequestParamResolverFactory implements ParamResolverFactory {

    @Override
    public ParamResolver<?> createResolver(Param param,
                                           StringConverterProvider converters,
                                           List<? extends HttpRequestSerializer> serializers,
                                           HandlerResolverFactory resolverFactory) {
        return context -> context.requestContext().request();
    }

    @Override
    public boolean supports(Param parameter) {
        return HttpRequest.class.isAssignableFrom(parameter.type());
    }

    /**
     * Default to HIGHEST_PRECEDENCE.
     *
     * @return order
     */
    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
