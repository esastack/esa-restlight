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

import esa.commons.reflect.AnnotationUtils;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.annotation.ValidGroup;
import esa.restlight.core.handler.HandlerAdvice;
import esa.restlight.core.handler.HandlerInvoker;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.metadata.MethodDescriptor;
import java.lang.reflect.Method;
import java.util.Set;

public class BeanValidationHandlerAdvice implements HandlerAdvice {

    private final Validator validator;
    private final Object object;
    private final Method method;
    private final boolean validateParams;
    private final boolean validateReturnValue;
    private final Class<?>[] groups;

    public BeanValidationHandlerAdvice(Validator validator, Object object, Method method) {
        this.validator = validator;
        this.method = method;
        this.object = object;
        MethodDescriptor methodDescriptor =
                validator.getConstraintsForClass(object.getClass()).getConstraintsForMethod(method.getName(),
                        method.getParameterTypes());
        this.validateParams = methodDescriptor != null && methodDescriptor.hasConstrainedParameters();
        this.validateReturnValue = methodDescriptor != null && methodDescriptor.hasConstrainedReturnValue();
        this.groups = getValidGroup(method);
    }

    protected Class<?>[] getValidGroup(Method method) {
        esa.restlight.ext.validator.core.ValidGroup group =
                AnnotationUtils.findAnnotation(method, esa.restlight.ext.validator.core.ValidGroup.class);
        if (group != null) {
            return group.value();
        }

        ValidGroup group0 = AnnotationUtils.findAnnotation(method, ValidGroup.class);
        return group0 != null ? group0.value() : new Class<?>[0];
    }

    @Override
    public Object invoke(AsyncRequest request,
                         AsyncResponse response,
                         Object[] args,
                         HandlerInvoker invoker) throws Throwable {
        if (validateParams) {
            Set<ConstraintViolation<Object>> constraintViolations =
                    validator.forExecutables().validateParameters(object,
                            method, args, groups);
            if (constraintViolations != null && !constraintViolations.isEmpty()) {
                throw new ConstraintViolationException("Failed to validate parameters of method '" + this.method + "'",
                        constraintViolations);
            }
        }

        Object result = invoker.invoke(request, response, args);

        if (validateReturnValue) {
            Set<ConstraintViolation<Object>> constraintViolations1 =
                    validator.forExecutables().validateReturnValue(object, method, result, groups);
            if (constraintViolations1 != null && !constraintViolations1.isEmpty()) {
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
