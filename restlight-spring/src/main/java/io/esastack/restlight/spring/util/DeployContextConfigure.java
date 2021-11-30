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
package io.esastack.restlight.spring.util;

import io.esastack.restlight.core.Restlight;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.server.ServerDeployContext;

/**
 * DeployContextConfigure allows user to modify the {@link ServerDeployContext} or change the options in {@link
 * RestlightOptions} before {@link Restlight#start()}ing.
 */
@FunctionalInterface
public interface DeployContextConfigure extends Ordered {

    /**
     * accept {@link ServerDeployContext} and do some modification before starting server.
     *
     * @param context   context
     */
    void accept(ServerDeployContext<? extends RestlightOptions> context);
        
}

