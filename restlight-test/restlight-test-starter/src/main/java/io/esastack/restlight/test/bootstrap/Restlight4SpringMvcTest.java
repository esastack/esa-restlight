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

import io.esastack.restlight.core.AbstractRestlight;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.server.bootstrap.RestlightServer;
import io.esastack.restlight.server.schedule.HandleableRestlightHandler;

class Restlight4SpringMvcTest extends AbstractRestlight<Restlight4SpringMvcTest,
        Deployments4SpringMvcTest, RestlightOptions> {

    private Restlight4SpringMvcTest(RestlightOptions options) {
        super(options);
    }

    static Restlight4SpringMvcTest forServer(RestlightOptions options) {
        return new Restlight4SpringMvcTest(options);
    }

    @Override
    protected Deployments4SpringMvcTest createDeployments() {
        return new Deployments4SpringMvcTest(this, options);
    }

    @Override
    protected final RestlightServer doBuildServer(HandleableRestlightHandler handler) {
        return new FakeServer(handler);
    }
}
