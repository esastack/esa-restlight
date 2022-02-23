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
package io.esastack.restlight.jaxrs.impl.ext;

import io.esastack.restlight.jaxrs.impl.core.LinkBuilderImpl;
import io.esastack.restlight.jaxrs.impl.core.ResponseBuilderImpl;
import io.esastack.restlight.jaxrs.impl.core.UriBuilderImpl;
import io.esastack.restlight.jaxrs.impl.core.VariantListBuilderImpl;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class RuntimeDelegateImplTest {

    @Test
    void testBasic() {
        RuntimeDelegateImpl delegate = new RuntimeDelegateImpl();
        assertTrue(delegate.createUriBuilder() instanceof UriBuilderImpl);
        assertTrue(delegate.createResponseBuilder() instanceof ResponseBuilderImpl);
        assertTrue(delegate.createVariantListBuilder() instanceof VariantListBuilderImpl);
        assertThrows(IllegalArgumentException.class, () -> delegate.createEndpoint(null, Object.class));
        assertThrows(UnsupportedOperationException.class,
                () -> delegate.createEndpoint(mock(Application.class), Object.class));
        assertTrue(delegate.createLinkBuilder() instanceof LinkBuilderImpl);

        assertNotNull(delegate.createHeaderDelegate(Cookie.class));
        assertNotNull(delegate.createHeaderDelegate(Date.class));
        assertNotNull(delegate.createHeaderDelegate(EntityTag.class));
        assertNotNull(delegate.createHeaderDelegate(Link.class));
        assertNotNull(delegate.createHeaderDelegate(Locale.class));
        assertNotNull(delegate.createHeaderDelegate(MediaType.class));
        assertNotNull(delegate.createHeaderDelegate(NewCookie.class));
        assertNotNull(delegate.createHeaderDelegate(URI.class));

        // test add headerDelegate
        assertNull(delegate.createHeaderDelegate(C1.class));
        assertNull(delegate.createHeaderDelegate(C2.class));
        assertNull(delegate.createHeaderDelegate(C3.class));

        RuntimeDelegateImpl.addHeaderDelegate(C1.class, new RuntimeDelegate.HeaderDelegate<C1>() {
            @Override
            public C1 fromString(String value) {
                return null;
            }

            @Override
            public String toString(C1 value) {
                return null;
            }
        });
        assertNotNull(RuntimeDelegate.getInstance().createHeaderDelegate(C1.class));

        RuntimeDelegateImpl.addHeaderDelegate(new RuntimeDelegate.HeaderDelegate<C2>() {
            @Override
            public C2 fromString(String value) {
                return null;
            }

            @Override
            public String toString(C2 value) {
                return null;
            }
        });
        assertNotNull(RuntimeDelegate.getInstance().createHeaderDelegate(C2.class));

        RuntimeDelegateImpl.addHeaderDelegateFactory(() -> new RuntimeDelegate.HeaderDelegate<C3>() {
            @Override
            public C3 fromString(String value) {
                return null;
            }

            @Override
            public String toString(C3 value) {
                return null;
            }
        });
        assertNotNull(RuntimeDelegate.getInstance().createHeaderDelegate(C3.class));
    }

    private static final class C1 {

    }

    private static final class C2 {

    }

    private static final class C3 {

    }

}

