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
package io.esastack.restlight.test.bootstrap;

import io.esastack.httpserver.core.HttpOutputStream;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.interceptor.HandlerInterceptor;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandledValue;
import io.esastack.restlight.core.resolver.ParamResolverAdapter;
import io.esastack.restlight.core.resolver.ParamResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.RequestEntity;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.resolver.ResponseEntityResolver;
import io.esastack.restlight.core.resolver.ResponseEntityResolverAdviceAdapter;
import io.esastack.restlight.core.serialize.HttpBodySerializer;
import io.esastack.restlight.test.context.MockMvc;
import io.esastack.restlight.test.mock.MockHttpRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.Collections;

class MinorityMockMvcBuilderTest {

    @Test
    void testPerform() {
        final MinorityMockMvcBuilder builder = new MinorityMockMvcBuilder(new Object());
        builder.controllerAdvices(A.class, new B());
        builder.serializers(Collections.singletonList(new HttpBodySerializerImpl()));
        builder.paramResolvers(Collections.singletonList(new ArgResolver()));
        builder.paramResolverAdvices(Collections.singletonList(new ArgResolverAdvice()));
        builder.responseEntityResolvers(Collections.singletonList(new RetResolver()));
        builder.responseEntityResolverAdvices(Collections.singletonList(new RetResolverAdvice()));
        builder.interceptors(Collections.singletonList(new InterceptorImpl()));

        final MockMvc mvc = builder.build();
        mvc.perform(MockHttpRequest.aMockRequest().withUri("/abc").build()).addExpect(result ->
                Assertions.assertEquals(404, result.response().status()));
    }

    @ControllerAdvice
    public static class A {

    }

    @ControllerAdvice
    public static class B {

    }

    private static class HttpBodySerializerImpl implements HttpBodySerializer {

        @Override
        public <T> HandledValue<T> deserialize(RequestEntity entity) {
            return null;
        }

        @Override
        public HandledValue<byte[]> serialize(ResponseEntity entity) {
            return HandledValue.succeed(new byte[0]);
        }

        @Override
        public HandledValue<Void> serialize(ResponseEntity entity, HttpOutputStream outputStream) {
            return null;
        }
    }

    private static class ArgResolver implements ParamResolverAdapter {

        private ArgResolver() {
        }

        @Override
        public Object resolve(Param param, RequestContext context) {
            return null;
        }

        @Override
        public boolean supports(Param param) {
            return false;
        }
    }

    private static class ArgResolverAdvice implements ParamResolverAdviceAdapter {
        private ArgResolverAdvice() {
        }

        @Override
        public boolean supports(Param param) {
            return false;
        }
    }

    private static class RetResolver implements ResponseEntityResolver {

        private RetResolver() {
        }

        @Override
        public HandledValue<Void> writeTo(ResponseEntity entity,
                                          io.esastack.httpserver.core.RequestContext context) {
            return HandledValue.succeed(null);
        }
    }

    private static class RetResolverAdvice implements ResponseEntityResolverAdviceAdapter {
        private RetResolverAdvice() {
        }

        @Override
        public boolean supports(HandlerMethod handlerMethod) {
            return false;
        }
    }

    private static class InterceptorImpl implements HandlerInterceptor {

    }

}

