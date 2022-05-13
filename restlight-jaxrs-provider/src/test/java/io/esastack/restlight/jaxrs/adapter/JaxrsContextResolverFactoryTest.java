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

import esa.commons.collection.AttributeMap;
import esa.commons.collection.Attributes;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.handler.method.MethodParamImpl;
import io.esastack.restlight.core.resolver.param.ParamResolverContext;
import io.esastack.restlight.core.resolver.param.ParamResolverContextImpl;
import io.esastack.restlight.core.route.predicate.ProducesPredicate;
import io.esastack.restlight.core.util.Ordered;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Providers;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JaxrsContextResolverFactoryTest {

    @Test
    void testBasic() throws Throwable {
        assertThrows(NullPointerException.class, () -> new JaxrsContextResolverFactory(null));
        final Providers providers = mock(Providers.class);
        final JaxrsContextResolverFactory resolver = new JaxrsContextResolverFactory(providers);
        assertEquals(Ordered.LOWEST_PRECEDENCE, resolver.getOrder());

        final Method method = this.getClass().getDeclaredMethod("demo", String.class, String.class);
        when(providers.getContextResolver(String.class, MediaType.WILDCARD_TYPE)).thenReturn(type -> null);
        assertTrue(resolver.supports(new MethodParamImpl(method, 0)));
        assertFalse(resolver.supports(new MethodParamImpl(method, 1)));
        when(providers.getContextResolver(String.class, MediaType.WILDCARD_TYPE)).thenReturn(null);
        assertFalse(resolver.supports(new MethodParamImpl(method, 0)));

        final Attributes attributes = new AttributeMap();
        final RequestContext context = new RequestContextImpl(attributes, mock(HttpRequest.class),
                mock(HttpResponse.class));
        final ParamResolverContext resolverContext = new ParamResolverContextImpl(context);
        attributes.attr(ProducesPredicate.COMPATIBLE_MEDIA_TYPES)
                .set(Collections.singletonList(io.esastack.commons.net.http.MediaType.ALL));
        when(providers.getContextResolver(String.class, MediaType.WILDCARD_TYPE)).thenReturn(type -> "Hello");
        assertEquals("Hello", resolver.createResolver(new MethodParamImpl(method, 0),
                null, null).resolve(resolverContext));
        attributes.attr(ProducesPredicate.COMPATIBLE_MEDIA_TYPES)
                .set(Collections.singletonList(io.esastack.commons.net.http.MediaType.APPLICATION_JSON));
        assertNull(resolver.createResolver(new MethodParamImpl(method, 0),
                null, null).resolve(resolverContext));
    }

    private String demo(@Context String name, String address) {
        return "Demo";
    }
}

