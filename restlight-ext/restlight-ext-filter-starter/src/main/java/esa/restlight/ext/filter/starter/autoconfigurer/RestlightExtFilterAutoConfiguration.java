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
package esa.restlight.ext.filter.starter.autoconfigurer;

import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.util.Ordered;
import esa.restlight.ext.filter.accesslog.AccessLogFilter;
import esa.restlight.ext.filter.connectionlimit.ConnectionLimitFilter;
import esa.restlight.ext.filter.cors.CorsFilter;
import esa.restlight.ext.filter.cpuload.CpuLoadProtectionFilter;
import esa.restlight.ext.filter.ipwhitelist.IpWhiteListFilter;
import esa.restlight.ext.filter.xss.XssFilter;
import esa.restlight.starter.ServerStarter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static esa.restlight.starter.autoconfigure.AutoRestlightServerOptions.PREFIX;

@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnClass({ServerStarter.class})
@EnableConfigurationProperties({CorsProperties.class,
        AccessLogProperties.class,
        CpuLoadProtectionProperties.class,
        ConnectionLimitProperties.class,
        XssProperties.class,
        IpWhiteListProperties.class})
public class RestlightExtFilterAutoConfiguration {

    static final String EXT = PREFIX + ".ext.";

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = IpWhiteListProperties.PREFIX, name = "enable", havingValue = "true")
    public IpWhiteListFilter ipWhiteListFilter(IpWhiteListProperties options) {
        return new IpWhiteListFilter(options);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = ConnectionLimitProperties.PREFIX, name = "enable", havingValue = "true")
    public ConnectionLimitFilter connectionLimitFilter(ConnectionLimitProperties options) {
        return new ConnectionLimitFilter(options);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = CpuLoadProtectionProperties.PREFIX, name = "enable", havingValue = "true")
    public CpuLoadProtectionFilter cpuLoadProtectionFilter(CpuLoadProtectionProperties options) {
        return CpuLoadProtectionFilter.newFilter(options.getThreshold(), options.getInitialDiscardRate(),
                options.getMaxDiscardRate());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = EXT + "access-log", name = "enable", havingValue = "true")
    public esa.restlight.ext.filter.AccessLogFilter oldAccessLogFilter(RestlightOptions options) {
        // check again
        if (options.extOption("access-log.enable")
                .map(Boolean::parseBoolean).orElse(Boolean.FALSE)) {
            final esa.restlight.ext.filter.config.AccessLogOptionsConfigure opts =
                    esa.restlight.ext.filter.config.AccessLogOptionsConfigure.newOpts();
            options.extOption("access-log.full-uri").ifPresent(s ->
                    opts.fullUri(Boolean.parseBoolean(s)));
            return new esa.restlight.ext.filter.AccessLogFilter(opts.configured());
        }
        return null;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AccessLogProperties.PREFIX, name = "enable", havingValue = "true")
    public AccessLogFilter accessLogFilter(RestlightOptions options, AccessLogProperties props) {
        // check again
        if (options.extOption("accesslog.enable")
                .map(Boolean::parseBoolean).orElse(Boolean.FALSE)) {
            options.extOption("accesslog.full-uri").ifPresent(s -> props.setFullUri(Boolean.parseBoolean(s)));
            return new AccessLogFilter(props);
        }
        return null;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = XssProperties.PREFIX, name = "enable", havingValue = "true")
    public XssFilter xssFilter(XssProperties options) {
        return new XssFilter(options);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = CorsProperties.PREFIX, name = "enable", havingValue = "true")
    public CorsFilter corsFilter(CorsProperties options) {
        return new CorsFilter(options.getRules());
    }
}
