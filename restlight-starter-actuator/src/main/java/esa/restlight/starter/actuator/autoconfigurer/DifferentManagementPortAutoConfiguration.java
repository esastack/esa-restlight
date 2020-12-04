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
package esa.restlight.starter.actuator.autoconfigurer;

import esa.restlight.core.handler.HandlerMappingProvider;
import esa.restlight.core.util.Constants;
import esa.restlight.starter.ServerStarter;
import esa.restlight.starter.actuator.ManagementServerStarter;
import esa.restlight.starter.actuator.adapt.JaxrsHandlerMappingProvider;
import esa.restlight.starter.actuator.adapt.SpringMvcHandlerMappingProvider;
import esa.restlight.starter.actuator.condition.ConditionalOnManagementPort;
import esa.restlight.starter.condition.ConditionalOnEnableServer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static esa.restlight.starter.actuator.autoconfigurer.SameManagementPortAutoConfiguration.JAX_RS_MARK;
import static esa.restlight.starter.actuator.autoconfigurer.SameManagementPortAutoConfiguration.SPRING_MVC_MARK;

/**
 * ManagementDifferentPortAutoConfiguration
 * <p>
 * Effective only when current application is not a web application.
 */
@Configuration
@ConditionalOnNotWebApplication
@AutoConfigureAfter(name = "esa.restlight.starter.autoconfigure.RestlightServerAutoConfigurer",
        value = {RestlightWebEndpointAutoConfiguration.class,
                RestlightOnlyEndpointAutoConfiguration.class})
@ConditionalOnManagementPort(ManagementPortType.DIFFERENT)
@ConditionalOnBean({ServerStarter.class})
@EnableConfigurationProperties(ManagementOptions.class)
public class DifferentManagementPortAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Mark.class)
    @ConditionalOnClass(name = SPRING_MVC_MARK)
    public Mark springMvcEnv() {
        return Mark.SPRING_MVC;
    }

    @Bean
    @ConditionalOnMissingBean(Mark.class)
    @ConditionalOnClass(name = JAX_RS_MARK)
    @ConditionalOnMissingClass(SPRING_MVC_MARK)
    public Mark jaxrsEnv() {
        return Mark.JAX_RS;
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    @ConditionalOnEnableServer
    @ConditionalOnBean(Mark.class)
    @Qualifier(Constants.MANAGEMENT)
    public ManagementServerStarter managementServerStarter(ManagementServerProperties managementProps,
                                                           WebEndpointsSupplier endpointsSupplier,
                                                           WebEndpointProperties endpointProperties,
                                                           ManagementOptions options,
                                                           Mark mark) {

        return new ManagementServerStarter(options, mark.provider(endpointsSupplier, endpointProperties),
                managementProps);
    }


    /**
     * A marker interface of {@link HandlerMappingProvider} to isolate Management Restlight Server and Restlight Server,
     * because we could not inject the {@link HandlerMappingProvider} into spring context directly which would make
     * Restlight Server and Management Restlight Server to share the {@link HandlerMappingProvider} instance we
     * injected.
     */
    public interface Mark {
        Mark SPRING_MVC = SpringMvcHandlerMappingProvider::new;
        Mark JAX_RS = JaxrsHandlerMappingProvider::new;

        /**
         * Provides a instance of {@link HandlerMappingProvider} by given arguments.
         */
        HandlerMappingProvider provider(WebEndpointsSupplier endpointsSupplier,
                                        WebEndpointProperties endpointProperties);
    }

}
