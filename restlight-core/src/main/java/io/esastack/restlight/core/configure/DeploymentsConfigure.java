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
package io.esastack.restlight.core.configure;

import esa.commons.annotation.Internal;
import esa.commons.spi.SPI;
import io.esastack.restlight.core.Deployments;

/**
 * DeploymentsConfigure allows user to add the custom components to {@link Deployments} before applying the
 * {@link Deployments}. You can see more information from {@link Deployments} {@code beforeApplyDeployments}.
 */
@SPI
@Internal
@FunctionalInterface
public interface DeploymentsConfigure {

    /**
     * Performs this operation on the given {@link Deployments} and {@link Deployments} which will be used
     * to configure current deployments.
     *
     * @param deployments current instance of deployments
     */
    void accept(ConfigurableDeployments deployments);

}

