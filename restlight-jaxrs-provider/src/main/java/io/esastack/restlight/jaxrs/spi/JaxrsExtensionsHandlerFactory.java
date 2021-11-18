/*
 * Copyright 2021 OPPO ESA Stack Project
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
import io.esastack.restlight.core.configure.ExtensionsHandler;
import io.esastack.restlight.core.configure.MiniConfigurableDeployments;
import io.esastack.restlight.core.spi.ExtensionsHandlerFactory;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.jaxrs.configure.JaxrsExtensionsHandler;

import java.util.Optional;

@Feature(tags = Constants.INTERNAL)
@Internal
public class JaxrsExtensionsHandlerFactory implements ExtensionsHandlerFactory {

    @Override
    public Optional<ExtensionsHandler> handler(MiniConfigurableDeployments deployments) {
        return Optional.of(new JaxrsExtensionsHandler(deployments));
    }
}

