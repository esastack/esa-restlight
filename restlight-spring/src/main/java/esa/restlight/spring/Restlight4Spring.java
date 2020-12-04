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
package esa.restlight.spring;

import esa.commons.Checks;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.config.RestlightOptionsConfigure;
import org.springframework.context.ApplicationContext;

import static esa.restlight.spring.util.SpringContextUtils.getBean;

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
 * Deployments4Spring}) to bootstrap a {@link esa.restlight.server.bootstrap.RestlightServer} which could be {@link
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
    protected Deployments4Spring.Impl createDeployments() {
        return new Deployments4Spring.Impl(this, context, options);
    }
}
