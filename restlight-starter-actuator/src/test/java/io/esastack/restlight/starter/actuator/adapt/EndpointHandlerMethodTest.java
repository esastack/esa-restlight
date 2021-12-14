/*
 * Copyright 2020 OPPO ESA Stack Project
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
package io.esastack.restlight.starter.actuator.adapt;

import io.esastack.httpserver.core.RequestContext;
import io.esastack.restlight.core.handler.Handler;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.MethodParam;
import io.esastack.restlight.springmvc.annotation.shaded.RequestBody0;
import io.esastack.restlight.springmvc.annotation.shaded.ResponseBody0;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.endpoint.InvocationContext;
import org.springframework.boot.actuate.endpoint.OperationType;
import org.springframework.boot.actuate.endpoint.web.WebEndpointHttpMethod;
import org.springframework.boot.actuate.endpoint.web.WebOperation;
import org.springframework.boot.actuate.endpoint.web.WebOperationRequestPredicate;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EndpointHandlerMethodTest {

    @Test
    void testSpringMvc() {
        final Handler handler = EndpointHandlerMethod.forSpringMvc(webOperation());
        final HandlerMethod handlerMethod = handler.handlerMethod();
        assertTrue(handlerMethod.hasMethodAnnotation(ResponseBody0.shadedClass()));
        final Annotation responseBody = handlerMethod.getMethodAnnotation(ResponseBody0.shadedClass());
        assertNotNull(responseBody);
        assertEquals(ResponseBody0.shadedClass(), responseBody.annotationType());
        assertNotEquals(0, responseBody.hashCode());
        assertNotNull(responseBody.toString());
        assertEquals(responseBody, handlerMethod.getMethodAnnotation(ResponseBody0.shadedClass()));
        assertEquals(responseBody,
                EndpointHandlerMethod.forSpringMvc(webOperation()).handlerMethod()
                        .getMethodAnnotation(ResponseBody0.shadedClass()));
        assertNotNull(responseBody.toString());

        assertNotNull(handlerMethod.parameters());
        assertEquals(2, handlerMethod.parameters().length);
        assertEquals(handlerMethod.parameters()[0].type(), RequestContext.class);
        assertEquals(handlerMethod.parameters()[1].type(), Map.class);
        final MethodParam body = handlerMethod.parameters()[1];

        assertTrue(body.hasAnnotation(RequestBody0.shadedClass()));
        final Annotation requestBody = body.getAnnotation(RequestBody0.shadedClass());
        assertNotNull(body.annotations());
        assertEquals(1, body.annotations().length);
        assertEquals(RequestBody0.shadedClass(), body.annotations()[0].annotationType());

        assertNotNull(requestBody);
        assertEquals(RequestBody0.shadedClass(), requestBody.annotationType());
        assertNotEquals(0, responseBody.hashCode());
        assertNotNull(requestBody.toString());
        assertEquals(requestBody, body.getAnnotation(RequestBody0.shadedClass()));
        assertEquals(requestBody,
                EndpointHandlerMethod.forSpringMvc(webOperation())
                        .handlerMethod()
                        .parameters()[1].getAnnotation(RequestBody0.shadedClass()));
        assertNotNull(requestBody.toString());
        assertFalse(RequestBody0.fromShade(requestBody).required());
    }

    @Test
    void testJaxrs() {
        final HandlerMethod handlerMethod = EndpointHandlerMethod.forJaxrs(webOperation()).handlerMethod();
        assertNotNull(handlerMethod.parameters());
        assertEquals(2, handlerMethod.parameters().length);
        assertEquals(handlerMethod.parameters()[0].type(), RequestContext.class);
        assertEquals(handlerMethod.parameters()[1].type(), Map.class);
    }

    private static WebOperation webOperation() {
        return new WebOperation() {
            @Override
            public String getId() {
                return "fake";
            }

            @Override
            public boolean isBlocking() {
                return false;
            }

            @Override
            public WebOperationRequestPredicate getRequestPredicate() {
                return new WebOperationRequestPredicate("/foo",
                        WebEndpointHttpMethod.GET,
                        Collections.emptyList(),
                        Collections.emptyList());
            }

            @Override
            public OperationType getType() {
                return OperationType.READ;
            }

            @Override
            public Object invoke(InvocationContext context) {
                return context;
            }
        };
    }


}
