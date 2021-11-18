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
package io.esastack.restlight.spring;

import esa.commons.Checks;
import esa.commons.concurrent.DirectExecutor;
import io.esastack.restlight.core.AbstractRestlight;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.spi.Filter;
import io.esastack.restlight.server.bootstrap.RestlightServer;
import io.esastack.restlight.spring.util.RestlightBizExecutorAware;
import io.esastack.restlight.spring.util.RestlightDeployContextAware;
import io.esastack.restlight.spring.util.RestlightIoExecutorAware;
import io.esastack.restlight.spring.util.RestlightServerAware;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.Executor;

public abstract class AbstractRestlight4Spring<R extends AbstractRestlight4Spring<R, D, O>,
        D extends Deployments4Spring<R, D, O>, O extends RestlightOptions>
        extends AbstractRestlight<R, D, O> {

    protected final ApplicationContext context;
    private boolean enableServerAware = true;
    private boolean enableIoExecutorAware = true;
    private boolean enableBizExecutorAware = true;

    protected AbstractRestlight4Spring(ApplicationContext context, O options) {
        super(options);
        Checks.checkNotNull(context, "Application ctx must not be null");
        this.context = context;
        autoConfigureFromSpringContext(context);
    }

    private void autoConfigureFromSpringContext(ApplicationContext context) {
        // auto inject filter
        context.getBeansOfType(Filter.class).values().forEach(this::addFilter);
    }

    public R enableServerAware(boolean enable) {
        checkImmutable();
        this.enableServerAware = enable;
        return self();
    }

    public R enableIoExecutorAware(boolean enable) {
        checkImmutable();
        this.enableIoExecutorAware = enable;
        return self();
    }

    public R enableBizExecutorAware(boolean enable) {
        checkImmutable();
        this.enableBizExecutorAware = enable;
        return self();
    }

    @Override
    protected void postStart(RestlightServer server) {

        super.postStart(server);

        fireDeployContextAware();

        if (enableServerAware) {
            fireServerAware(server);
        }

        if (enableIoExecutorAware) {
            fireIoExecutorAware(server);
        }

        if (enableBizExecutorAware) {
            fireBizExecutorAware(server);
        }
    }

    private void fireDeployContextAware() {
        context.getBeansOfType(RestlightDeployContextAware.class)
                .values()
                .forEach(aware -> aware.setDeployContext(deployments().deployContext()));
    }

    private void fireServerAware(RestlightServer server) {
        context.getBeansOfType(RestlightServerAware.class)
                .values()
                .forEach(aware -> aware.setRestlightServer(server));
    }

    private void fireIoExecutorAware(RestlightServer server) {
        final Executor executor = nonNull(server.ioExecutor());
        context.getBeansOfType(RestlightIoExecutorAware.class)
                .values()
                .forEach(aware -> aware.setRestlightIoExecutor(executor));
    }

    private void fireBizExecutorAware(RestlightServer server) {
        final Executor executor = nonNull(server.bizExecutor());
        context.getBeansOfType(RestlightBizExecutorAware.class)
                .values()
                .forEach(aware -> aware.setRestlightBizExecutor(executor));
    }

    private static Executor nonNull(Executor executor) {
        return executor == null ? DirectExecutor.INSTANCE : executor;
    }
}
