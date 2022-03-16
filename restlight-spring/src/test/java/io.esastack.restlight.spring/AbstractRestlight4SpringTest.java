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
package io.esastack.restlight.spring;

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.config.RestlightOptionsConfigure;
import io.esastack.restlight.server.bootstrap.RestlightServer;
import io.esastack.restlight.server.handler.Filter;
import io.esastack.restlight.spring.util.RestlightBizExecutorAware;
import io.esastack.restlight.spring.util.RestlightDeployContextAware;
import io.esastack.restlight.spring.util.RestlightIoExecutorAware;
import io.esastack.restlight.spring.util.RestlightServerAware;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertSame;

class AbstractRestlight4SpringTest {

    @Test
    void testAutoAware() {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(AutoConfiguration.class);
        context.refresh();

        final Restlight4Spring restlight = new Restlight4Spring(context, RestlightOptionsConfigure.defaultOpts());
        restlight.enableServerAware(true);
        restlight.enableIoExecutorAware(true);
        restlight.enableBizExecutorAware(true);

        restlight.postStart(restlight);
        DeployContextAwareImpl deployContextAware = context.getBean(DeployContextAwareImpl.class);
        assertSame(restlight.deployments().deployContext(), deployContextAware.ctx);

        ServerAwareImpl serverAware = context.getBean(ServerAwareImpl.class);
        assertSame(restlight, serverAware.server);

        IoExecutorAwareImpl ioExecutorAware = context.getBean(IoExecutorAwareImpl.class);
        assertSame(restlight.ioExecutor(), ioExecutorAware.ioExecutor);

        BizExecutorAware bizExecutorAware = context.getBean(BizExecutorAware.class);
        assertSame(restlight.bizExecutor(), bizExecutorAware.bizExecutor);
    }

    @Configuration
    public static class AutoConfiguration {

        @Bean
        public Filter filter() {
            return (context, chain) -> {
                return chain.doFilter(context);
            };
        }

        @Bean
        public DeployContextAwareImpl deployContextAware() {
            return new DeployContextAwareImpl();
        }

        @Bean
        public ServerAwareImpl serverAware() {
            return new ServerAwareImpl();
        }

        @Bean
        public IoExecutorAwareImpl ioExecutorAware() {
            return new IoExecutorAwareImpl();
        }

        @Bean
        public BizExecutorAware bizExecutorAware() {
            return new BizExecutorAware();
        }

    }

    private static class DeployContextAwareImpl implements RestlightDeployContextAware {

        volatile DeployContext ctx;

        @Override
        public void setDeployContext(DeployContext ctx) {
            this.ctx = ctx;
        }
    }

    private static class ServerAwareImpl implements RestlightServerAware {
        volatile RestlightServer server;

        @Override
        public void setRestlightServer(RestlightServer server) {
            this.server = server;
        }
    }

    private static class IoExecutorAwareImpl implements RestlightIoExecutorAware {

        volatile Executor ioExecutor;

        @Override
        public void setRestlightIoExecutor(Executor ioExecutor) {
            this.ioExecutor = ioExecutor;
        }
    }

    private static class BizExecutorAware implements RestlightBizExecutorAware {

        volatile Executor bizExecutor;

        @Override
        public void setRestlightBizExecutor(Executor bizExecutor) {
            this.bizExecutor = bizExecutor;
        }
    }

    private static class Restlight4Spring extends AbstractRestlight4Spring {

        private Restlight4Spring(ApplicationContext context, RestlightOptions options) {
            super(context, options);
        }

        @Override
        protected DeploymentsImpl createDeployments() {
            return new DeploymentsImpl(this, context, options);
        }

        @Override
        public Executor ioExecutor() {
            return GlobalEventExecutor.INSTANCE;
        }

        @Override
        public Executor bizExecutor() {
            return GlobalEventExecutor.INSTANCE;
        }
    }

    private static class DeploymentsImpl extends Deployments4Spring {

        private DeploymentsImpl(Restlight4Spring restlight,
                                ApplicationContext context,
                                RestlightOptions options) {
            super(restlight, context, options);
        }

    }
}

