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
package esa.restlight.springmvc;

import esa.restlight.core.DeployContextImpl;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.config.RestlightOptionsConfigure;
import esa.restlight.core.handler.locate.RouteHandlerLocator;
import esa.restlight.core.resolver.ArgumentResolverAdapter;
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.resolver.HandlerResolverFactory;
import esa.restlight.core.resolver.HandlerResolverFactoryImpl;
import esa.restlight.core.resolver.ReturnValueResolverFactory;
import esa.restlight.core.resolver.arg.AsyncRequestArgumentResolverFactory;
import esa.restlight.core.resolver.arg.AsyncResponseArgumentResolverFactory;
import esa.restlight.core.resolver.arg.QueryBeanArgumentResolver;
import esa.restlight.core.resolver.result.SimpleReturnValueResolver;
import esa.restlight.core.serialize.FastJsonHttpBodySerializer;
import esa.restlight.core.serialize.HttpBodySerializer;
import esa.restlight.core.util.Constants;
import esa.restlight.springmvc.resolver.arg.CookieValueArgumentResolver;
import esa.restlight.springmvc.resolver.arg.MatrixVariableArgumentResolver;
import esa.restlight.springmvc.resolver.arg.PathVariableArgumentResolver;
import esa.restlight.springmvc.resolver.arg.RequestAttributeArgumentResolver;
import esa.restlight.springmvc.resolver.arg.RequestBodyArgumentResolver;
import esa.restlight.springmvc.resolver.arg.RequestHeaderArgumentResolver;
import esa.restlight.springmvc.resolver.arg.RequestParamArgumentResolver;
import esa.restlight.springmvc.resolver.arg.SpecifiedFixedRequestBodyArgumentResolver;
import esa.restlight.springmvc.resolver.result.ResponseBodyReturnValueResolver;
import esa.restlight.springmvc.resolver.result.ResponseStatusReturnValueResolver;
import esa.restlight.springmvc.resolver.result.SpecifiedFixedResponseBodyReturnValueResolver;
import esa.restlight.springmvc.spi.SpringMvcRouteHandlerLocatorFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MockUtils {

    public static List<ArgumentResolverFactory> mockArgumentResolverFactoryBeans() {
        return Arrays.asList(new RequestParamArgumentResolver(),
                new RequestHeaderArgumentResolver(),
                new PathVariableArgumentResolver(),
                new CookieValueArgumentResolver(),
                new MatrixVariableArgumentResolver(),
                new RequestAttributeArgumentResolver(),
                new QueryBeanArgumentResolver(new Context(RestlightOptionsConfigure.defaultOpts())),
                new RequestBodyArgumentResolver(),
                new SpecifiedFixedRequestBodyArgumentResolver(),
                new AsyncRequestArgumentResolverFactory(),
                new AsyncResponseArgumentResolverFactory());
    }

    public static List<ReturnValueResolverFactory> mockReturnValueResolversFactoryBeans() {
        return Arrays.asList(new SimpleReturnValueResolver(),
                new ResponseStatusReturnValueResolver(),
                new SpecifiedFixedResponseBodyReturnValueResolver(),
                new ResponseBodyReturnValueResolver(true, null));
    }

    public static HandlerResolverFactory mockResolverFactory() {
        Collection<? extends HttpBodySerializer> serializers =
                Collections.singletonList(new FastJsonHttpBodySerializer());
        return new HandlerResolverFactoryImpl(
                serializers, serializers,
                null,
                mockArgumentResolverFactoryBeans(),
                null,
                null,
                null,
                mockReturnValueResolversFactoryBeans(),
                null,
                null);
    }

    public static HandlerResolverFactory mockResolverFactoryWithArgumentResolvers(Collection<?
            extends ArgumentResolverAdapter> argumentResolvers) {
        Collection<? extends HttpBodySerializer> serializers =
                Collections.singletonList(new FastJsonHttpBodySerializer());
        return new HandlerResolverFactoryImpl(serializers, serializers,
                argumentResolvers,
                mockArgumentResolverFactoryBeans(),
                null,
                null,
                null,
                mockReturnValueResolversFactoryBeans(),
                null,
                null);
    }

    public static RouteHandlerLocator mockRouteHandlerLocator() {
        return new SpringMvcRouteHandlerLocatorFactory()
                .locator(new Context(RestlightOptionsConfigure.defaultOpts()));
    }

    private static class Context extends DeployContextImpl<RestlightOptions> {

        Context(RestlightOptions options) {
            super(Constants.SERVER, options);
        }
    }


}
