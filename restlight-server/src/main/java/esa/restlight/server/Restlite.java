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
package esa.restlight.server;

import esa.restlight.server.config.ServerOptions;
import esa.restlight.server.config.ServerOptionsConfigure;

/**
 * Restlite is a entrance for creating a HTTP server of Restlight. You can use this to build a HTTP server by use the
 * {@link #forServer()} or {@link #forServer(ServerOptions)} function and get these mechanisms:
 * <ul>
 * <li>A lightweight HTTP server</li>
 * <li>Request-Routing</li>
 * <li>Thread-Scheduling</li>
 * <li>Filter</li>
 * </ul>
 * <p>
 * This class allows to set some server-level configurations and the biz-level configurations(in {@link
 * BaseDeployments}) to bootstrap a {@link esa.restlight.server.bootstrap.RestlightServer} which could be {@link
 * #start()} for service.
 */
public class Restlite extends BaseRestlightServer<Restlite, BaseDeployments.Impl, ServerOptions> {

    Restlite(ServerOptions options) {
        super(options);
    }

    /**
     * Creates a HTTP server by default options.
     *
     * @return Restlite
     */
    public static Restlite forServer() {
        return forServer(ServerOptionsConfigure.defaultOpts());
    }

    /**
     * Creates a HTTP server by given options.
     *
     * @return Restlite
     */
    public static Restlite forServer(ServerOptions options) {
        return new Restlite(options);
    }

    @Override
    protected BaseDeployments.Impl createDeployments() {
        return new BaseDeployments.Impl(this, options);
    }
}
