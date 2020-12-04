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
package esa.restlight.springmvc.resolver.exception;

import esa.restlight.core.handler.impl.HandlerImpl;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.resolver.ExceptionResolver;
import esa.restlight.springmvc.resolver.Pojo;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.Subject;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControllerAdviceExceptionMapperTest {

    private Map<Class<? extends Throwable>, ExceptionResolver<Throwable>> mappings = Collections
            .singletonMap(Exception.class, (request, response, throwable) -> null);

    @Test
    void testWhenIsController() throws NoSuchMethodException {
        ControllerAdviceExceptionMapper mapper = new ControllerAdviceExceptionMapper(mappings, new Object(),
                true, new String[0], new Class<?>[0], Collections.emptyList(), Collections.emptyList());
        assertTrue(mapper.isApplicable(new HandlerImpl(
                HandlerMethod.of(Subject0.class.getDeclaredMethod("method0"), new Subject()))));

        mapper = new ControllerAdviceExceptionMapper(mappings, new Subject(),
                true, new String[0], new Class<?>[0], Collections.emptyList(), Collections.emptyList());
        assertFalse(mapper.isApplicable(new HandlerImpl(
                HandlerMethod.of(Subject0.class.getDeclaredMethod("method0"), new Subject()))));
    }

    @Test
    void testApplicableByPackages() throws NoSuchMethodException {
        ControllerAdviceExceptionMapper mapper = new ControllerAdviceExceptionMapper(mappings, new Object(),
                false, new String[]{this.getClass().getPackage().getName()},
                new Class<?>[0], Collections.emptyList(), Collections.emptyList());

        // matched package's name
        assertTrue(mapper.isApplicable(new HandlerImpl(
                HandlerMethod.of(Subject0.class.getDeclaredMethod("method0"), new Subject0()))));

        // unmatched package's name
        assertFalse(mapper.isApplicable(new HandlerImpl(
                HandlerMethod.of(Pojo.class.getDeclaredMethod("getId"), new Pojo()))));

        // init package names by class
        mapper = new ControllerAdviceExceptionMapper(mappings, new Object(),
                false, new String[]{Object.class.getPackage().getName()},
                new Class<?>[]{Subject.class}, Collections.emptyList(), Collections.emptyList());

        assertTrue(mapper.isApplicable(new HandlerImpl(
                HandlerMethod.of(Subject0.class.getDeclaredMethod("method0"), new Subject()))));

        assertFalse(mapper.isApplicable(new HandlerImpl(
                HandlerMethod.of(Pojo.class.getDeclaredMethod("getId"), new Pojo()))));
    }

    @Test
    void testApplicableByAssignableTypes() throws NoSuchMethodException {
        ControllerAdviceExceptionMapper mapper = new ControllerAdviceExceptionMapper(mappings, new Object(),
                false, new String[]{Object.class.getPackage().getName()},
                new Class<?>[0], Collections.singletonList(Subject0.class), Collections.emptyList());

        assertFalse(mapper.isApplicable(new HandlerImpl(
                HandlerMethod.of(Subject2.class.getDeclaredMethod("method0"), new Subject2()))));
        assertTrue(mapper.isApplicable(new HandlerImpl(
                HandlerMethod.of(Subject1.class.getDeclaredMethod("method0"), new Subject1()))));
    }

    @Test
    void testApplicableByAnnotations() throws NoSuchMethodException {
        ControllerAdviceExceptionMapper mapper = new ControllerAdviceExceptionMapper(mappings, new Object(),
                false, new String[]{Object.class.getPackage().getName()},
                new Class<?>[0], Collections.singletonList(Subject0.class),
                Collections.singletonList(RestController.class));

        assertFalse(mapper.isApplicable(new HandlerImpl(
                HandlerMethod.of(Subject3.class.getDeclaredMethod("method0"), new Subject3()))));

        assertTrue(mapper.isApplicable(new HandlerImpl(
                HandlerMethod.of(Subject2.class.getDeclaredMethod("method0"), new Subject2()))));
    }

    @Test
    void testMatchAll() throws NoSuchMethodException {
        ControllerAdviceExceptionMapper mapper = new ControllerAdviceExceptionMapper(mappings, new Object(),
                false, new String[0], new Class<?>[0], Collections.emptyList(),
                Collections.emptyList());

        assertTrue(mapper.isApplicable(new HandlerImpl(
                HandlerMethod.of(Subject0.class.getDeclaredMethod("method0"), new Subject0()))));

        assertTrue(mapper.isApplicable(new HandlerImpl(
                HandlerMethod.of(Subject1.class.getDeclaredMethod("method0"), new Subject1()))));

        assertTrue(mapper.isApplicable(new HandlerImpl(
                HandlerMethod.of(Subject2.class.getDeclaredMethod("method0"), new Subject2()))));

        assertTrue(mapper.isApplicable(new HandlerImpl(
                HandlerMethod.of(Subject3.class.getDeclaredMethod("method0"), new Subject3()))));
    }

    private static class Subject0 {
        private void method0() {
        }
    }

    private static class Subject1 extends Subject0 {
        private void method0() {
        }
    }

    @RestController
    private static class Subject2 {
        private void method0() {
        }
    }

    private static class Subject3 {
        private void method0() {
        }
    }
}
