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
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ProvidersPredicateTest {

    @Test
    void testPredicate() {
        final Attributes attrs = new AttributeMap();
        final RequestContext context = new RequestContextImpl(attrs,
                mock(HttpRequest.class), mock(HttpResponse.class));
        assertTrue(ProvidersPredicate.BINDING_GLOBAL.test(context));
        assertFalse(ProvidersPredicate.BINDING_HANDLER.test(context));

        attrs.attr(AttributeKey.valueOf("$internal.handled.method")).set(mock(HandlerMethod.class));
        assertFalse(ProvidersPredicate.BINDING_GLOBAL.test(context));
        assertTrue(ProvidersPredicate.BINDING_HANDLER.test(context));
    }

}

