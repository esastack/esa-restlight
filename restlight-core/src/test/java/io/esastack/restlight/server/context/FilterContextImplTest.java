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
package io.esastack.restlight.server.context;

import esa.commons.collection.AttributeKey;
import esa.commons.collection.AttributeMap;
import io.esastack.restlight.server.context.impl.FilterContextImpl;
import io.esastack.restlight.server.core.FilteringRequest;
import io.esastack.restlight.server.core.HttpResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class FilterContextImplTest {

    @Test
    void testAll() {
        final FilteringRequest request = mock(FilteringRequest.class);
        final HttpResponse response = mock(HttpResponse.class);
        final FilterContext context = new FilterContextImpl(new AttributeMap(), request, response);
        assertEquals(context.request(), request);
        assertEquals(context.response(), response);
        final String key = "key";
        final String value = "value";
        context.attrs().attr(AttributeKey.stringKey(key)).set(value);
        assertEquals(value, context.attrs().attr(AttributeKey.stringKey(key)).get());
    }

}
