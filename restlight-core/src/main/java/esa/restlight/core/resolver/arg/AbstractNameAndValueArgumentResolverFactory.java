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
package esa.restlight.core.resolver.arg;

import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.method.Param;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.serialize.HttpRequestSerializer;

import java.util.List;

public abstract class AbstractNameAndValueArgumentResolverFactory implements ArgumentResolverFactory {

    @Override
    public ArgumentResolver createResolver(Param param,
                                           List<? extends HttpRequestSerializer> serializers) {
        return new AbstractNameAndValueArgumentResolver(param) {

            @Override
            protected Object resolveName(String name, AsyncRequest request) throws Exception {
                return AbstractNameAndValueArgumentResolverFactory.this.resolveName(name, request);
            }

            @Override
            protected NameAndValue createNameAndValue(Param param) {
                return AbstractNameAndValueArgumentResolverFactory.this.createNameAndValue(param);
            }
        };
    }

    /**
     * Try to resolve the value by the given name from the {@link AsyncRequest}
     *
     * @param name      name
     * @param request   request
     *
     * @return resolved value
     * @throws Exception occurred
     */
    protected abstract Object resolveName(String name, AsyncRequest request) throws Exception;

    /**
     * Create an instance of {@link NameAndValue} for the parameter.
     *
     * @param parameter parameter
     *
     * @return name and value
     */
    protected abstract NameAndValue createNameAndValue(Param parameter);
}
