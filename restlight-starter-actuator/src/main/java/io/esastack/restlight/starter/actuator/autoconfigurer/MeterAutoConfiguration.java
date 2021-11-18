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
import io.esastack.restlight.starter.actuator.meter.RestlightBizThreadPoolBinder;
import io.esastack.restlight.starter.actuator.meter.RestlightIoExecutorBinder;
import io.esastack.restlight.starter.autoconfigure.AutoRestlightServerOptions;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({ServerStarter.class, MeterRegistry.class})
@AutoConfigureAfter(name = "org.springframework.boot.actuate.autoconfigure.metrics" +
        ".export.prometheus.PrometheusMetricsExportAutoConfiguration")
public class MeterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(MeterRegistry.class)
    public RestlightBizThreadPoolBinder restlightBizThreadPoolMeter(MeterRegistry registry,
                                                                    AutoRestlightServerOptions config) {
        return new RestlightBizThreadPoolBinder(registry, config);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(MeterRegistry.class)
    public RestlightIoExecutorBinder restlightIoExecutorMeter(MeterRegistry registry) {
        return new RestlightIoExecutorBinder(registry);
    }
}
