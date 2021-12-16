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
package io.esastack.restlight.ext.multipart.resolver;

import io.esastack.restlight.core.method.MethodParam;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.ext.multipart.core.MultipartFile;
import io.esastack.restlight.server.bootstrap.WebServerException;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.impl.RequestContextImpl;
import io.esastack.restlight.server.mock.MockHttpResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultipartFileArgumentResolverTest extends AbstractMultipartResolverTest {

    private static Object createResolverAndResolve(HttpRequest request,
                                                   String method,
                                                   int index) throws Exception {
        final MethodParam parameter = handlerMethods.get(method).parameters()[index];
        assertTrue(fileResolver.supports(parameter));
        final ParamResolver resolver = fileResolver.createResolver(parameter, null);
        return resolver.resolve(parameter, new RequestContextImpl(request,
                MockHttpResponse.aMockResponse().build()));
    }

    private static Object createFormResolverAndResolve(HttpRequest request,
                                                       String method,
                                                       int index) throws Exception {
        final MethodParam parameter = handlerMethods.get(method).parameters()[index];
        assertTrue(attrResolver.supports(parameter));
        final ParamResolver resolver = attrResolver.createResolver(parameter, null);
        return resolver.resolve(parameter, new RequestContextImpl(request,
                MockHttpResponse.aMockResponse().build()));
    }

    @Test
    void testNormal() throws Exception {
        String body = "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"foo\"; filename=\"foo.tab\"\r\n" +
                "Content-Type: text/whatever\r\n" +
                "\r\n" +
                "happy\n" +
                "\r\n" +
                "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"multi\"; filename=\"foo1.tab\"\r\n" +
                "\r\n" +
                "value2\r\n" +
                "-----1234--\r\n";

        final MultipartFile resolved = (MultipartFile)
                createResolverAndResolve(build(body), "multipartFile", 0);
        assertEquals("foo.tab", resolved.originalFilename());
        resolved.delete();
    }

    @Test
    void testNamedFormParam() throws Exception {
        String body = "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"foo\"; filename=\"foo.tab\"\r\n" +
                "Content-Type: text/whatever\r\n" +
                "\r\n" +
                "happy\n" +
                "\r\n" +
                "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"multi\"; filename=\"foo1.tab\"\r\n" +
                "\r\n" +
                "value2\r\n" +
                "-----1234--\r\n";

        final MultipartFile resolved = (MultipartFile)
                createResolverAndResolve(build(body), "multipartFileName", 0);
        assertEquals("foo1.tab", resolved.originalFilename());
    }

    @Test
    void testRequiredFormParam() {
        String body = "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"foo\"; filename=\"foo.tab\"\r\n" +
                "Content-Type: text/whatever\r\n" +
                "\r\n" +
                "happy\n" +
                "\r\n" +
                "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"multi0\"; filename=\"foo1.tab\"\r\n" +
                "\r\n" +
                "value2\r\n" +
                "-----1234--\r\n";

        assertThrows(WebServerException.class,
                () -> createResolverAndResolve(build(body), "multipartFileName", 0));
    }

    @Test
    void testNoneRequiredFormParam() throws Exception {
        String body = "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"foo0\"; filename=\"foo.tab\"\r\n" +
                "Content-Type: text/whatever\r\n" +
                "\r\n" +
                "happy\n" +
                "\r\n" +
                "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"multi0\"; filename=\"foo1.tab\"\r\n" +
                "\r\n" +
                "value2\r\n" +
                "-----1234--\r\n";

        final MultipartFile resolved = (MultipartFile)
                createResolverAndResolve(build(body), "noneRequiredMultipartFile", 0);
        assertNull(resolved);
    }

    @Test
    void testCollection() throws Exception {
        String body = "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"foo\"; filename=\"foo.tab\"\r\n" +
                "Content-Type: text/whatever\r\n" +
                "\r\n" +
                "happy\n" +
                "\r\n" +
                "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"foo\"; filename=\"foo1.tab\"\r\n" +
                "\r\n" +
                "value2\r\n" +
                "-----1234--\r\n";

        @SuppressWarnings("unchecked") final List<MultipartFile> resolved = (List<MultipartFile>)

                createResolverAndResolve(build(body), "multipartFileList", 0);
        assertEquals(2, resolved.size());
        assertEquals("foo.tab", resolved.get(0).originalFilename());
        assertEquals("foo1.tab", resolved.get(1).originalFilename());
    }

    @Test
    void testMultipartFileAndFormParam0() throws Exception {
        String body = "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"foo\"; filename=\"foo.tab\"\r\n" +
                "Content-Type: text/whatever\r\n" +
                "\r\n" +
                "happy\n" +
                "\r\n" +
                "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"baz\"\r\n" +
                "\r\n" +
                "value2\r\n" +
                "-----1234--\r\n";

        @SuppressWarnings("unchecked") final List<MultipartFile> resolved0 = (List<MultipartFile>)
                createResolverAndResolve(build(body), "multipartFileAndFormParam0", 0);
        assertEquals(1, resolved0.size());
        assertEquals("foo.tab", resolved0.get(0).originalFilename());

        final Object resolved1 =
                createFormResolverAndResolve(build(body), "multipartFileAndFormParam0", 1);

        assertEquals("value2", resolved1);
    }

    @Test
    void testMultipartFileAndFormParam1() throws Exception {
        String body = "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"foo\"; filename=\"foo.tab\"\r\n" +
                "Content-Type: text/whatever\r\n" +
                "\r\n" +
                "happy\n" +
                "\r\n" +
                "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"baz\"\r\n" +
                "\r\n" +
                "value2\r\n" +
                "-----1234--\r\n";

        @SuppressWarnings("unchecked") final List<MultipartFile> resolved0 = (List<MultipartFile>)
                createResolverAndResolve(build(body), "multipartFileAndFormParam1", 1);
        assertEquals(1, resolved0.size());
        assertEquals("foo.tab", resolved0.get(0).originalFilename());

        final Object resolved1 = createFormResolverAndResolve(build(body), "multipartFileAndFormParam1", 0);
        assertEquals("value2", resolved1);
    }

}
