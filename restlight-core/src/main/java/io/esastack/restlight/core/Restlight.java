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
package io.esastack.restlight.core;

import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.config.RestlightOptionsConfigure;
import io.esastack.restlight.server.bootstrap.RestlightServer;

/**
 * Restlight is a entrance for creating a HTTP server of Restlight. You can use this to build a HTTP server by use the
 * {@link #forServer()} or {@link #forServer(RestlightOptions)} function and get these mechanisms:
 * <ul>
 * <li>A lightweight HTTP server</li>
 * <li>Request-Routing</li>
 * <li>Thread-Scheduling</li>
 * <li>Filter</li>
 * <li>Self-Protection</li>
 * <li>Interceptor</li>
 * <li>ArgumentResolver</li>
 * <li>ReturnValueResolver</li>
 * <li>ControllerAdvice</li>
 * <li>Serialize &amp; Deserialize(Json &amp; Proto-buf)</li>
 * <li>Bean-Validation</li>
 * <li>...</li>
 * </ul>
 * <p>
 * This class allows to set some server-level configurations and the biz-level configurations(in {@link Deployments}) to
 * bootstrap a {@link RestlightServer} which could be {@link #start()} for service.
 */
public class Restlight extends AbstractRestlight {

    Restlight(RestlightOptions options) {
        super(options);
    }

    /**
     * Creates a HTTP server of Restlight by default options.
     *
     * @return Restlight
     */
    public static Restlight forServer() {
        return forServer(RestlightOptionsConfigure.defaultOpts());
    }

    /**
     * Creates a HTTP server of Restlight by given {@link RestlightOptions}.
     *
     * @return Restlight
     */
    public static Restlight forServer(RestlightOptions options) {
        return new Restlight(options);
    }

    @Override
    protected Deployments.Impl createDeployments() {
        return new Deployments.Impl(this, options);
    }
}
