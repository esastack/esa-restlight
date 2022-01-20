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
package io.esastack.restlight.starter.actuator;

import esa.commons.Checks;
import esa.commons.NetworkUtils;
import esa.commons.StringUtils;
import esa.commons.TimeCounter;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import io.esastack.restlight.core.Restlight;
import io.esastack.restlight.core.handler.HandlerMappingProvider;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.core.util.OrderedComparator;
import io.esastack.restlight.server.bootstrap.AbstractDelegatedRestlightServer;
import io.esastack.restlight.server.bootstrap.RestlightServer;
import io.esastack.restlight.server.util.LoggerUtils;
import io.esastack.restlight.starter.actuator.autoconfigurer.ManagementConfigure;
import io.esastack.restlight.starter.actuator.autoconfigurer.ManagementOptions;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.AbstractApplicationContext;

public class ManagementServerStarter extends AbstractDelegatedRestlightServer
        implements SmartInitializingSingleton, RestlightServer, ApplicationContextAware,
        ApplicationListener<ContextClosedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ManagementServerStarter.class);

    private final ManagementOptions managementOptions;
    private volatile ApplicationContext context;
    private final ManagementServerProperties managementProps;
    private final HandlerMappingProvider mappingProvider;

    public ManagementServerStarter(ManagementOptions managementOptions,
                                   HandlerMappingProvider mappingProvider,
                                   ManagementServerProperties managementProps) {
        Checks.checkNotNull(managementOptions);
        Checks.checkNotNull(managementProps);
        Checks.checkNotNull(mappingProvider);
        this.managementOptions = managementOptions;
        this.managementProps = managementProps;
        this.mappingProvider = mappingProvider;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (context == null) {
            throw new IllegalStateException("Could not find ApplicationContext of Spring.");
        }
        try {
            logger.info("Starting Restlight(Actuator) server...\n" + managementOptions.toString());
            Restlight server = Restlight.forServer(managementOptions)
                    .name(Constants.MANAGEMENT)
                    .deployments()
                    .addHandlerMappingProvider(mappingProvider)
                    .server();

            final ConfigurableManagementRestlight configured = new ConfigurableManagementRestlight(server,
                    managementOptions);
            // configure by RestlightConfigure
            context.getBeansOfType(ManagementConfigure.class)
                    .values()
                    .stream()
                    .sorted(OrderedComparator.INSTANCE)
                    .forEach(configure -> configure.accept(configured));

            if (configured.address == null) {
                if (StringUtils.isNotEmpty(managementOptions.getUnixDomainSocketFile())) {
                    server.domainSocketAddress(managementOptions.getUnixDomainSocketFile());
                } else if (managementProps.getAddress() != null
                        && managementProps.getAddress().getHostAddress() != null) {
                    server.address(managementProps.getAddress().getHostAddress(),
                            managementProps.getPort());
                } else {
                    server.address(managementProps.getPort());
                }
            } else {
                server.address(configured.address);
            }

            setServer(server);
            TimeCounter.start();
            this.start();
            logger.info("Started Restlight(Actuator) server in {} millis on address:{}", TimeCounter.countMillis(),
                    server.address());
        } catch (Throwable t) {
            throw new BeanCreationException("Failed to start Restlight(Actuator) server.", t);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    /**
     * Guarantee the {@link ManagementServerStarter#shutdown()} will be executed before spring beans are
     * destroyed during {@link AbstractApplicationContext#close()}.
     * <p>
     * See https://github.com/esastack/esa-restlight/issues/38.
     *
     * @param event event
     */
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        try {
            this.shutdown();
        } catch (Throwable ex) {
            LoggerUtils.logger().error("Failed to shutdown [{}] server: {}",
                    serverName(), NetworkUtils.parseAddress(address()), ex);
        }
    }

    @Override
    protected String serverName() {
        return "Restlight(Actuator)";
    }
}
