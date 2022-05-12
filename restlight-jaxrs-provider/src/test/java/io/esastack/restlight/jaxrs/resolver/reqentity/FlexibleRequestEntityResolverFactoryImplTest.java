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

import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.RequestEntity;
import io.esastack.restlight.core.context.RequestEntityImpl;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.MethodParam;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.core.mock.MockHttpResponse;
import io.esastack.restlight.core.resolver.entity.request.FlexibleRequestEntityResolverFactory;
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolver;
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolverContext;
import io.esastack.restlight.core.resolver.entity.request.RequestEntityResolverContextImpl;
import io.esastack.restlight.core.serialize.GsonHttpBodySerializer;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.serialize.JacksonHttpBodySerializer;
import io.esastack.restlight.core.serialize.JacksonSerializer;
import io.esastack.restlight.jaxrs.resolver.Pojo;
import io.esastack.restlight.jaxrs.resolver.ResolverUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import jakarta.ws.rs.DefaultValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlexibleRequestEntityResolverFactoryImplTest {

    private static final FlexibleRequestEntityResolverFactory resolverFactory =
            new FlexibleRequestEntityResolverFactoryImpl();

    private static final Subject SUBJECT = new Subject();

    private static Map<String, HandlerMethod> handlerMethods;

    @BeforeAll
    static void setUp() {
        handlerMethods = ResolverUtils.extractHandlerMethods(SUBJECT);
    }

    @Test
    void testSupportIfAnnotationAbsent() throws Exception {
        final Pojo origin = new Pojo(1024, "hello restlight");
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .withBody(JacksonSerializer.getDefaultMapper().writeValueAsBytes(origin))
                .build();
        final Object resolved = createResolverAndResolve(request, "none");
        assertEquals(origin, resolved);
    }

    @Test
    void testDefautlValue() throws Exception {
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .build();
        final Object resolved = createResolverAndResolve(request, "defaultValue");
        assertEquals("default", resolved);
    }

    @Test
    void testMultiSerializer() throws Exception {
        final MethodParam param = handlerMethods.get("none").parameters()[0];

        List<HttpRequestSerializer> serializers = Arrays.asList(new GsonHttpBodySerializer() {
            @Override
            protected boolean supportsRead(RequestEntity entity) {
                return entity.mediaType().isCompatibleWith(MediaType.APPLICATION_XML);
            }
        }, new JacksonHttpBodySerializer());
        RequestEntityResolver resolver = new FlexibleRequestEntityResolverFactoryImpl()
                .createResolver(param, ResolverUtils.defaultConverters(param), serializers);

        final Pojo origin = new Pojo(1024, "hello restlight");
        final HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_UTF8.value())
                .withBody(JacksonSerializer.getDefaultMapper().writeValueAsBytes(origin))
                .build();
        final RequestContext context = new RequestContextImpl(request, MockHttpResponse.aMockResponse().build());
        final RequestEntityResolverContext resolverContext =
                new RequestEntityResolverContextImpl(Mockito.mock(DeployContext.class), context,
                        new RequestEntityImpl(param, context));
        final Object resolvedWithJson = resolver.resolve(resolverContext).get();
        assertEquals(origin, resolvedWithJson);

        final HttpRequest request2 = MockHttpRequest
                .aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_XML.value())
                .withBody(JacksonSerializer.getDefaultMapper().writeValueAsBytes(origin))
                .build();
        final RequestContext context2 = new RequestContextImpl(request2, MockHttpResponse.aMockResponse().build());
        final RequestEntityResolverContext resolverContext2 =
                new RequestEntityResolverContextImpl(Mockito.mock(DeployContext.class), context2,
                        new RequestEntityImpl(param, context2));
        final Object resolvedWithXml = resolver.resolve(resolverContext2).get();

        assertEquals(origin, resolvedWithXml);
    }

    private static Object createResolverAndResolve(HttpRequest request, String method) throws Exception {
        final MethodParam param = handlerMethods.get(method).parameters()[0];
        assertTrue(resolverFactory.supports(param));
        final RequestEntityResolver resolver = resolverFactory.createResolver(param,
                ResolverUtils.defaultConverters(param),
                Collections.singletonList(new JacksonHttpBodySerializer()));

        final RequestContext context = new RequestContextImpl(request, MockHttpResponse.aMockResponse().build());
        final RequestEntity entity = new RequestEntityImpl(param, context);
        final RequestEntityResolverContext resolverContext =
                new RequestEntityResolverContextImpl(Mockito.mock(DeployContext.class), context, entity);
        return resolver.resolve(resolverContext).get();
    }

    private static class Subject {

        public void none(Pojo pojo) {
        }

        public void defaultValue(@DefaultValue("default") String pojoStr) {
        }

    }

}

