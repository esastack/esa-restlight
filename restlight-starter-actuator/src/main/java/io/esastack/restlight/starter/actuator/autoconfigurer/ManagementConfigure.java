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
package io.esastack.restlight.starter.actuator.autoconfigurer;

import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.spring.Restlight4Spring;
import io.esastack.restlight.starter.actuator.ConfigurableManagementRestlight;

/**
 * RestlightConfigure allows user to modify the {@link RestlightOptions} or change the options in {@link
 * Restlight4Spring} before starting the server.
 */
@FunctionalInterface
public interface ManagementConfigure extends Ordered {

    /**
     * Performs this operation on the given {@link Restlight4Spring} and {@link RestlightOptions} which will be used to
     * start a Restlight server.
     *
     * @param restlight current instance of restlight
     */
    void accept(ConfigurableManagementRestlight restlight);

}
