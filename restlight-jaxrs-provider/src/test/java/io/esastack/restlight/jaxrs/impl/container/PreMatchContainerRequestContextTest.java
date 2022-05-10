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
package io.esastack.restlight.jaxrs.impl.container;

import esa.commons.collection.AttributeMap;
import esa.commons.io.IOUtils;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.restlight.core.filter.FilterContext;
import io.esastack.restlight.core.filter.FilterContextImpl;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.filter.FilteringRequestImpl;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.core.mock.MockHttpResponse;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class PreMatchContainerRequestContextTest {

    @Test
    void testBasic() throws Throwable {
        assertThrows(NullPointerException.class, () -> new PreMatchContainerRequestContext(null));
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final FilterContext context0 = new FilterContextImpl(new AttributeMap(),
                new FilteringRequestImpl(request), response);

        assertDoesNotThrow(() -> new PreMatchContainerRequestContext(context0));
        final PreMatchContainerRequestContext context = new PreMatchContainerRequestContext(context0);
        assertEquals("/", context0.request().uri());
        context.setRequestUri(URI.create("/abc/def?m=n&c=d#x=y"));
        assertEquals("/abc/def?m=n&c=d#x=y", context0.request().uri());

        context.setRequestUri(URI.create("http://localhost:8080/abc"), URI.create("/xyz"));
        assertEquals("http://localhost:8080/abc", context.getUriInfo().getBaseUri().toString());
        assertEquals("/xyz", context0.request().uri());

        context.setMethod("POST");
        assertEquals(HttpMethod.POST, context0.request().method());
        context.setMethod("puT");
        assertEquals(HttpMethod.PUT, context0.request().method());

        final InputStream ins = mock(InputStream.class);
        context.setEntityStream(ins);
        verify(ins, never()).close();
        IOUtils.closeQuietly(context0.request().inputStream());
        verify(ins, times(2)).close();
    }
}

