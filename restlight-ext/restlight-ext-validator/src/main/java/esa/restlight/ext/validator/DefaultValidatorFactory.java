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
package esa.restlight.ext.validator;

import esa.commons.StringUtils;
import esa.commons.annotation.Internal;
import esa.commons.spi.Feature;
import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.util.Constants;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Optional;

import static esa.restlight.ext.validator.BeanValidationHandlerAdviceFactory.VALIDATION;

@Feature(tags = Constants.INTERNAL)
@Internal
public class DefaultValidatorFactory implements ValidatorFactory {

    private Validator instance;

    @Override
    public Optional<Validator> validator(DeployContext<? extends RestlightOptions> ctx) {
        if (instance != null) {
            return Optional.of(instance);
        } else {
            instance = doCreate(ctx);
            return Optional.ofNullable(instance);
        }
    }

    protected Validator doCreate(DeployContext<? extends RestlightOptions> ctx) {
        final String messageFile = ctx.options()
                .extOption(VALIDATION + ".message-file").orElse(null);
        if (StringUtils.isEmpty(messageFile)) {
            return Validation.buildDefaultValidatorFactory().getValidator();
        } else {
            return Validation.byDefaultProvider().configure()
                    .messageInterpolator(new ResourceBundleMessageInterpolator(
                            new PlatformResourceBundleLocator(messageFile)))
                    .buildValidatorFactory().getValidator();
        }
    }

}

