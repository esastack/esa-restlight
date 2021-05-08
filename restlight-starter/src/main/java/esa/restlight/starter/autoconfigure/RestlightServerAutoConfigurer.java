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
package esa.restlight.starter.autoconfigure;

import esa.restlight.core.util.Constants;
import esa.restlight.server.bootstrap.RestlightServer;
import esa.restlight.starter.ServerStarter;
import esa.restlight.starter.condition.ConditionalOnEnableServer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
@ConditionalOnClass({RestlightServer.class})
@EnableConfigurationProperties(AutoRestlightServerOptions.class)
@ConditionalOnEnableServer
public class RestlightServerAutoConfigurer {

    @Bean(destroyMethod = "")
    @Primary
    @Qualifier(Constants.SERVER)
    public ServerStarter defaultServerStarter(AutoRestlightServerOptions options) {
        return new ServerStarter(options);
    }

    @Bean
    public RestlightGracefulShutdown restlightGracefulShutdown(List<RestlightServer> servers) {
        return new RestlightGracefulShutdown(servers);
    }
}
