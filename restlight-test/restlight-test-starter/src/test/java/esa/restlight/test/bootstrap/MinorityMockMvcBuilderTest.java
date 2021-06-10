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
package esa.restlight.test.bootstrap;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.httpserver.core.HttpInputStream;
import esa.httpserver.core.HttpOutputStream;
import esa.restlight.core.interceptor.HandlerInterceptor;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.method.Param;
import esa.restlight.core.resolver.ArgumentResolverAdapter;
import esa.restlight.core.resolver.ArgumentResolverAdviceAdapter;
import esa.restlight.core.resolver.ReturnValueResolverAdapter;
import esa.restlight.core.resolver.ReturnValueResolverAdviceAdapter;
import esa.restlight.core.serialize.HttpBodySerializer;
import esa.restlight.test.context.MockMvc;
import esa.restlight.test.mock.MockAsyncRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.lang.reflect.Type;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MinorityMockMvcBuilderTest {

    @Test
    void testPerform() {
        final MinorityMockMvcBuilder builder = new MinorityMockMvcBuilder(new Object());
        builder.controllerAdvices(A.class, new B());
        builder.serializers(Collections.singletonList(new HttpBodySerializerImpl()));
        builder.argumentResolvers(Collections.singletonList(new ArgResolver()));
        builder.argumentResolverAdvices(Collections.singletonList(new ArgResolverAdvice()));
        builder.returnValueResolvers(Collections.singletonList(new RetResolver()));
        builder.returnValueResolverAdvices(Collections.singletonList(new RetResolverAdvice()));
        builder.interceptors(Collections.singletonList(new InterceptorImpl()));

        final MockMvc mvc = builder.build();
        mvc.perform(MockAsyncRequest.aMockRequest().withUri("/abc").build()).addExpect(result ->
                assertEquals(404, result.response().status()));
    }

    @ControllerAdvice
    public static class A {

    }

    @ControllerAdvice
    public static class B {

    }

    private static class HttpBodySerializerImpl implements HttpBodySerializer {
        @Override
        public Object customResponse(AsyncRequest request, AsyncResponse response, Object returnValue) {
            return null;
        }

        @Override
        public <T> T deSerialize(byte[] data, Type type) throws Exception {
            return null;
        }

        @Override
        public <T> T deSerialize(HttpInputStream inputStream, Type type) throws Exception {
            return null;
        }

        @Override
        public byte[] serialize(Object target) throws Exception {
            return new byte[0];
        }

        @Override
        public void serialize(Object target, HttpOutputStream outputStream) throws Exception {

        }
    }

    private static class ArgResolver implements ArgumentResolverAdapter {

        private ArgResolver() {
        }

        @Override
        public Object resolve(AsyncRequest request, AsyncResponse response) throws Exception {
            return null;
        }

        @Override
        public boolean supports(Param param) {
            return false;
        }
    }

    private static class ArgResolverAdvice implements ArgumentResolverAdviceAdapter {
        private ArgResolverAdvice() {
        }

        @Override
        public boolean supports(Param param) {
            return false;
        }
    }

    private static class RetResolver implements ReturnValueResolverAdapter {

        private RetResolver() {
        }

        @Override
        public byte[] resolve(Object returnValue, AsyncRequest request, AsyncResponse response) throws Exception {
            return new byte[0];
        }

        @Override
        public boolean supports(InvocableMethod invocableMethod) {
            return false;
        }
    }

    private static class RetResolverAdvice implements ReturnValueResolverAdviceAdapter {
        private RetResolverAdvice() {
        }

        @Override
        public boolean supports(InvocableMethod invocableMethod) {
            return false;
        }
    }

    private static class InterceptorImpl implements HandlerInterceptor {

    }

}

