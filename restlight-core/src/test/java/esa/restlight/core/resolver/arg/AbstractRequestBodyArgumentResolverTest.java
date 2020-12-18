package esa.restlight.core.resolver.arg;

import esa.commons.http.HttpHeaderNames;
import esa.commons.http.HttpHeaderValues;
import esa.restlight.core.method.Param;
import esa.restlight.core.serialize.JacksonHttpBodySerializer;
import esa.restlight.server.bootstrap.WebServerException;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
class AbstractRequestBodyArgumentResolverTest {

    @Test
    void testResolveTextPlainBody() throws Exception {
        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) int.class);
        when(param.genericType()).thenReturn(int.class);
        final AbstractRequestBodyArgumentResolver resolver = new AbstractRequestBodyArgumentResolver() {
            @Override
            protected boolean required(Param param) {
                return true;
            }
        };
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                .withBody("123".getBytes(StandardCharsets.UTF_8))
                .build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final Object ret = resolver.createResolver(param, Collections.singletonList(new JacksonHttpBodySerializer()))
                .resolve(request, response);
        assertEquals(123, ret);
    }

    @Test
    void testMissingRequiredTextPlainBody() {
        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) int.class);
        when(param.genericType()).thenReturn(int.class);
        final AbstractRequestBodyArgumentResolver resolver = new AbstractRequestBodyArgumentResolver() {
            @Override
            protected boolean required(Param param) {
                return true;
            }
        };
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                .build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        assertThrows(WebServerException.class,
                () -> resolver.createResolver(param, Collections.singletonList(new JacksonHttpBodySerializer()))
                        .resolve(request, response));
    }

    @Test
    void testResolveBodyBySerializer() throws Exception {
        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) Subject.class);
        when(param.genericType()).thenReturn(Subject.class);
        final AbstractRequestBodyArgumentResolver resolver = new AbstractRequestBodyArgumentResolver() {
            @Override
            protected boolean required(Param param) {
                return true;
            }
        };
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                .withBody("{\"foo\": \"bar\"}".getBytes(StandardCharsets.UTF_8))
                .build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final Object ret = resolver.createResolver(param, Collections.singletonList(new JacksonHttpBodySerializer()))
                .resolve(request, response);
        assertTrue(ret instanceof Subject);
        assertEquals("bar", ((Subject) ret).getFoo());
    }

    @Test
    void testMissingRequiredValue() {
        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) Subject.class);
        when(param.genericType()).thenReturn(Subject.class);
        final AbstractRequestBodyArgumentResolver resolver = new AbstractRequestBodyArgumentResolver() {
            @Override
            protected boolean required(Param param) {
                return true;
            }
        };
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                .build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        assertThrows(WebServerException.class,
                () -> resolver.createResolver(param, Collections.singletonList(new JacksonHttpBodySerializer()))
                        .resolve(request, response));
    }

    @Test
    void testNegotiation() throws Exception {
        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) Subject.class);
        when(param.genericType()).thenReturn(Subject.class);
        final AbstractRequestBodyArgumentResolver resolver =
                new AbstractRequestBodyArgumentResolver(true, "neg") {
                    @Override
                    protected boolean required(Param param) {
                        return true;
                    }
                };
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest()
                .withHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_XML)
                .withParameter("neg", "json")
                .withBody("{\"foo\": \"bar\"}".getBytes(StandardCharsets.UTF_8))
                .build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final Object ret = resolver.createResolver(param, Collections.singletonList(new JacksonHttpBodySerializer()))
                .resolve(request, response);
        assertTrue(ret instanceof Subject);
        assertEquals("bar", ((Subject) ret).getFoo());
    }

    private static class Subject {
        private String foo;

        public String getFoo() {
            return foo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }
    }

}
