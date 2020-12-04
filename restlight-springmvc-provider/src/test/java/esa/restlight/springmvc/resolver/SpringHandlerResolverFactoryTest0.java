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
package esa.restlight.springmvc.resolver;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.annotation.RequestSerializer;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.MethodParam;
import esa.restlight.core.method.Param;
import esa.restlight.core.resolver.ArgumentResolverAdapter;
import esa.restlight.core.resolver.HandlerResolverFactory;
import esa.restlight.core.serialize.FastJsonHttpBodySerializer;
import esa.restlight.springmvc.MockUtils;
import esa.restlight.springmvc.ResolverUtils;
import esa.restlight.springmvc.annotation.shaded.RequestMapping0;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SpringHandlerResolverFactoryTest0 {

    private static final Subject SUBJECT = new Subject();

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        assumeTrue(RequestMapping0.shadedClass().getName().startsWith("org.springframework"));
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }


    @Test
    void testArgument() {
        final HandlerResolverFactory factory = MockUtils.mockResolverFactory();
        final MethodParam[] parameters = handlerMethods.get("full").parameters();
        assertNotNull(factory.getArgumentResolver(parameters[0]));
        assertNotNull(factory.getArgumentResolver(parameters[1]));
        assertNotNull(factory.getArgumentResolver(parameters[2]));
        assertNotNull(factory.getArgumentResolver(parameters[3]));
        assertNotNull(factory.getArgumentResolver(parameters[4]));
        assertNotNull(factory.getArgumentResolver(parameters[5]));
        assertNotNull(factory.getArgumentResolver(parameters[6]));
        assertNotNull(factory.getArgumentResolver(parameters[7]));
    }

    @Test
    void testCustomArgument() {

        final HandlerResolverFactory factory = MockUtils
                .mockResolverFactoryWithArgumentResolvers(Collections.singletonList(new CustomArgumentResolver()));
        final MethodParam[] parameters = handlerMethods.get("customArgument").parameters();
        assertEquals(CustomArgumentResolver.class, factory.getArgumentResolver(parameters[0]).getClass());
    }

    @Test
    void testAnnotationAbsentArgument() {
        final HandlerResolverFactory factory = MockUtils.mockResolverFactory();
        final MethodParam[] parameters = handlerMethods.get("none").parameters();
        assertNull(factory.getArgumentResolver(parameters[0]));
    }

    private static class CustomArgumentResolver implements ArgumentResolverAdapter {

        @Override
        public boolean supports(Param parameter) {
            return "foo".equals(parameter.name());
        }

        @Override
        public Object resolve(AsyncRequest request, AsyncResponse response) {
            return "bar";
        }
    }

    private static class Subject {

        public String none(String none) {
            return null;
        }

        public String customArgument(String foo) {
            return null;
        }

        public String full(@CookieValue String cookie,
                           @MatrixVariable String matrix,
                           @PathVariable String path,
                           @RequestAttribute String attr,
                           @RequestBody Pojo body,
                           @RequestHeader String header,
                           @RequestParam String param,
                           @RequestBody @RequestSerializer(FastJsonHttpBodySerializer.class) Pojo foo) {
            return null;
        }
    }

}
