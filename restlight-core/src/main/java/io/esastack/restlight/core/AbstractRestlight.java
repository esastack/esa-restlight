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

import esa.commons.StringUtils;
import esa.commons.spi.SpiLoader;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.configure.RestlightServerEventListener;
import io.esastack.restlight.core.context.FilterContext;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.server.BaseRestlightServer;
import io.esastack.restlight.server.bootstrap.RestlightServer;
import io.esastack.restlight.server.util.LoggerUtils;

import java.util.Collections;
import java.util.List;

/**
 * Abstract implementation for a Restlight server bootstrap. This class allows to set some server-level configurations
 * and the biz-level configurations(in {@link Deployments}) to bootstrap a {@link RestlightServer} which could
 * be {@link #start()} for service.
 * <p>
 *
 * @param <R> type of Restlight
 * @param <D> type of Deployments
 */
public abstract class AbstractRestlight<R extends AbstractRestlight<R, D, O>,
        D extends Deployments<R, D, O>, O extends RestlightOptions> extends BaseRestlightServer<R, D, O,
        RequestContext, FilterContext> implements RestlightServer {

    private List<RestlightServerEventListener> listeners;

    protected AbstractRestlight(O options) {
        super(options);
    }

    @Override
    protected void preStart() {
        super.preStart();
        this.listeners = SpiLoader.cached(RestlightServerEventListener.class)
                .getByFeature(name(),
                        true,
                        Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                        false);
        if (!this.listeners.isEmpty()) {
            for (RestlightServerEventListener listener : listeners) {
                try {
                    listener.preStart(name());
                } catch (Throwable th) {
                    LoggerUtils.logger().error("Error occurred when executing RestlightServer#preStart()");
                }
            }
        }
    }

    @Override
    protected void postStart(RestlightServer server) {
        super.postStart(server);
        if (listeners != null && !listeners.isEmpty()) {
            RestlightOptions options = deployments().server().options;
            for (RestlightServerEventListener listener : listeners) {
                try {
                    listener.postStart(options, server);
                } catch (Throwable th) {
                    LoggerUtils.logger().error("Error occurred when executing RestlightServer#preStart()");
                }
            }
        }
    }

}
