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
package io.esastack.restlight.jaxrs.adapter;

import esa.commons.collection.AttributeKey;
import esa.commons.collection.AttributeMap;
import esa.commons.collection.Attributes;
import esa.commons.io.IOUtils;
import io.esastack.restlight.server.bootstrap.ResponseContent;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.esastack.restlight.server.core.HttpOutputStream;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.mock.MockHttpResponse;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResponseEntityStreamUtilsTest {

    @Test
    void testGetUnClosableOutputStream() {
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final Attributes attrs = new AttributeMap();
        final RequestContext context = new RequestContextImpl(attrs, request, response);

        assertThrows(NullPointerException.class, () -> ResponseEntityStreamUtils.getUnClosableOutputStream(context));
        final ResponseContent content = mock(ResponseContent.class);
        context.attrs().attr(RequestContextImpl.RESPONSE_CONTENT).set(content);
        when(content.alloc()).thenReturn(ByteBufAllocator.DEFAULT);
        HttpOutputStream os = ResponseEntityStreamUtils.getUnClosableOutputStream(context);
        assertNotNull(os);
        IOUtils.closeQuietly(os);
        assertFalse(os.isClosed());
    }

    @Test
    void testClose() throws Throwable {
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final Attributes attrs = new AttributeMap();
        final RequestContext context = new RequestContextImpl(attrs, request, response);
        final HttpOutputStream os = mock(HttpOutputStream.class);
        attrs.attr(AttributeKey.valueOf("$closable.stream")).set(os);
        ResponseEntityStreamUtils.close(context);
        verify(os).close();
    }

}

