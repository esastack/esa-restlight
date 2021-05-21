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
package esa.restlight.ext.validator.starter.autoconfigurer;

import esa.commons.StringUtils;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.util.Ordered;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static esa.restlight.ext.validator.BeanValidationHandlerAdviceFactory.VALIDATION;

@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@EnableConfigurationProperties({ValidationProperties.class})
public class ValidatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ValidationConfigure validatorConfigure(RestlightOptions options, ValidationProperties properties) {
        final ValidationConfigure configure = new ValidationConfigure();
        configure.config(options, properties);
        return configure;
    }

    private static class ValidationConfigure {

        private void config(RestlightOptions options, ValidationProperties properties) {
            String messageFile = properties.getMessageFile();
            if (StringUtils.isEmpty(messageFile)) {
                messageFile = options.getValidationMessageFile();
            }
            options.extOption(VALIDATION + ".message-file", messageFile);
        }

    }

}

