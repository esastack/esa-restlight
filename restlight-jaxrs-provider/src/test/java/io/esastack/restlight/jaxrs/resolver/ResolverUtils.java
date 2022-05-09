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
package io.esastack.restlight.jaxrs.resolver;

import esa.commons.ClassUtils;
import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.buffer.BufferAllocator;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.handler.method.HandlerMethodImpl;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.context.ResponseEntityChannelImpl;
import io.esastack.restlight.core.context.ResponseEntityImpl;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolver;
import io.esastack.restlight.core.resolver.converter.StringConverterFactory;
import io.esastack.restlight.core.resolver.converter.StringConverterProvider;
import io.esastack.restlight.core.spi.impl.DefaultStringConverterFactory;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.mock.MockResponseContent;

import java.util.Map;
import java.util.stream.Collectors;

public class ResolverUtils {

    private static final StringConverterFactory defaultResolver = new DefaultStringConverterFactory();

    public static byte[] writtenContent(HttpRequest request,
                                        HttpResponse response,
                                        Object returnValue,
                                        HandlerMethod method,
                                        ResponseEntityResolver resolver) throws Exception {
        final Buffer buffer = BufferAllocator.getDefault().buffer();
        final RequestContext context = new RequestContextImpl(request, response);
        response.entity(returnValue);
        context.attrs().attr(RequestContextImpl.RESPONSE_CONTENT).set(new MockResponseContent(buffer));
        resolver.writeTo(new ResponseEntityImpl(method, response, MediaType.ALL),
                new ResponseEntityChannelImpl(context), context);
        return buffer.getBytes();
    }

    public static Map<String, HandlerMethod> extractHandlerMethods(Object target) {
        return ClassUtils.userDeclaredMethods(target.getClass())
                .stream()
                .map(method -> HandlerMethodImpl.of(ClassUtils.getUserType(target), method))
                .collect(Collectors.toMap(h -> h.method().getName(), hm -> hm));
    }

    public static StringConverterProvider defaultConverters(Param param) {
        return key -> defaultResolver.createConverter(StringConverterFactory.Key
                .of(key.genericType(), key.type(), param)).orElse(null);
    }

    private ResolverUtils() {
    }
}

