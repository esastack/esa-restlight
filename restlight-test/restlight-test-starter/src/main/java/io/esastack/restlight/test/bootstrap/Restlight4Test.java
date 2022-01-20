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
import io.esastack.restlight.server.schedule.AbstractRestlightHandler;

class Restlight4Test extends AbstractRestlight {

    private Restlight4Test(RestlightOptions options) {
        super(options);
    }

    static Restlight4Test forServer(RestlightOptions options) {
        return new Restlight4Test(options);
    }

    @Override
    protected Deployments4Test createDeployments() {
        return new Deployments4Test(this, options);
    }

    @Override
    public Deployments4Test deployments() {
        return (Deployments4Test) super.deployments();
    }

    @Override
    protected final RestlightServer doBuildServer(AbstractRestlightHandler handler) {
        return new FakeServer(handler);
    }
}
