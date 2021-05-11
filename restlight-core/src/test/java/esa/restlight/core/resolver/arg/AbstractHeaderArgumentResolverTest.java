package esa.restlight.core.resolver.arg;

import esa.restlight.core.method.Param;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.handler.codec.http.HttpHeaders;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
class AbstractHeaderArgumentResolverTest {

    @Test
    void testResolveStringValue() throws Exception {
        final AbstractHeaderArgumentResolver resolver = asResolver("foo");
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withHeader("foo", "1")
                .build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) String.class);
        when(param.genericType()).thenReturn(String.class);
        Object ret = resolver.createResolver(param, Collections.emptyList()).resolve(request, response);
        assertEquals("1", ret);
    }

    @Test
    void testResolveMultiValueToSingleStringValue() throws Exception {
        final AbstractHeaderArgumentResolver resolver = asResolver("foo");
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withHeader("foo", "1")
                .withHeader("foo", "2")
                .build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) String.class);
        when(param.genericType()).thenReturn(String.class);
        Object ret = resolver.createResolver(param, Collections.emptyList()).resolve(request, response);
        assertEquals("1", ret);
    }

    @Test
    void testResolveMultiValuesToList() throws Exception {
        final AbstractHeaderArgumentResolver resolver = asResolver("foo");
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withHeader("foo", "1")
                .withHeader("foo", "2")
                .build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) Subject.class.getDeclaredMethod("intList").getReturnType());
        when(param.genericType()).thenReturn(Subject.class.getDeclaredMethod("intList").getGenericReturnType());
        Object ret = resolver.createResolver(param, Collections.emptyList()).resolve(request, response);
        assertTrue(ret instanceof List);
        assertTrue(((List<Integer>) ret).contains(1));
        assertTrue(((List<Integer>) ret).contains(2));
    }

    @Test
    void testResolveMultiValuesToArray() throws Exception {
        final AbstractHeaderArgumentResolver resolver = asResolver("foo");
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withHeader("foo", "1")
                .withHeader("foo", "2")
                .build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) Subject.class.getDeclaredMethod("longArray").getReturnType());
        when(param.genericType()).thenReturn(Subject.class.getDeclaredMethod("longArray").getGenericReturnType());
        Object ret = resolver.createResolver(param, Collections.emptyList()).resolve(request, response);
        assertTrue(ret instanceof long[]);
        assertArrayEquals(new long[]{1L, 2L}, ((long[]) ret));
    }

    @Test
    void testResolveHttpHeadersObject() throws Exception {
        final AbstractHeaderArgumentResolver resolver = asResolver("foo");
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withHeader("foo", "1")
                .withHeader("bar", "2")
                .build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) HttpHeaders.class);
        when(param.genericType()).thenReturn(HttpHeaders.class);
        Object ret = resolver.createResolver(param, Collections.emptyList()).resolve(request, response);
        assertTrue(ret instanceof HttpHeaders);
        assertTrue(((HttpHeaders) ret).contains("foo", "1", false));
        assertTrue(((HttpHeaders) ret).contains("bar", "2", false));
    }

    private static AbstractHeaderArgumentResolver asResolver(String name) {
        return new AbstractHeaderArgumentResolver() {
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
        List<Integer> intList();

        long[] longArray();
    }

}
