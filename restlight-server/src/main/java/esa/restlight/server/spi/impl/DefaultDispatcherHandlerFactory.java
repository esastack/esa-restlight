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
package esa.restlight.server.spi.impl;

import esa.commons.spi.Feature;
import esa.restlight.core.util.Constants;
import esa.restlight.server.ServerDeployContext;
import esa.restlight.server.bootstrap.DefaultDispatcherHandler;
import esa.restlight.server.bootstrap.DispatcherHandler;
import esa.restlight.server.config.ServerOptions;
import esa.restlight.server.spi.DispatcherHandlerFactory;

import java.util.Collections;

@Feature(tags = Constants.INTERNAL)
public class DefaultDispatcherHandlerFactory implements DispatcherHandlerFactory {
    @Override
    public DispatcherHandler dispatcherHandler(ServerDeployContext<? extends ServerOptions> context) {
        return new DefaultDispatcherHandler(context.routeRegistry().orElse(null),
                context.dispatcherExceptionHandlers().orElse(Collections.emptyList()));
    }
}
