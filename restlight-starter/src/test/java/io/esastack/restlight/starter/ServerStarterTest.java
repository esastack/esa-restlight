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
package io.esastack.restlight.starter;

import esa.commons.NetworkUtils;
import io.esastack.restlight.starter.autoconfigure.AutoRestlightServerOptions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Configuration
class ServerStarterTest {

    @Test
    void testAfterSingletonsInstantiated() {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.refresh();

        final AutoRestlightServerOptions options = new AutoRestlightServerOptions();
        final int port = NetworkUtils.selectRandomPort();
        options.setPort(port);
        options.setHost("127.0.0.1");
        final ServerStarter starter = new ServerStarter(options);
        starter.setApplicationContext(ctx);
        starter.afterSingletonsInstantiated();

        assertTrue(NetworkUtils.checkPortStatus(options.getHost(), options.getPort()));
        ctx.close();
        starter.onApplicationEvent(new ContextClosedEvent(ctx));
    }

}

