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
package io.esastack.restlight.starter.actuator;

import esa.commons.NetworkUtils;
import io.esastack.restlight.starter.actuator.autoconfigurer.ManagementOptions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import java.net.InetAddress;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManagementServerStarterTest {

    @Test
    void testAfterSingletonsInstantiated() throws Throwable {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.refresh();

        final ManagementServerProperties managementProps = new ManagementServerProperties();
        managementProps.setAddress(InetAddress.getByName("127.0.0.1"));
        managementProps.setPort(NetworkUtils.selectRandomPort());

        ManagementServerStarter starter = new ManagementServerStarter(new ManagementOptions(),
                ctx1 -> Collections.emptyList(),
                managementProps);
        starter.setApplicationContext(ctx);
        starter.afterSingletonsInstantiated();
        assertEquals("Restlight(Actuator)", starter.serverName());

        assertTrue(NetworkUtils.checkPortStatus(managementProps.getAddress().getHostAddress(),
                managementProps.getPort()));
        ctx.close();
        starter.onApplicationEvent(new ContextClosedEvent(ctx));
    }

}

