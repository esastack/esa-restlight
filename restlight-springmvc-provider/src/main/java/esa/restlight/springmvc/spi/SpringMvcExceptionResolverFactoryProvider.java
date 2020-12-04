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
package esa.restlight.springmvc.spi;

import esa.commons.spi.Feature;
import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.resolver.exception.ExceptionResolverFactory;
import esa.restlight.core.spi.ExceptionResolverFactoryProvider;
import esa.restlight.core.util.Constants;
import esa.restlight.springmvc.resolver.exception.SpringMvcExceptionResolverFactory;

@Feature(tags = Constants.INTERNAL)
public class SpringMvcExceptionResolverFactoryProvider implements ExceptionResolverFactoryProvider {

    @Override
    public ExceptionResolverFactory factory(DeployContext<? extends RestlightOptions> ctx) {
        return new SpringMvcExceptionResolverFactory(
                ctx.exceptionMappers().orElse(null),
                ctx.controllers().orElse(null),
                ctx.advices().orElse(null),
                ctx.routeHandlerLocator().orElse(null),
                ctx.resolverFactory().orElse(null));
    }
}
