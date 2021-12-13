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
package io.esastack.restlight.jaxrs.spi;

import esa.commons.annotation.Internal;
import esa.commons.spi.Feature;
import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.handler.locate.AbstractRouteMethodLocator;
import io.esastack.restlight.core.handler.locate.RouteMethodLocator;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.spi.RouteMethodLocatorFactory;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.jaxrs.util.JaxrsMappingUtils;

@Internal
@Feature(tags = Constants.INTERNAL)
public class JaxrsRouteMethodLocatorFactory implements RouteMethodLocatorFactory {

    @Override
    public RouteMethodLocator locator(DeployContext<? extends RestlightOptions> ctx) {
        return new HandlerLocator(ctx.options().getScheduling().getDefaultScheduler());
    }

    static class HandlerLocator extends AbstractRouteMethodLocator {

        HandlerLocator(String globalScheduling) {
            super(globalScheduling);
        }

        @Override
        protected HttpStatus getCustomResponse(HandlerMethod handlerMethod) {
            return null;
        }

        @Override
        protected boolean isLocator(HandlerMethod handlerMethod) {
            return JaxrsMappingUtils.getMethod(handlerMethod.method()) == null;
        }
    }
}
