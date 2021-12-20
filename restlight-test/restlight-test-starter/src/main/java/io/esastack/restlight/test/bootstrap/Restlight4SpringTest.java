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
package io.esastack.restlight.test.bootstrap;

import esa.commons.Checks;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.server.bootstrap.RestlightServer;
import io.esastack.restlight.server.handler.RestlightHandler;
import io.esastack.restlight.server.handler.Filter;
import io.esastack.restlight.spring.AbstractRestlight4Spring;
import io.esastack.restlight.test.autoconfig.AutoMockMvcOptions;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static io.esastack.restlight.spring.util.SpringContextUtils.getBean;

class Restlight4SpringTest extends AbstractRestlight4Spring<Restlight4SpringTest, Deployments4SpringTest,
        RestlightOptions> {

    private Restlight4SpringTest(ApplicationContext context, RestlightOptions options) {
        super(context, options);
    }

    static Restlight4SpringTest forServer(ApplicationContext context) {
        final RestlightOptions options =
                getBean(Checks.checkNotNull(context, "context"),
                        AutoMockMvcOptions.class)
                        .orElseThrow(() -> new NullPointerException("options"));
        return new Restlight4SpringTest(context, options);
    }

    @Override
    protected Deployments4SpringTest createDeployments() {
        return new Deployments4SpringTest(this, context, options);
    }

    @Override
    protected final RestlightServer doBuildServer(RestlightHandler handler,
                                                  List<Filter> fs) {
        return new FakeServer(handler, fs, deployments().exceptionHandler());
    }
}
