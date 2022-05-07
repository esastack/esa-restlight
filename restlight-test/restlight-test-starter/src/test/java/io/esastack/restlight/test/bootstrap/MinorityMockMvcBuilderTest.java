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

import esa.commons.Result;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.interceptor.HandlerInterceptor;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.param.ParamResolverAdapter;
import io.esastack.restlight.core.resolver.param.ParamResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.param.ParamResolverContext;
import io.esastack.restlight.core.context.RequestEntity;
import io.esastack.restlight.core.context.ResponseEntity;
import io.esastack.restlight.core.resolver.rspentity.ResponseEntityResolverAdapter;
import io.esastack.restlight.core.resolver.rspentity.ResponseEntityResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.rspentity.ResponseEntityResolverContext;
import io.esastack.restlight.core.resolver.rspentity.AbstractResponseEntityResolver;
import io.esastack.restlight.core.serialize.HttpBodySerializer;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.test.context.MockMvc;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.Collections;
import java.util.List;

class MinorityMockMvcBuilderTest {

    @Test
    void testPerform() {
        final MinorityMockMvcBuilder builder = new MinorityMockMvcBuilder(new Object());
        builder.controllerAdvices(A.class, new B());
        builder.serializers(Collections.singletonList(new HttpBodySerializerImpl()));
        builder.paramResolvers(Collections.singletonList(new ParamResolverImpl()));
        builder.paramResolverAdvices(Collections.singletonList(new ParamResolverAdviceImpl()));
        builder.responseEntityResolvers(Collections.singletonList(new ResponseEntityResolverImpl()));
        builder.responseEntityResolverAdvices(Collections.singletonList(new ResponseEntityResolverAdviceImpl()));
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
        public <T> Result<T, Void> deserialize(RequestEntity entity) {
            return null;
        }

        @Override
        public Result<byte[], Void> serialize(ResponseEntity entity) {
            return Result.ok(new byte[0]);
        }

        @Override
        public int getOrder() {
            return 10000;
        }
    }

    private static class ParamResolverImpl implements ParamResolverAdapter {

        private ParamResolverImpl() {
        }

        @Override
        public Object resolve(RequestContext context) {
            return null;
        }

        @Override
        public boolean supports(Param param) {
            return false;
        }
    }

    private static class ParamResolverAdviceImpl implements ParamResolverAdviceAdapter {

        private ParamResolverAdviceImpl() {
        }

        @Override
        public Object aroundResolve(ParamResolverContext context) throws Exception {
            return context.proceed();
        }

        @Override
        public boolean supports(Param param) {
            return false;
        }
    }

    private static class ResponseEntityResolverImpl extends AbstractResponseEntityResolver
            implements ResponseEntityResolverAdapter {

        private ResponseEntityResolverImpl() {
        }

        @Override
        protected byte[] serialize(ResponseEntity entity,
                                   List<MediaType> mediaTypes,
                                   RequestContext context) {
            return new byte[0];
        }

        @Override
        public boolean supports(HandlerMethod method) {
            return true;
        }
    }

    private static class ResponseEntityResolverAdviceImpl implements ResponseEntityResolverAdviceAdapter {

        private ResponseEntityResolverAdviceImpl() {
        }

        @Override
        public void aroundWrite(ResponseEntityResolverContext context) throws Exception {
            context.proceed();
        }

        @Override
        public boolean supports(HandlerMethod method) {
            return true;
        }
    }

    private static class InterceptorImpl implements HandlerInterceptor {

    }

}

