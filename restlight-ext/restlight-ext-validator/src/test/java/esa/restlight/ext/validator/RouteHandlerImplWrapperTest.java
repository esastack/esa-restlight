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

import esa.commons.ClassUtils;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.handler.HandlerInvoker;
import esa.restlight.core.handler.LinkedHandlerInvoker;
import esa.restlight.core.handler.impl.HandlerInvokerImpl;
import esa.restlight.core.handler.impl.RouteHandlerImpl;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.metadata.MethodDescriptor;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteHandlerImplWrapperTest {

    private static final BeanSubject subject = new BeanSubject();

    private static final Map<String, RouteHandlerImpl> handlerMethodAdapters = new LinkedHashMap<>(16);

    private RouteHandlerImpl handlerMethodInvokerAdapter;

    private static final Object[] simpleArg = {"Hello"};
    private static final String MESSAGE_INTERPOLATOR_FILE = "validation-message";

    private final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
    private final AsyncResponse response = MockAsyncResponse.aMockResponse().build();

    private static final Validator validator =
            Validation.byDefaultProvider().configure().messageInterpolator(new ResourceBundleMessageInterpolator(
                    new PlatformResourceBundleLocator(MESSAGE_INTERPOLATOR_FILE)))
                    .buildValidatorFactory()
                    .getValidator();

    @BeforeAll
    static void setUp() {
        Locale.setDefault(Locale.CHINA);
        ClassUtils.userDeclaredMethods(BeanSubject.class)
                .forEach((method -> {
                    final InvocableMethod handlerMethod = HandlerMethod.of(BeanSubject.class, method,
                            new BeanSubject());
                    HandlerInvoker invoker = new HandlerInvokerImpl(handlerMethod);

                    final MethodDescriptor descriptor =
                            validator.getConstraintsForClass(handlerMethod.beanType())
                                    .getConstraintsForMethod(method.getName(), method.getParameterTypes());

                    invoker = LinkedHandlerInvoker.immutable(new BeanValidationHandlerAdvice[]{
                            new BeanValidationHandlerAdvice(validator, subject, method,
                                    descriptor != null && descriptor.hasConstrainedParameters(),
                                    descriptor != null && descriptor.hasConstrainedReturnValue())},
                            invoker);
                    handlerMethodAdapters.put(method.getName(),
                            new RouteHandlerImpl(HandlerMethod.of(method, subject),
                                    null, invoker, true, null));
                }));
    }

    @Test
    void testNormalInvoke() throws Throwable {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testSimpleValidation");
        final Object returnValue = handlerMethodInvokerAdapter.invoke(request, response, simpleArg);
        assertEquals(simpleArg[0], returnValue);
    }

    @Test
    void testNormalWrapper() throws Throwable {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testSimpleValidation");
        try {
            handlerMethodInvokerAdapter.invoke(request, response, simpleArg);
        } catch (ConstraintViolationException e) {
            assertEquals(1, e.getConstraintViolations().size());
        }
    }

    @Test
    void testMessageWrapper() {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testSimpleValidation");
        try {
            handlerMethodInvokerAdapter.invoke(request, response, simpleArg);
        } catch (ConstraintViolationException e) {
            assertEquals(1, e.getConstraintViolations().size());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Test
    void testParametersValidation() throws Throwable {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testParametersValidation");

        try {
            Object[] args = {new BeanSubject.SimpleBean("abc", 34,
                    new BeanSubject.SimpleBean.InnerBean("a", 0)), ""};

            handlerMethodInvokerAdapter.invoke(request, response, args);
        } catch (ConstraintViolationException e) {
            assertEquals(3L, e.getConstraintViolations().size());
        }
    }

    @Test
    void testParametersValidationWithGroup() throws Throwable {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testParametersValidationWithGroup");

        try {
            Object[] args = {new BeanSubject.SimpleBean2("abc", 34,
                    new BeanSubject.SimpleBean2.InnerBean("a", 0)), ""};
            handlerMethodInvokerAdapter.invoke(request, response, args);
        } catch (ConstraintViolationException e) {
            assertEquals(2L, e.getConstraintViolations().size());
        }
    }

    @Test
    void testReturnValueValidation() throws Throwable {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testReturnValueValidation");

        try {
            Object[] args = {"", -1};
            handlerMethodInvokerAdapter.invoke(request, response, args);
        } catch (ConstraintViolationException e) {
            assertEquals(4L, e.getConstraintViolations().size());
        }
    }

    @Test
    void testReturnValueValidationWithGroup() throws Throwable {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testReturnValueValidationWithGroup");

        try {
            Object[] args = {"", -1};
            handlerMethodInvokerAdapter.invoke(request, response, args);
        } catch (ConstraintViolationException e) {
            assertEquals(1L, e.getConstraintViolations().size());
        }
    }

    @Test
    void testParametersAndReturnValueValidation() throws Throwable {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testParametersAndReturnValueValidation");

        try {
            Object[] args = {"abc", -1, 100L};
            handlerMethodInvokerAdapter.invoke(request, response, args);
        } catch (ConstraintViolationException e) {
            assertEquals(1L, e.getConstraintViolations().size());
        }
    }

    @Test
    void testParametersAndReturnValueValidationWitGroup() throws Throwable {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testParametersAndReturnValueValidationWithGroup");

        try {
            Object[] args = {"", -1, 100L};
            handlerMethodInvokerAdapter.invoke(request, response, args);
        } catch (ConstraintViolationException e) {
            assertEquals(1L, e.getConstraintViolations().size());
        }
    }

    @Test
    void testInternationalCnSupport() throws Throwable {
        handlerMethodInvokerAdapter = handlerMethodAdapters.get("testSimpleValidation");

        // Test zh_CN
        try {
            handlerMethodInvokerAdapter.invoke(request, response, simpleArg);
        } catch (ConstraintViolationException e) {
            assertTrue(e.getConstraintViolations()
                    .toArray(new ConstraintViolation[]{})[0].getMessage().contains("CN"));
        }
    }

}
