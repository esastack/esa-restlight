package esa.restlight.core.resolver.arg;

import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.method.Param;
import esa.restlight.server.bootstrap.WebServerException;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractNameAndValueArgumentResolverTest {

    @Test
    void testResolveValue() throws Exception {
        final Object value = new Object();
        final Param param = mock(Param.class);
        final AbstractNameAndValueArgumentResolver resolver = new AbstractNameAndValueArgumentResolver(param) {

            @Override
            protected NameAndValue createNameAndValue(Param param) {
                return new NameAndValue("foo", true, null);
            }

            @Override
            protected Object resolveName(String name, AsyncRequest request) throws Exception {
                return value;
            }
        };
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        assertSame(value, resolver.resolve(request, response));
    }

    @Test
    void testMissingRequiredValue() {
        final Param param = mock(Param.class);
        final AbstractNameAndValueArgumentResolver resolver = new AbstractNameAndValueArgumentResolver(param) {

            @Override
            protected NameAndValue createNameAndValue(Param param) {
                return new NameAndValue("foo", true, null);
            }

            @Override
            protected Object resolveName(String name, AsyncRequest request) throws Exception {
                return null;
            }
        };
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        assertThrows(WebServerException.class, () -> resolver.resolve(request, response));
    }

    @Test
    void testMissingRequiredStringValue() {
        final Param param = mock(Param.class);
        final AbstractNameAndValueArgumentResolver resolver = new AbstractNameAndValueArgumentResolver(param) {

            @Override
            protected NameAndValue createNameAndValue(Param param) {
                return new NameAndValue("foo", true, null);
            }

            @Override
            protected Object resolveName(String name, AsyncRequest request) throws Exception {
                return "";
            }
        };
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        assertThrows(WebServerException.class, () -> resolver.resolve(request, response));
    }

    @Test
    void testUseDefaultValueIfMissing() throws Exception {
        final Object def = new Object();
        final Param param = mock(Param.class);
        final AbstractNameAndValueArgumentResolver resolver = new AbstractNameAndValueArgumentResolver(param) {

            @Override
            protected NameAndValue createNameAndValue(Param param) {
                return new NameAndValue("foo", true, def);
            }

            @Override
            protected Object resolveName(String name, AsyncRequest request) throws Exception {
                return null;
            }
        };
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        assertSame(def, resolver.resolve(request, response));
    }

    @Test
    void testUpdateNameByParamName() throws Exception {
        final Param param = mock(Param.class);
        when(param.name()).thenReturn("foo");
        final AtomicReference<String> nameRef = new AtomicReference<>();
        final AbstractNameAndValueArgumentResolver resolver = new AbstractNameAndValueArgumentResolver(param) {

            @Override
            protected NameAndValue createNameAndValue(Param param) {
                return new NameAndValue("", true, null);
            }

            @Override
            protected Object resolveName(String name, AsyncRequest request) throws Exception {
                nameRef.set(name);
                return new Object();
            }
        };
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        assertNotNull(resolver.resolve(request, response));
        assertEquals("foo", nameRef.get());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void testUseObjectDefaultValue() throws Exception {
        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) int.class);
        final AbstractNameAndValueArgumentResolver resolver = new AbstractNameAndValueArgumentResolver(param) {

            @Override
            protected NameAndValue createNameAndValue(Param param) {
                return new NameAndValue("foo", false, null);
            }

            @Override
            protected Object resolveName(String name, AsyncRequest request) throws Exception {
                return null;
            }
        };
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        assertEquals(0, resolver.resolve(request, response));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void testConvertDefaultStringValueToTargetType() throws Exception {
        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) int.class);
        when(param.genericType()).thenReturn(int.class);
        final AbstractNameAndValueArgumentResolver resolver = new AbstractNameAndValueArgumentResolver(param) {

            @Override
            protected NameAndValue createNameAndValue(Param param) {
                return new NameAndValue("foo", false, "1");
            }

            @Override
            protected Object resolveName(String name, AsyncRequest request) throws Exception {
                return null;
            }
        };
        final MockAsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
        assertEquals(1, resolver.resolve(request, response));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void testConvertDefaultStringValueToUnknownType() {
        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) Param.class);
        when(param.genericType()).thenReturn(Param.class);
        assertThrows(IllegalArgumentException.class, () -> new AbstractNameAndValueArgumentResolver(param) {

            @Override
            protected NameAndValue createNameAndValue(Param param) {
                return new NameAndValue("foo", false, "1");
            }

            @Override
            protected Object resolveName(String name, AsyncRequest request) throws Exception {
                return null;
            }
        });
    }

}
