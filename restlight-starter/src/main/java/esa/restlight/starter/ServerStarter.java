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
package esa.restlight.starter;

import esa.commons.NetworkUtils;
import esa.commons.StringUtils;
import esa.commons.TimeCounter;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import esa.restlight.core.util.OrderedComparator;
import esa.restlight.server.bootstrap.AbstractDelegatedRestlightServer;
import esa.restlight.server.bootstrap.RestlightServer;
import esa.restlight.server.bootstrap.RestlightThread;
import esa.restlight.spring.Restlight4Spring;
import esa.restlight.starter.autoconfigure.AutoRestlightServerOptions;
import esa.restlight.starter.autoconfigure.RestlightConfigure;
import esa.restlight.starter.autoconfigure.WarmUpOptions;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ServerStarter extends AbstractDelegatedRestlightServer
        implements SmartInitializingSingleton, RestlightServer, ApplicationContextAware {

    private static final Logger logger =
            LoggerFactory.getLogger(ServerStarter.class);

    private final AutoRestlightServerOptions options;
    private volatile ApplicationContext context;

    public ServerStarter(AutoRestlightServerOptions options) {
        this.options = options;
    }

    @Override
    public void afterSingletonsInstantiated() {
        final CompletableFuture<Void> startFuture = new CompletableFuture<>();
        final long wamUp;
        WarmUpOptions warmUpOptions = options.getWarmUp();
        if (warmUpOptions != null && warmUpOptions.isEnable()) {
            wamUp = warmUpOptions.getDelay();
        } else {
            wamUp = 0L;
        }
        TimeCounter.start();
        new RestlightThread(() -> {
            try {
                // initialize server from options and ctx
                initializeServer();
                if (options.isPrintBanner()) {
                    printBanner(version());
                }
                logger.info("Starting Restlight server...\n{}", options.toString());

                if (wamUp > 0) {
                    logger.info("Warm-up delay for {} mills.", warmUpOptions.getDelay());
                    try {
                        Thread.sleep(warmUpOptions.getDelay());
                    } catch (InterruptedException e) {
                        logger.warn("Unexpected interruption. Stopping warm-up...", e);
                    }
                }
                this.start();
                startFuture.complete(null);
                this.await();
            } catch (Throwable t) {
                startFuture.completeExceptionally(t);
            }
        }, "restlight-server").start();

        try {
            startFuture.get(wamUp + 30L * 1000L, TimeUnit.MILLISECONDS);
            logger.info("Started Restlight server in {} millis on {}", TimeCounter.countMillis(),
                    NetworkUtils.parseAddress(getServer().address()));
        } catch (Exception e) {
            throw new BeanCreationException("Failed to start Restlight server.", e);
        }
    }

    private void initializeServer() {
        // create a server
        final Restlight4Spring server = Restlight4Spring
                .forServer(context, options);

        final ConfigurableRestlight configured = new ConfigurableRestlight(server, options);
        // configure by RestlightConfigure
        context.getBeansOfType(RestlightConfigure.class)
                .values()
                .stream()
                .sorted(OrderedComparator.INSTANCE)
                .forEach(configure -> configure.accept(configured));

        if (configured.address == null) {
            if (StringUtils.isNotEmpty(options.getUnixDomainSocketFile())) {
                server.domainSocketAddress(options.getUnixDomainSocketFile());
            } else if (StringUtils.isNotEmpty(options.getHost())) {
                int port = options.getPort() > 0 ? options.getPort() : 8080;
                server.address(options.getHost(), port);
            } else if (options.getPort() > 0) {
                server.address(options.getPort());
            }
        } else {
            server.address(configured.address);
        }
        setServer(server);
    }

    private void printBanner(String version) {
        logger.info("\n" +
                "______             _    _  _         _      _   \n" +
                "| ___ \\           | |  | |(_)       | |    | |  \n" +
                "| |_/ /  ___  ___ | |_ | | _   __ _ | |__  | |_ \n" +
                "|    /  / _ \\/ __|| __|| || | / _` || '_ \\ | __|\n" +
                "| |\\ \\ |  __/\\__ \\| |_ | || || (_| || | | || |_ \n" +
                "\\_| \\_| \\___||___/ \\__||_||_| \\__, ||_| |_| \\__|\n" +
                "                               __/ |            \n" +
                "                              |___/             \n" +
                " :: ESA Restlight ::        (v" + version + ")\n");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
