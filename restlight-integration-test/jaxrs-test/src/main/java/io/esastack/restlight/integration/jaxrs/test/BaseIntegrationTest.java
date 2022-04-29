/*
 * Copyright 2022 OPPO ESA Stack Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.esastack.restlight.integration.jaxrs.test;

import esa.commons.NetworkUtils;
import io.esastack.restclient.RestClient;
import io.esastack.restlight.starter.ServerStarter;
import io.esastack.restlight.starter.autoconfigure.AutoRestlightServerOptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public abstract class BaseIntegrationTest {

    public static RestClient restClient;

    public static String domain;

    private static AnnotationConfigApplicationContext ctx;

    @BeforeAll
    public static void setUp() {
        restClient = RestClient.ofDefault();

        ctx =  new AnnotationConfigApplicationContext();
        ctx.scan("io.esastack.restlight.integration.jaxrs.cases");
        ctx.refresh();
        final AutoRestlightServerOptions options = new AutoRestlightServerOptions();

        // set port and host
        final int port = NetworkUtils.selectRandomPort();
        final String host = "127.0.0.1";
        options.setPort(port);
        options.setHost(host);

        // start server
        final ServerStarter starter = new ServerStarter(options);
        starter.setApplicationContext(ctx);
        starter.afterSingletonsInstantiated();
        domain = "http://" + host + ":" + port + "/integration/test";
    }

    @AfterAll
    public static void tearDown() {
        ctx.close();
    }
}
