package esa.restlight.core.resolver.arg;

import esa.restlight.core.method.Param;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.handler.codec.http.cookie.Cookie;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
class AbstractCookieValueArgumentResolverTest {

    @Test
    void testResolveStringValue() throws Exception {
        final AbstractCookieValueArgumentResolver resolver = asResolver("foo");
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withCookie("foo", "1")
                .build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) String.class);
        when(param.genericType()).thenReturn(String.class);
        Object ret = resolver.createResolver(param, Collections.emptyList()).resolve(request, response);
        assertEquals("1", ret);
    }

    @Test
    void testResolveCookieObject() throws Exception {
        final AbstractCookieValueArgumentResolver resolver = asResolver("foo");
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withCookie("foo", "1")
                .build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) Cookie.class);
        when(param.genericType()).thenReturn(Cookie.class);
        Object ret = resolver.createResolver(param, Collections.emptyList()).resolve(request, response);
        assertTrue(ret instanceof Cookie);
        assertEquals("foo", ((Cookie) ret).name());
        assertEquals("1", ((Cookie) ret).value());
    }

    @Test
    void testResolveCookieSet() throws Exception {
        final AbstractCookieValueArgumentResolver resolver = asResolver("foo");
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withCookie("foo", "1")
                .withCookie("bar", "2")
                .build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) Subject.class.getDeclaredMethod("cookieSet").getReturnType());
        when(param.genericType()).thenReturn(Subject.class.getDeclaredMethod("cookieSet").getGenericReturnType());
        Object ret = resolver.createResolver(param, Collections.emptyList()).resolve(request, response);
        assertTrue(ret instanceof Set);
        final Iterator<Cookie> iterator = ((Set<Cookie>) ret).iterator();
        assertEquals("2", iterator.next().value());
        assertEquals("1", iterator.next().value());
    }

    private static AbstractCookieValueArgumentResolver asResolver(String name) {
        return new AbstractCookieValueArgumentResolver() {
            @Override
            protected NameAndValue createNameAndValue(Param param) {
                return new NameAndValue(name, true);
            }

            @Override
            public boolean supports(Param param) {
                return true;
            }
        };
    }

    private interface Subject {
        Set<Cookie> cookieSet();
    }
}
