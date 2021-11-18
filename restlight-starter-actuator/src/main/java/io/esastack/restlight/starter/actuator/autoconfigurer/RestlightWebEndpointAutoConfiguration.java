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
package io.esastack.restlight.starter.actuator.autoconfigurer;

import io.esastack.restlight.starter.ServerStarter;
import io.esastack.restlight.starter.actuator.adapt.RestlightMappingDescriptionProvider;
import io.esastack.restlight.starter.actuator.endpoint.ForceFullGcEndpoint;
import io.esastack.restlight.starter.actuator.endpoint.RestlightBizThreadPoolEndpoint;
import io.esastack.restlight.starter.actuator.endpoint.RestlightConfigEndpoint;
import io.esastack.restlight.starter.actuator.endpoint.RestlightIoExecutorEndpoint;
import io.esastack.restlight.starter.actuator.endpoint.TerminationEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.web.mappings.MappingDescriptionProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RestlightEndpointAutoConfiguration
 * <p>
 * Effective only when current application is a web application.
 */
@Configuration
@ConditionalOnWebApplication
@AutoConfigureAfter(EndpointAutoConfiguration.class)
@EnableConfigurationProperties(WebEndpointProperties.class)
@ConditionalOnBean({ServerStarter.class})
public class RestlightWebEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnEnabledEndpoint
    public RestlightConfigEndpoint restlightConfigEndpoint() {
        return new RestlightConfigEndpoint();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnEnabledEndpoint
    public RestlightBizThreadPoolEndpoint restlightBussinesThreadPoolEndpoint() {
        return new RestlightBizThreadPoolEndpoint();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnEnabledEndpoint
    public TerminationEndpoint terminationEndpoint() {
        return new TerminationEndpoint();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnEnabledEndpoint
    public RestlightIoExecutorEndpoint restlightIoExecutorEndpoint() {
        return new RestlightIoExecutorEndpoint();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnEnabledEndpoint
    public ForceFullGcEndpoint forceFullGcEndpoint() {
        return new ForceFullGcEndpoint();
    }

    @Bean
    public MappingDescriptionProvider restlightMappingDescriptionProvider() {
        return new RestlightMappingDescriptionProvider();
    }
}
