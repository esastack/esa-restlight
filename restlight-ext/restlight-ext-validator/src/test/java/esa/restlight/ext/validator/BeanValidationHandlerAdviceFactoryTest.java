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

import esa.restlight.core.DeployContextImpl;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.config.RestlightOptionsConfigure;
import esa.restlight.core.handler.Handler;
import esa.restlight.core.handler.impl.HandlerImpl;
import esa.restlight.core.method.HandlerMethod;
import org.junit.jupiter.api.Test;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeanValidationHandlerAdviceFactoryTest {

    @Test
    void testHandlerAdvice() throws Throwable {
        final BeanValidationHandlerAdviceFactory factory = new BeanValidationHandlerAdviceFactory();

        final Handler handler1 = new HandlerImpl(HandlerMethod.of(
                BeanValidationHandlerAdviceFactoryTest.class.getDeclaredMethod("m1"), this));
        assertFalse(factory.handlerAdvice(new Context("",
                RestlightOptionsConfigure.defaultOpts()), handler1).isPresent());

        final Handler handler2 = new HandlerImpl(HandlerMethod.of(
                BeanValidationHandlerAdviceFactoryTest.class.getDeclaredMethod("m2"), this));
        assertTrue(factory.handlerAdvice(new Context("",
                RestlightOptionsConfigure.defaultOpts()), handler2).isPresent());

        final Handler handler3 = new HandlerImpl(HandlerMethod.of(
                BeanValidationHandlerAdviceFactoryTest.class.getDeclaredMethod("m3", String.class),
                this));
        assertTrue(factory.handlerAdvice(new Context("",
                RestlightOptionsConfigure.defaultOpts()), handler3).isPresent());
    }

    private Object m1() {
        return null;
    }

    @Valid
    private Object m2() {
        return null;
    }

    private void m3(@NotEmpty String name) {

    }

    private static class Context extends DeployContextImpl<RestlightOptions> {
        private Context(String name, RestlightOptions options) {
            super(name, options);
        }
    }
}

