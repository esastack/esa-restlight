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

import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.mock.MockHttpRequest;
import io.esastack.restlight.server.mock.MockHttpResponse;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class PostMatchContainerRequestContextTest {

    @Test
    void testBasic() {
        assertThrows(NullPointerException.class, () -> new PostMatchContainerRequestContext(null));
        final HttpRequest request = MockHttpRequest.aMockRequest().build();
        final HttpResponse response = MockHttpResponse.aMockResponse().build();
        final RequestContext context0 = new RequestContextImpl(request, response);
        assertDoesNotThrow(() -> new PostMatchContainerRequestContext(context0));

        PostMatchContainerRequestContext context = new PostMatchContainerRequestContext(context0);
        assertThrows(IllegalStateException.class, () -> context.setRequestUri(URI.create("/abc")));
        assertThrows(IllegalStateException.class, () -> context.setRequestUri(URI.create("/abc"), URI.create("/def")));
        assertThrows(IllegalStateException.class, () -> context.setMethod("get"));
        assertThrows(IllegalStateException.class, () -> context.setEntityStream(mock(InputStream.class)));
    }

}

