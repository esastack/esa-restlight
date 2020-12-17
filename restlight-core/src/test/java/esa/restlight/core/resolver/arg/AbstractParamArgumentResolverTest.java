package esa.restlight.core.resolver.arg;

import esa.restlight.core.method.Param;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
class AbstractParamArgumentResolverTest {

    @Test
    void testResolveSingleString() throws Exception {
        final AbstractParamArgumentResolver resolver = asResolver("foo");
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withParameter("foo", "1")
                .build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) String.class);
        when(param.genericType()).thenReturn(String.class);
        Object ret = resolver.createResolver(param, Collections.emptyList()).resolve(request, response);
        assertEquals("1", ret);
    }

    @Test
    void testResolveMultiValueToSingleString() throws Exception {
        final AbstractParamArgumentResolver resolver = asResolver("foo");
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withParameter("foo", "1")
                .withParameter("foo", "2")
                .build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) String.class);
        when(param.genericType()).thenReturn(String.class);
        Object ret = resolver.createResolver(param, Collections.emptyList()).resolve(request, response);
        assertEquals("1", ret);
    }

    @Test
    void testResolveMultiValueToMultiValueMap() throws Exception {
        final AbstractParamArgumentResolver resolver = asResolver("");
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withParameter("foo", "1")
                .withParameter("bar", "2")
                .build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final Param param = mock(Param.class);
        when(param.name()).thenReturn("foo");
        when(param.type()).thenReturn((Class) Subject.class.getDeclaredMethod("multiMap").getReturnType());
        when(param.genericType()).thenReturn(Subject.class.getDeclaredMethod("multiMap").getGenericReturnType());
        Object ret = resolver.createResolver(param, Collections.emptyList()).resolve(request, response);
        assertTrue(ret instanceof Map);
        assertEquals("1", ((Map<String, List<String>>) ret).get("foo").get(0));
        assertEquals("2", ((Map<String, List<String>>) ret).get("bar").get(0));
    }

    @Test
    void testResolveMultiValueToSingleValueMap() throws Exception {
        final AbstractParamArgumentResolver resolver = asResolver("");
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withParameter("foo", "1")
                .withParameter("bar", "2")
                .build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final Param param = mock(Param.class);
        when(param.name()).thenReturn("foo");
        when(param.type()).thenReturn((Class) Subject.class.getDeclaredMethod("singleMap").getReturnType());
        when(param.genericType()).thenReturn(Subject.class.getDeclaredMethod("singleMap").getGenericReturnType());
        Object ret = resolver.createResolver(param, Collections.emptyList()).resolve(request, response);
        assertTrue(ret instanceof Map);
        assertEquals("1", ((Map<String, String>) ret).get("foo"));
        assertEquals("2", ((Map<String, String>) ret).get("bar"));
    }

    @Test
    void testResolveMultiValueToList() throws Exception {
        final AbstractParamArgumentResolver resolver = asResolver("foo");
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withParameter("foo", "1")
                .withParameter("foo", "2")
                .build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) Subject.class.getDeclaredMethod("intList").getReturnType());
        when(param.genericType()).thenReturn(Subject.class.getDeclaredMethod("intList").getGenericReturnType());
        Object ret = resolver.createResolver(param, Collections.emptyList()).resolve(request, response);
        assertTrue(ret instanceof List);
        assertArrayEquals(new Integer[]{1, 2}, ((List<?>) ret).toArray());
    }

    @Test
    void testResolveMultiValueToArray() throws Exception {
        final AbstractParamArgumentResolver resolver = asResolver("foo");
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withParameter("foo", "1")
                .withParameter("foo", "2")
                .build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) Subject.class.getDeclaredMethod("longArray").getReturnType());
        when(param.genericType()).thenReturn(Subject.class.getDeclaredMethod("longArray").getGenericReturnType());
        Object ret = resolver.createResolver(param, Collections.emptyList()).resolve(request, response);
        assertTrue(ret instanceof long[]);
        assertArrayEquals(new long[]{1L, 2L}, (long[]) ret);
    }

    private static AbstractParamArgumentResolver asResolver(String name) {
        return new AbstractParamArgumentResolver() {
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
        Map<String, List<String>> multiMap();

        Map<String, String> singleMap();

        List<Integer> intList();

        long[] longArray();
    }
}
