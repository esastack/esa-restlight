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
package esa.restlight.starter.autoconfigure;

import esa.commons.NetworkUtils;
import esa.restlight.server.bootstrap.RestlightServer;
import esa.restlight.server.util.LoggerUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.Collections;
import java.util.List;

/**
 * The class is designed to guarantee the {@link RestlightServer#shutdown()} happens
 * before spring beans are destroyed. When restlight-server-shutdown-hook registered at
 * {@link RestlightServer#start()} executes after the spring-context-shutdown-hook registered by
 * {@link AbstractApplicationContext#registerShutdownHook()}, the {@link #onApplicationEvent(ContextClosedEvent)}
 * will make sure {@link RestlightServer#shutdown()} execute before destroying spring beans
 * during {@link AbstractApplicationContext#close()}. And as the the {@link RestlightServer#shutdown()} operation
 * is idempotent, there will be no side effects even if the order is reversed.
 * You can get more information from https://github.com/esastack/esa-restlight/issues/38.
 */
class RestlightGracefulShutdown implements ApplicationListener<ContextClosedEvent> {

    private final List<RestlightServer> servers;

    RestlightGracefulShutdown(List<RestlightServer> servers) {
        this.servers = servers == null ? Collections.emptyList() : Collections.unmodifiableList(servers);
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        servers.forEach(server -> {
            try {
                server.shutdown();
            } catch (Throwable ex) {
                LoggerUtils.logger().error("Failed to shutdown Restlight server binding on: {}",
                        NetworkUtils.parseAddress(server.address()), ex);
            }
        });
    }
}
