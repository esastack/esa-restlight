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
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.config.RestlightOptionsConfigure;
import io.esastack.restlight.server.bootstrap.RestlightServer;
import io.esastack.restlight.server.util.LoggerUtils;
import org.springframework.context.ApplicationContext;

import static io.esastack.restlight.spring.util.SpringContextUtils.getBean;

/**
 * Restlight4Spring is a entrance for creating a HTTP server of Restlight from Spring environment. You can use this to
 * build a HTTP server by use the {@link #forServer(ApplicationContext)} or {@link #forServer(ApplicationContext,
 * RestlightOptions)} function and get these mechanisms:
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
 * <li>Auto-configuration from Spring ApplicationContext</li>
 * <li>...</li>
 * </ul>
 * <p>
 * This class allows to set some server-level configurations and the biz-level configurations(in {@link
 * Deployments4Spring}) to bootstrap a {@link RestlightServer} which could be {@link
 * #start()} for service.
 */
public class Restlight4Spring extends AbstractRestlight4Spring<Restlight4Spring, Deployments4Spring.Impl,
        RestlightOptions> {

    private Restlight4Spring(ApplicationContext context, RestlightOptions options) {
        super(context, options);
    }

    /**
     * Creates a HTTP server of Restlight and auto-configure by given {@link ApplicationContext}.
     *
     * @param context ctx of spring
     *
     * @return Restlight4Spring
     */
    public static Restlight4Spring forServer(ApplicationContext context) {
        final RestlightOptions options =
                getBean(Checks.checkNotNull(context, "Application ctx must not be null"),
                        RestlightOptions.class)
                        .orElse(RestlightOptionsConfigure.defaultOpts());
        return forServer(context, options);
    }

    /**
     * Creates a HTTP server of Restlight and auto-configure by given {@link ApplicationContext} and {@link
     * RestlightOptions}.
     *
     * @param context ctx of spring
     *
     * @return Restlight4Spring
     */
    public static Restlight4Spring forServer(ApplicationContext context, RestlightOptions options) {
        return new Restlight4Spring(context, options);
    }

    @Override
    protected void preStart() {
        DeployContext<RestlightOptions> context = deployments().deployContext();
        deployments().contextConfigures.forEach(c -> {
            try {
                c.accept(context);
            } catch (Throwable th) {
                LoggerUtils.logger().error("Failed to configure deploy context", th);
            }
        });
        super.preStart();
    }

    @Override
    protected Deployments4Spring.Impl createDeployments() {
        return new Deployments4Spring.Impl(this, context, options);
    }
}
