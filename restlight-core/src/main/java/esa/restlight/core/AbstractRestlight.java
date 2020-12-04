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
package esa.restlight.core;

import esa.restlight.core.config.RestlightOptions;
import esa.restlight.server.BaseRestlightServer;
import esa.restlight.server.bootstrap.RestlightServer;

/**
 * Abstract implementation for a Restlight server bootstrap. This class allows to set some server-level configurations
 * and the biz-level configurations(in {@link Deployments}) to bootstrap a {@link RestlightServer} which could
 * be {@link #start()} for service.
 * <p>
 *
 * @param <R> type of Restlight
 * @param <D> type of Deployments
 */

public abstract class AbstractRestlight<R extends AbstractRestlight<R, D, O>, D extends Deployments<R, D, O>,
        O extends RestlightOptions>
        extends BaseRestlightServer<R, D, O> implements RestlightServer {

    protected AbstractRestlight(O options) {
        super(options);
    }
}
