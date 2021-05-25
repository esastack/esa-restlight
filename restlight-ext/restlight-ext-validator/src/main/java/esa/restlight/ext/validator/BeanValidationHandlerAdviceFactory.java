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

import esa.commons.annotation.Internal;
import esa.commons.spi.Feature;
import esa.commons.spi.SpiLoader;
import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.handler.Handler;
import esa.restlight.core.handler.HandlerAdvice;
import esa.restlight.core.spi.HandlerAdviceFactory;
import esa.restlight.core.util.Constants;
import esa.restlight.core.util.OrderedComparator;

import javax.validation.Validator;
import javax.validation.metadata.MethodDescriptor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

@Feature(tags = Constants.INTERNAL)
@Internal
public class BeanValidationHandlerAdviceFactory implements HandlerAdviceFactory {

    private static final String VALIDATION_VALIDATOR = "$bean-validation-validator";

    @Override
    public Optional<HandlerAdvice> handlerAdvice(DeployContext<? extends RestlightOptions> ctx, Handler handler) {
        Optional<Validator> validator = ctx.uncheckedAttribute(VALIDATION_VALIDATOR);
        if (validator == null) {
            validator = doCreate(ctx);
            ctx.attribute(VALIDATION_VALIDATOR, validator);
        }

        if (!validator.isPresent()) {
            return Optional.empty();
        }

        final Method method = handler.handler().method();
        final MethodDescriptor descriptor =
                validator.get().getConstraintsForClass(handler.handler().beanType())
                        .getConstraintsForMethod(method.getName(), method.getParameterTypes());

        if (descriptor == null) {
            return Optional.empty();
        }

        final boolean validateParams = descriptor.hasConstrainedParameters();
        final boolean validateReturnValue = descriptor.hasConstrainedReturnValue();
        if (validateParams || validateReturnValue) {
            return Optional.of(new BeanValidationHandlerAdvice(validator.get(),
                    handler.handler().object(),
                    handler.handler().method(),
                    validateParams,
                    validateReturnValue));
        }
        return Optional.empty();
    }

    private Optional<Validator> doCreate(DeployContext<? extends RestlightOptions> ctx) {
        final List<ValidatorFactory> factories =
                SpiLoader.cached(ValidatorFactory.class)
                        .getByGroup(ctx.name(), true);
        if (factories.isEmpty()) {
            return Optional.empty();
        } else {
            OrderedComparator.sort(factories);

            Optional<Validator> validator;
            for (ValidatorFactory factory : factories) {
                validator = factory.validator(ctx);
                if (validator.isPresent()) {
                    return validator;
                }
            }
            return Optional.empty();
        }
    }
}
