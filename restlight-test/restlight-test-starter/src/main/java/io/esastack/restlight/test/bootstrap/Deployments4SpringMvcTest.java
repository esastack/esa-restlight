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
package io.esastack.restlight.test.bootstrap;

import io.esastack.restlight.core.Deployments;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.server.handler.RestlightHandler;

class Deployments4SpringMvcTest extends Deployments<Restlight4SpringMvcTest,
        Deployments4SpringMvcTest, RestlightOptions> {

    Deployments4SpringMvcTest(Restlight4SpringMvcTest restlight, RestlightOptions options) {
        super(restlight, options);
    }

    RestlightHandler<RequestContext> handler() {
        return getRestlightHandler();
    }
}
