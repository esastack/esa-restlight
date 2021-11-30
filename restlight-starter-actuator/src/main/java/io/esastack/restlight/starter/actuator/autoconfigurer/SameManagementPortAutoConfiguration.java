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
import io.esastack.restlight.starter.actuator.adapt.BaseHandlerMappingProvider;
import io.esastack.restlight.starter.actuator.adapt.JaxrsHandlerMappingProvider;
import io.esastack.restlight.starter.actuator.adapt.SpringMvcHandlerMappingProvider;
import io.esastack.restlight.starter.actuator.condition.ConditionalOnManagementPort;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({RestlightWebEndpointAutoConfiguration.class,
        RestlightOnlyEndpointAutoConfiguration.class})
@ConditionalOnManagementPort(ManagementPortType.SAME)
@ConditionalOnBean({ServerStarter.class})
public class SameManagementPortAutoConfiguration {

    static final String SPRING_MVC_MARK =
            "esa.restlight.springmvc.spi.SpringMvcRouteHandlerLocatorFactory";
    static final String JAX_RS_MARK =
            "esa.restlight.jaxrs.spi.JaxrsRouteHandlerLocatorFactory";

    @Bean
    @ConditionalOnMissingBean(BaseHandlerMappingProvider.class)
    @ConditionalOnClass(name = SPRING_MVC_MARK)
    public BaseHandlerMappingProvider springMvcHandlerMappingProvider(WebEndpointsSupplier webEndpointsSupplier,
                                                                      WebEndpointProperties webEndpointProperties) {
        return new SpringMvcHandlerMappingProvider(webEndpointsSupplier, webEndpointProperties);
    }

    @Bean
    @ConditionalOnMissingBean(BaseHandlerMappingProvider.class)
    @ConditionalOnClass(name = JAX_RS_MARK)
    @ConditionalOnMissingClass(SPRING_MVC_MARK)
    public BaseHandlerMappingProvider jaxrsHandlerMappingProvider(WebEndpointsSupplier webEndpointsSupplier,
                                                                  WebEndpointProperties webEndpointProperties) {
        return new JaxrsHandlerMappingProvider(webEndpointsSupplier, webEndpointProperties);
    }
}
