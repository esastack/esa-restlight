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
package esa.restlight.core.resolver;

import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.method.Param;
import esa.restlight.core.serialize.HttpRequestSerializer;
import esa.restlight.core.serialize.HttpResponseSerializer;

import java.util.List;

public interface HandlerResolverFactory {

    /**
     * Get the ArgumentResolver for given parameter
     *
     * @param param parameter
     *
     * @return resolver
     */
    ArgumentResolver getArgumentResolver(Param param);

    /**
     * Get default return value resolver, must not be null.
     *
     * @param handleMethod handlerMethod
     *
     * @return resolver
     */
    ReturnValueResolver getReturnValueResolver(InvocableMethod handleMethod);

    /**
     * Returns all the {@link ArgumentResolverFactory}s in this factory.
     *
     * @return resolvers
     */
    List<ArgumentResolverFactory> argumentResolvers();

    /**
     * Returns all the {@link ReturnValueResolverFactory}s in this factory.
     *
     * @return resolvers
     */
    List<ReturnValueResolverFactory> returnValueResolvers();

    /**
     * Returns all the {@link HttpRequestSerializer}s in this factory.
     *
     * @return serializers
     */
    List<HttpRequestSerializer> rxSerializers();

    /**
     * Returns all the {@link HttpResponseSerializer}s in this factory.
     *
     * @return serializers
     */
    List<HttpResponseSerializer> txSerializers();


}
