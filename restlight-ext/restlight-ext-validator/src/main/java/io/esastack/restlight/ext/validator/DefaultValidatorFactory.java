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
package io.esastack.restlight.ext.validator;

import esa.commons.StringUtils;
import esa.commons.collection.AttributeKey;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.ext.validator.core.ValidationOptions;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Optional;

public class DefaultValidatorFactory implements ValidatorFactory {

    public static final AttributeKey<ValidationOptions> VALIDATION_OPTIONS = AttributeKey
            .valueOf("$bean-validation-options");

    @Override
    public Optional<Validator> validator(DeployContext ctx) {
        final ValidationOptions options = ctx.attrs().attr(VALIDATION_OPTIONS).getAndRemove();
        if (options == null || StringUtils.isEmpty(options.getMessageFile())) {
            return Optional.of(Validation.buildDefaultValidatorFactory().getValidator());
        } else {
            return Optional.of(Validation.byDefaultProvider().configure()
                    .messageInterpolator(new ResourceBundleMessageInterpolator(
                            new PlatformResourceBundleLocator(options.getMessageFile())))
                    .buildValidatorFactory().getValidator());
        }
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}

