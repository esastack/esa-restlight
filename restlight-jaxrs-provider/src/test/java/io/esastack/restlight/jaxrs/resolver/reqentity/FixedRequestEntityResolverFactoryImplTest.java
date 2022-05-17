/*
 * Copyright 2022 OPPO ESA Stack Project
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
package io.esastack.restlight.jaxrs.resolver.reqentity;

import esa.commons.Result;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.annotation.RequestSerializer;
import io.esastack.restlight.core.annotation.Serializer;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.RequestEntity;
import io.esastack.restlight.core.context.RequestEntityImpl;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.MethodParam;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.core.mock.MockHttpResponse;
import io.esastack.restlight.core.resolver.param.ParamResolver;
import io.esastack.restlight.core.resolver.param.ParamResolverContext;
import io.esastack.restlight.core.resolver.param.ParamResolverContextImpl;
import io.esastack.restlight.core.resolver.param.entity.FixedRequestEntityResolverFactory;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.serialize.JacksonHttpBodySerializer;
import io.esastack.restlight.core.serialize.JacksonSerializer;
import io.esastack.restlight.jaxrs.resolver.Pojo;
import io.esastack.restlight.jaxrs.resolver.ResolverUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedRequestEntityResolverFactoryImplTest {

    private static final FixedRequestEntityResolverFactory resolverFactory
            = new FixedRequestEntityResolverFactoryImpl();
    private static final Subject SUBJECT = new Subject();
    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testSupport() {
        final MethodParam absent = handlerMethods.get("requestBody").parameters()[0];
        assertFalse(resolverFactory.supports(absent));

        final MethodParam illegal = handlerMethods.get("illegalSpecifiedRequestBody").parameters()[0];
        assertFalse(resolverFactory.supports(illegal));

        final MethodParam support = handlerMethods.get("jacksonSpecifiedRequestBody").parameters()[0];
        assertTrue(resolverFactory.supports(support));

        final MethodParam supportOnMethod =
                handlerMethods.get("jacksonSpecifiedOnMethodRequestBody").parameters()[0];
        assertTrue(resolverFactory.supports(supportOnMethod));

        final MethodParam supportWithSerializerAnnotation =
                handlerMethods.get("jacksonSerializerSpecified").parameters()[0];
        assertTrue(resolverFactory.supports(supportWithSerializerAnnotation));
    }

    @Test
    void testSpecified() throws Exception {
        final Pojo origin = new Pojo(1024, "hell restlight");
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .withBody(JacksonSerializer.getDefaultMapper().writeValueAsBytes(origin))
                .build();
        final Object resolved = createResolverAndResolve(request, "jacksonSpecifiedRequestBody");
        assertEquals(origin, resolved);
    }

    private static Object createResolverAndResolve(HttpRequest request, String method) throws Exception {
        final MethodParam param = handlerMethods.get(method).parameters()[0];
        assertTrue(resolverFactory.supports(param));
        final ParamResolver resolver = resolverFactory.createResolver(param,
                ResolverUtils.defaultConverters(param),
                Collections.singletonList(new JacksonHttpBodySerializer()));
        final RequestContext context = new RequestContextImpl(request, MockHttpResponse.aMockResponse().build());
        final RequestEntity entity = new RequestEntityImpl(param, context);
        final ParamResolverContext resolverContext =
                new ParamResolverContextImpl(context, entity, param);
        return ((Result) resolver.resolve(resolverContext)).get();
    }

    private static class Subject {
        public void requestBody(Pojo pojo) {
        }

        public void illegalSpecifiedRequestBody(@RequestSerializer(HttpRequestSerializer.class) Pojo pojo) {
        }

        public void jacksonSpecifiedRequestBody(@RequestSerializer(JacksonHttpBodySerializer.class) Pojo pojo) {
        }

        @RequestSerializer(JacksonHttpBodySerializer.class)
        public void jacksonSpecifiedOnMethodRequestBody(Pojo pojo) {
        }

        @Serializer(JacksonHttpBodySerializer.class)
        public Pojo jacksonSerializerSpecified(Pojo pojo) {
            return new Pojo(11, "foo");
        }
    }

}

