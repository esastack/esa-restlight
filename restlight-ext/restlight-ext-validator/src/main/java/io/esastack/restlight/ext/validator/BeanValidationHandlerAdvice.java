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
package io.esastack.restlight.ext.validator;

import esa.commons.reflect.AnnotationUtils;
import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restlight.core.handler.HandlerAdvice;
import io.esastack.restlight.core.handler.HandlerInvoker;
import io.esastack.restlight.core.context.RequestContext;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.lang.reflect.Method;
import java.util.Set;

class BeanValidationHandlerAdvice implements HandlerAdvice {

    private final Validator validator;
    private final Object object;
    private final Method method;
    private final boolean validateParams;
    private final boolean validateReturnValue;
    private final Class<?>[] groups;

    BeanValidationHandlerAdvice(Validator validator,
                                Object object,
                                Method method,
                                boolean validateParams,
                                boolean validateReturnValue) {
        this.validator = validator;
        this.method = method;
        this.object = object;
        this.validateParams = validateParams;
        this.validateReturnValue = validateReturnValue;
        this.groups = getValidGroup(method);
    }

    private Class<?>[] getValidGroup(Method method) {
        io.esastack.restlight.ext.validator.core.ValidGroup group =
                AnnotationUtils.findAnnotation(method, io.esastack.restlight.ext.validator.core.ValidGroup.class);
        if (group != null) {
            return group.value();
        }
        return new Class[0];
    }

    @Override
    public Object invoke(RequestContext context, Object[] args, HandlerInvoker invoker) throws Throwable {
        if (validateParams) {
            Set<ConstraintViolation<Object>> constraintViolations =
                    validator.forExecutables().validateParameters(object,
                            method, args, groups);
            if (constraintViolations != null && !constraintViolations.isEmpty()) {
                context.response().status(HttpStatus.BAD_REQUEST.code());
                throw new ConstraintViolationException("Failed to validate parameters of method '" + this.method + "'",
                        constraintViolations);
            }
        }

        Object result = invoker.invoke(context, args);

        if (validateReturnValue) {
            Set<ConstraintViolation<Object>> constraintViolations1 =
                    validator.forExecutables().validateReturnValue(object, method, result, groups);
            if (constraintViolations1 != null && !constraintViolations1.isEmpty()) {
                context.response().status(HttpStatus.INTERNAL_SERVER_ERROR.code());
                throw new ConstraintViolationException("Failed to validate method[" + this.method + "]'s return value",
                        constraintViolations1);
            }
        }

        return result;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
