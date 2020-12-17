package esa.restlight.core.resolver.arg;

import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.method.Param;
import esa.restlight.server.route.predicate.PatternsPredicate;
import esa.restlight.server.util.PathVariableUtils;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractPathVariableArgumentResolverTest {

    @Test
    void testResolveStringValue() throws Exception {
        final Param param = mock(Param.class);
        when(param.type()).thenReturn((Class) int.class);
        when(param.genericType()).thenReturn(int.class);

        final AbstractPathVariableArgumentResolver resolver = asResolver("foo");
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final Map<String, String> variables = new HashMap<>();
        variables.put("foo", "1");
        variables.put("bar", "2");
        request.setAttribute(PatternsPredicate.TEMPLATE_VARIABLES, variables);
        final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();

        final Object ret = resolver.createResolver(param, Collections.emptyList()).resolve(request, response);
        assertEquals(1, ret);
    }

    private static AbstractPathVariableArgumentResolver asResolver(String name) {
        return new AbstractPathVariableArgumentResolver() {

            @Override
            protected NameAndValue createNameAndValue(Param parameter) {
                return new NameAndValue(name, true, null);
            }

            @Override
            public boolean supports(Param param) {
                return true;
            }
        };
    }

}
