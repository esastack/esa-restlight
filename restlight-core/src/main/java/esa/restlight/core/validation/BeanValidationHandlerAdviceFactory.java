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
package esa.restlight.core.validation;

import esa.commons.annotation.Internal;
import esa.commons.spi.Feature;
import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.handler.Handler;
import esa.restlight.core.handler.HandlerAdvice;
import esa.restlight.core.spi.HandlerAdviceFactory;
import esa.restlight.core.util.Constants;

import javax.validation.Validator;
import javax.validation.metadata.MethodDescriptor;
import java.lang.reflect.Method;
import java.util.Optional;

@Feature(tags = Constants.INTERNAL)
@Internal
public class BeanValidationHandlerAdviceFactory implements HandlerAdviceFactory {
    @Override
    public Optional<HandlerAdvice> handlerAdvice(DeployContext<? extends RestlightOptions> ctx, Handler handler) {
        if (ctx.validator().isPresent()
                && isValidated(ctx.validator().get(), handler.handler().beanType(), handler.handler().method())) {
            return Optional.of(new BeanValidationHandlerAdvice(ctx.validator().get(),
                    handler.handler().object(),
                    handler.handler().method()));
        }
        return Optional.empty();
    }

    private boolean isValidated(Validator validator, Class<?> clazz, Method method) {
        MethodDescriptor methodDescriptor =
                validator.getConstraintsForClass(clazz).getConstraintsForMethod(method.getName(),
                        method.getParameterTypes());
        return methodDescriptor != null && (methodDescriptor.hasConstrainedReturnValue()
                || methodDescriptor.hasConstrainedParameters());
    }
}
