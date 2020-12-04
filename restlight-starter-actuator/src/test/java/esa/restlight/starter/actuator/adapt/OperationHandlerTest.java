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
import esa.restlight.server.route.predicate.PatternsPredicate;
import esa.restlight.server.util.Futures;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.endpoint.InvocationContext;
import org.springframework.boot.actuate.endpoint.OperationType;
import org.springframework.boot.actuate.endpoint.web.WebEndpointHttpMethod;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.WebOperation;
import org.springframework.boot.actuate.endpoint.web.WebOperationRequestPredicate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class OperationHandlerTest {

    @Test
    void testEmpty() {
        final WebOperation op = webOperation(c -> null);
        final OperationHandler handler = new OperationHandler(op);
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final CompletableFuture<Object> ret = handler.handle(request, response, null);
        assertNotNull(ret);
        assertNull(ret.join());
    }

    @Test
    void testInvokeWithArgs() {
        final WebOperation op = webOperation(InvocationContext::getArguments);
        final OperationHandler handler = new OperationHandler(op);
        final AsyncRequest get = MockAsyncRequest.aMockRequest()
                .withMethod("GET")
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        Map<String, String> body = new HashMap<>(16);
        body.put("foo", "1");
        body.put("bar", "2");
        final CompletableFuture<Object> ret = handler.handle(get, response, body);
        assertNotNull(ret);
        assertNotNull(ret.join());
        final Map<String, Object> args = (Map<String, Object>) ret.join();
        assertEquals(0, args.size());

        final AsyncRequest post = MockAsyncRequest.aMockRequest()
                .withMethod("POST")
                .build();
        final AsyncResponse response1 = MockAsyncResponse.aMockResponse().build();
        final CompletableFuture<Object> ret1 = handler.handle(post, response1, body);
        assertNotNull(ret1);
        assertNotNull(ret1.join());
        final Map<String, Object> args1 = (Map<String, Object>) ret1.join();
        assertEquals(body, args1);
    }

    @Test
    void testInvokeWithPathVariables() {
        final WebOperation op = webOperation(InvocationContext::getArguments);
        final OperationHandler handler = new OperationHandler(op);
        final AsyncRequest get = MockAsyncRequest.aMockRequest()
                .withMethod("GET")
                .withUri("/a/b")
                .build();

        PatternsPredicate p = new PatternsPredicate(new String[]{"/{foo}/{bar}"});
        p.test(get);
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final CompletableFuture<Object> ret1 = handler.handle(get, response, null);
        assertNotNull(ret1);
        assertNotNull(ret1.join());
        final Map<String, Object> args = (Map<String, Object>) ret1.join();
        assertEquals(2, args.size());
        assertTrue(args.containsKey("foo"));
        assertTrue(args.containsKey("bar"));
        assertEquals("a", args.get("foo"));
        assertEquals("b", args.get("bar"));
    }

    @Test
    void testInvokeWithUrlParams() {
        final WebOperation op = webOperation(InvocationContext::getArguments);
        final OperationHandler handler = new OperationHandler(op);
        final AsyncRequest get = MockAsyncRequest.aMockRequest()
                .withMethod("GET")
                .withParameter("foo", "a")
                .withParameter("bar", "b")
                .build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final CompletableFuture<Object> ret1 = handler.handle(get, response, null);
        assertNotNull(ret1);
        assertNotNull(ret1.join());
        final Map<String, Object> args = (Map<String, Object>) ret1.join();
        assertEquals(2, args.size());
        assertTrue(args.containsKey("foo"));
        assertTrue(args.containsKey("bar"));
        assertEquals("a", args.get("foo"));
        assertEquals("b", args.get("bar"));
    }

    @Test
    void testReturnNull() {
        final WebOperation op = webOperation(c -> new WebEndpointResponse<>());
        final OperationHandler handler = new OperationHandler(op);
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final CompletableFuture<Object> ret1 = handler.handle(request, response, null);
        assertNotNull(ret1);
        assertNull(ret1.join());
    }

    @Test
    void testReturnObjectAndStatus() {
        final WebOperation op = webOperation(c -> new WebEndpointResponse<>("foo", 404));
        final OperationHandler handler = new OperationHandler(op);
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final CompletableFuture<Object> ret1 = handler.handle(request, response, null);
        assertNotNull(ret1);
        assertNotNull(ret1.join());
        assertEquals("foo", ret1.join());
        assertEquals(404, response.status());
    }

    @Test
    void testReturnCompletableFuture() {
        CompletableFuture<Void> cf = Futures.completedFuture();
        final WebOperation op = webOperation(c -> cf);
        final OperationHandler handler = new OperationHandler(op);
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final CompletableFuture<Object> ret1 = handler.handle(request, response, null);
        assertNotNull(ret1);
        assertEquals(cf, ret1);
        assertNull(ret1.join());
    }

    @Test
    void testReturnObject() {
        final Object obj = new Object();
        final WebOperation op = webOperation(c -> obj);
        final OperationHandler handler = new OperationHandler(op);
        final AsyncRequest request = MockAsyncRequest.aMockRequest().build();
        final AsyncResponse response = MockAsyncResponse.aMockResponse().build();
        final CompletableFuture<Object> ret1 = handler.handle(request, response, null);
        assertNotNull(ret1);
        assertEquals(obj, ret1.join());
    }

    private static WebOperation webOperation(Function<InvocationContext, Object> exe) {
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
                return exe.apply(context);
            }
        };
    }

}
