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
package esa.restlight.ext.validator;

import esa.commons.StringUtils;
import esa.commons.annotation.Internal;
import esa.commons.spi.Feature;
import esa.commons.spi.SpiLoader;
import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.handler.Handler;
import esa.restlight.core.handler.HandlerAdvice;
import esa.restlight.core.spi.HandlerAdviceFactory;
import esa.restlight.core.util.Constants;

import javax.validation.Validator;
import javax.validation.metadata.MethodDescriptor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Feature(tags = Constants.INTERNAL)
@Internal
public class BeanValidationHandlerAdviceFactory implements HandlerAdviceFactory {

    private ValidatorFactory instance;

    public static final String VALIDATION = "validation";

    @Override
    public Optional<HandlerAdvice> handlerAdvice(DeployContext<? extends RestlightOptions> ctx, Handler handler) {
        final ValidatorFactory factory = getOrCreate(ctx);
        if (factory == null) {
            return Optional.empty();
        }

        final Validator validator = factory.validator(ctx).orElse(null);
        if (validator != null && isValidated(validator,
                handler.handler().beanType(), handler.handler().method())) {
            return Optional.of(new BeanValidationHandlerAdvice(validator,
                    handler.handler().object(),
                    handler.handler().method()));
        }

        return Optional.empty();
    }

    protected boolean isValidated(Validator validator, Class<?> clazz, Method method) {
        final MethodDescriptor descriptor =
                validator.getConstraintsForClass(clazz).getConstraintsForMethod(method.getName(),
                        method.getParameterTypes());
        return descriptor != null && (descriptor.hasConstrainedReturnValue()
                || descriptor.hasConstrainedParameters());
    }

    protected ValidatorFactory getOrCreate(DeployContext<? extends RestlightOptions> ctx) {
        if (instance == null) {
            // load ValidatorFactory by spi
            List<ValidatorFactory> validatorFactories =
                    SpiLoader.cached(ValidatorFactory.class)
                            .getByFeature(ctx.name(),
                                    true,
                                    Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()),
                                    false);
            instance = validatorFactories.iterator().next();
        }
        return instance;
    }
}
