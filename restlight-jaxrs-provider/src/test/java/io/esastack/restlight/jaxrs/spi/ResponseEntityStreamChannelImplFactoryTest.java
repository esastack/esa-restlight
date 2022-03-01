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
package io.esastack.restlight.jaxrs.spi;

import io.esastack.commons.net.buffer.Buffer;
import io.esastack.restlight.jaxrs.resolver.ResponseEntityStreamChannelImpl;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.mock.MockHttpResponse;
import io.esastack.restlight.server.mock.MockResponseContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResponseEntityStreamChannelImplFactoryTest {

    @Test
    void testAll() {
        final ResponseEntityStreamChannelImplFactory factory = new ResponseEntityStreamChannelImplFactory();
        assertEquals(-1000, factory.getOrder());

        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext context = new RequestContextImpl(request, response);
        context.attrs().attr(RequestContextImpl.RESPONSE_CONTENT).set(new MockResponseContent(Buffer
                .defaultAlloc().buffer()));

        assertNotNull(factory.create(context));
        assertEquals(ResponseEntityStreamChannelImpl.class, factory.create(context).getClass());
    }

}

