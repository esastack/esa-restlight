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
package io.esastack.restlight.test.autoconfig;

import io.esastack.restlight.server.bootstrap.DispatcherHandler;
import io.esastack.restlight.server.bootstrap.RestlightServer;
import io.esastack.restlight.test.bootstrap.MockMvcBuilders;
import io.esastack.restlight.test.context.MockMvc;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Configuration
@ConditionalOnClass({RestlightServer.class, DispatcherHandler.class})
@AutoConfigureOrder(HIGHEST_PRECEDENCE + 100)
public class MockMvcAutoConfiguration {

    @Bean
    public MockMvc mockMvc(ApplicationContext context) {
        return MockMvcBuilders.contextSetup(context);
    }
}
