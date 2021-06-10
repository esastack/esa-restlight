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
package esa.restlight.starter.actuator.adapt;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.method.MethodParam;
import esa.restlight.springmvc.annotation.shaded.RequestBody0;
import esa.restlight.springmvc.annotation.shaded.ResponseBody0;
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
        final HandlerMethod handlerMethod = EndpointHandlerMethod.forSpringMvc(webOperation());
        assertTrue(handlerMethod.hasMethodAnnotation(ResponseBody0.shadedClass()));
        final Annotation responseBody = handlerMethod.getMethodAnnotation(ResponseBody0.shadedClass());
        assertNotNull(responseBody);
        assertEquals(ResponseBody0.shadedClass(), responseBody.annotationType());
        assertNotEquals(0, responseBody.hashCode());
        assertNotNull(responseBody.toString());
        assertEquals(responseBody, handlerMethod.getMethodAnnotation(ResponseBody0.shadedClass()));
        assertEquals(responseBody,
                EndpointHandlerMethod.forSpringMvc(webOperation()).getMethodAnnotation(ResponseBody0.shadedClass()));
        assertNotNull(responseBody.toString());


        assertNotNull(handlerMethod.parameters());
        assertEquals(3, handlerMethod.parameters().length);
        assertEquals(handlerMethod.parameters()[0].type(), AsyncRequest.class);
        assertEquals(handlerMethod.parameters()[1].type(), AsyncResponse.class);
        assertEquals(handlerMethod.parameters()[2].type(), Map.class);
        final MethodParam body = handlerMethod.parameters()[2];

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
                        .parameters()[2].getAnnotation(RequestBody0.shadedClass()));
        assertNotNull(requestBody.toString());
        assertFalse(RequestBody0.fromShade(requestBody).required());
    }

    @Test
    void testJaxrs() {
        final HandlerMethod handlerMethod = EndpointHandlerMethod.forJaxrs(webOperation());
        assertNotNull(handlerMethod.parameters());
        assertEquals(3, handlerMethod.parameters().length);
        assertEquals(handlerMethod.parameters()[0].type(), AsyncRequest.class);
        assertEquals(handlerMethod.parameters()[1].type(), AsyncResponse.class);
        assertEquals(handlerMethod.parameters()[2].type(), Map.class);
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
