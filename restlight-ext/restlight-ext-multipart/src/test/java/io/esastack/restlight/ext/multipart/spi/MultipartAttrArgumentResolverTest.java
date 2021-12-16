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
package io.esastack.restlight.ext.multipart.spi;

import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.core.context.impl.HttpResponseAdapter;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.method.MethodParam;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.util.ConverterUtils;
import io.esastack.restlight.server.bootstrap.WebServerException;
import io.esastack.restlight.test.mock.MockHttpResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MultipartAttrArgumentResolverTest extends AbstractMultipartResolverTest {

    private static Object createResolverAndResolve(HttpRequest request, String method) throws Exception {
        final MethodParam parameter = handlerMethods.get(method).parameters()[0];
        assertTrue(attrResolver.supports(parameter));
        HandlerResolverFactory resolverFactory = mock(HandlerResolverFactory.class);
        when(resolverFactory.getStringConverter(parameter, parameter.type(), parameter.genericType()))
                .thenReturn(ConverterUtils.str2ObjectConverter(parameter.genericType())::apply);
        final ParamResolver resolver = attrResolver.createResolver(parameter, resolverFactory);
        return resolver.resolve(parameter, new RequestContextImpl(request,
                new HttpResponseAdapter(MockHttpResponse.aMockResponse().build())));
    }

    @Test
    void testNormal() throws Exception {
        String body = "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"foo\"\r\n" +
                "\r\n" +
                "value2\r\n" +
                "-----1234--\r\n";

        final Object resolved = createResolverAndResolve(build(body), "formParam");
        assertEquals("value2", resolved);
    }

    @Test
    void testNamedFormParam() throws Exception {
        String body = "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"baz\"\r\n" +
                "\r\n" +
                "value2\r\n" +
                "-----1234--\r\n";

        final Object resolved = createResolverAndResolve(build(body), "formParamName");
        assertEquals("value2", resolved);
    }

    @Test
    void testRequiredFormParam() {
        String body = "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"foo1\"\r\n" +
                "\r\n" +
                "value2\r\n" +
                "-----1234--\r\n";

        assertThrows(WebServerException.class, () -> createResolverAndResolve(build(body), "formParamName"));
    }

    @Test
    void testNoneRequiredFormParam() throws Exception {
        String body = "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"foo1\"\r\n" +
                "\r\n" +
                "value2\r\n" +
                "-----1234--\r\n";

        final Object resolved = createResolverAndResolve(build(body), "noneRequiredParam");
        assertNull(resolved);
    }

    @Test
    void testDefaultFormParam() throws Exception {
        final Object resolved = createResolverAndResolve(build(new byte[0]), "defaultFormParam");
        assertEquals("foo", resolved);
    }

    @Test
    void testDefaultAndRequiredFormParam() throws Exception {
        String body = "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"foo1\"\r\n" +
                "\r\n" +
                "value2\r\n" +
                "-----1234--\r\n";

        final Object resolved = createResolverAndResolve(build(body), "defaultAndRequiredFormParam");
        assertEquals("foo", resolved);
    }

    @Test
    void testCollection() throws Exception {
        String body = "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"foo\"\r\n" +
                "\r\n" +
                "value1,value2,value3\r\n" +
                "-----1234--\r\n";

        @SuppressWarnings("unchecked") final List<String> resolved =
                (List<String>) createResolverAndResolve(build(body), "formParamCollection");
        assertEquals(3, resolved.size());
        assertEquals("value1", resolved.get(0));
        assertEquals("value2", resolved.get(1));
        assertEquals("value3", resolved.get(2));
    }
}
