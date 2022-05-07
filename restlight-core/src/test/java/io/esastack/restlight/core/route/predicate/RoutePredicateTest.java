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
package io.esastack.restlight.core.route.predicate;

import io.esastack.commons.net.http.HttpMethod;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.impl.RequestContextImpl;
import io.esastack.restlight.core.context.HttpRequest;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.mock.MockHttpRequest;
import io.esastack.restlight.core.route.Mapping;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class RoutePredicateTest {

    private RoutePredicate predicate;

    @Test
    void testConsumeExpressions() {
        predicate = RoutePredicate.parseFrom(Mapping.get("/test")
                .consumes("text/plain", "application/json"));

        HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "aaa")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/xml")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testConsumeExpressionsWithHeaders() {
        predicate = RoutePredicate.parseFrom(Mapping.get("/test")
                .consumes("text/plain")
                .consumes("application/json")
                .headers("content-type=application/xml,application/pdf"));

        HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "aaa")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/xml")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/html")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testMergeConsumeExpressions() {
        predicate = RoutePredicate.parseFrom(Mapping.get("/")
                .headers("content-type=application/xml,application/pdf")
                .combine(Mapping.get("/test")
                        .consumes("text/plain", "application/json")));

        HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "aaa")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/xml")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/pdf")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testProduceExpressions() {
        predicate = RoutePredicate.parseFrom(Mapping.get("/test")
                .produces("text/plain", "application/json"));

        HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "aaa")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "text/plain")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "application/json")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "application/xml")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testProduceExpressionsWithHeaders() {
        predicate = RoutePredicate.parseFrom(Mapping.get("/test")
                .produces("text/plain", "application/json")
                .headers("accept=application/xml,application/pdf"));

        HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "aaa")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "text/plain")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "application/json")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "application/xml")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "text/html")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testMergeProduceExpressions() {

        predicate = RoutePredicate.parseFrom(Mapping.get("/")
                .headers("accept=application/xml,application/pdf")
                .produces("!text/plain", "!application/json")
                .combine(Mapping.get("/test")
                        .produces("text/plain", "application/json")));

        HttpRequest request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "aaa")
                .build();
        RequestContext context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "text/plain")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "application/json")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertTrue(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "application/xml")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));

        request = MockHttpRequest
                .aMockRequest()
                .withUri("/test")
                .withHeader(HttpHeaderNames.ACCEPT.toString(), "application/pdf")
                .build();
        context = new RequestContextImpl(request, mock(HttpResponse.class));
        assertFalse(predicate.test(context));
    }

    @Test
    void testMayAmbiguous() {
        assertFalse(RoutePredicate
                .parseFrom(Mapping.mapping()
                        .path("/abc"))
                .mayAmbiguousWith(null));
        assertFalse(RoutePredicate
                .parseFrom(Mapping.mapping()
                        .path("/abc"))
                .mayAmbiguousWith(new MethodPredicate(HttpMethod.GET)));

        // path
        assertTrue(RoutePredicate
                .parseFrom(Mapping.mapping()
                        .path("/abc"))
                .mayAmbiguousWith(RoutePredicate
                        .parseFrom(Mapping.mapping()
                                .path("/abc"))));
        assertTrue(RoutePredicate
                .parseFrom(Mapping.mapping()
                        .path("/abc", "/def"))
                .mayAmbiguousWith(RoutePredicate
                        .parseFrom(Mapping.mapping()
                                .path("/abc", "/gh"))));
        assertFalse(RoutePredicate
                .parseFrom(Mapping.mapping()
                        .path("/abc", "/def"))
                .mayAmbiguousWith(RoutePredicate
                        .parseFrom(Mapping.mapping()
                                .path("/ddd", "/gh"))));

        // params
        assertTrue(RoutePredicate
                .parseFrom(Mapping.mapping()
                        .path("/abc")
                        .hasParam("a")
                        .hasParam("b", "1"))
                .mayAmbiguousWith(RoutePredicate
                        .parseFrom(Mapping.mapping("/abc")
                                .hasParam("a")
                                .hasParam("c", "1"))));
        assertFalse(RoutePredicate
                .parseFrom(Mapping.mapping()
                        .path("/abc")
                        .hasParam("a")
                        .hasParam("b", "1"))
                .mayAmbiguousWith(RoutePredicate
                        .parseFrom(Mapping.mapping("/abc")
                                .hasParam("c")
                                .hasParam("c", "1"))));

        // headers
        assertTrue(RoutePredicate
                .parseFrom(Mapping.mapping()
                        .path("/abc")
                        .hasHeader("a")
                        .hasHeader("b", "1"))
                .mayAmbiguousWith(RoutePredicate
                        .parseFrom(Mapping.mapping()
                                .path("/abc")
                                .hasHeader("a")
                                .hasHeader("c", "1"))));
        assertFalse(RoutePredicate
                .parseFrom(Mapping.mapping()
                        .path("/abc")
                        .hasHeader("a")
                        .hasHeader("b", "1"))
                .mayAmbiguousWith(RoutePredicate
                        .parseFrom(Mapping.mapping()
                                .path("/abc")
                                .hasHeader("c")
                                .hasHeader("c", "1"))));

        // consumes
        assertTrue(RoutePredicate
                .parseFrom(Mapping.mapping()
                        .path("/abc")
                        .consumes("a/1", "a/2"))
                .mayAmbiguousWith(RoutePredicate
                        .parseFrom(Mapping.mapping()
                                .path("/abc")
                                .consumes("a/1", "b/2"))));
        assertFalse(RoutePredicate
                .parseFrom(Mapping.mapping()
                        .path("/abc")
                        .consumes("a/1", "a/2"))
                .mayAmbiguousWith(RoutePredicate
                        .parseFrom(Mapping.mapping()
                                .path("/abc")
                                .consumes("b/1", "b/2"))));
        // produces
        assertTrue(RoutePredicate
                .parseFrom(Mapping.mapping()
                        .path("/abc")
                        .produces("a/1", "a/2"))
                .mayAmbiguousWith(RoutePredicate
                        .parseFrom(Mapping.mapping()
                                .path("/abc")
                                .produces("a/1", "b/2"))));
        assertFalse(RoutePredicate
                .parseFrom(Mapping.mapping()
                        .path("/abc")
                        .produces("a/1", "a/2"))
                .mayAmbiguousWith(RoutePredicate
                        .parseFrom(Mapping.mapping()
                                .path("/abc")
                                .produces("b/1", "b/2"))));

        // all
        assertTrue(RoutePredicate
                .parseFrom(Mapping.mapping()
                        .path("/abc")
                        .hasParam("a")
                        .hasParam("b=1")
                        .hasHeader("a")
                        .hasHeader("b=1")
                        .consumes("a/1", "a/2")
                        .produces("a/1", "a/2"))
                .mayAmbiguousWith(RoutePredicate
                        .parseFrom(Mapping.mapping()
                                .path("/abc")
                                .hasParam("a")
                                .hasParam("b=1")
                                .hasHeader("a")
                                .hasHeader("b=1")
                                .consumes("a/1", "a/2")
                                .produces("a/1", "a/2"))));
        assertFalse(RoutePredicate
                .parseFrom(Mapping.mapping()
                        .path("/abc")
                        .hasParam("a")
                        .hasParam("b=1")
                        .hasHeader("a")
                        .hasHeader("b=1")
                        .consumes("a/1", "a/2")
                        .produces("a/1", "a/2"))
                .mayAmbiguousWith(RoutePredicate
                        .parseFrom(Mapping.mapping()
                                .path("/def")
                                .hasParam("a")
                                .hasParam("b=1")
                                .hasHeader("a")
                                .hasHeader("b=1")
                                .consumes("a/1", "a/2")
                                .produces("a/1", "a/2"))));
        assertFalse(RoutePredicate
                .parseFrom(Mapping.mapping()
                        .path("/abc")
                        .hasParam("c")
                        .hasParam("c=1")
                        .hasHeader("a")
                        .hasHeader("b=1")
                        .consumes("a/1", "a/2")
                        .produces("a/1", "a/2"))
                .mayAmbiguousWith(RoutePredicate
                        .parseFrom(Mapping.mapping()
                                .path("/abc")
                                .hasParam("a")
                                .hasParam("b=1")
                                .hasHeader("a")
                                .hasHeader("b=1")
                                .consumes("a/1", "a/2")
                                .produces("a/1", "a/2"))));
        assertFalse(RoutePredicate
                .parseFrom(Mapping.mapping()
                        .path("/abc")
                        .hasParam("a")
                        .hasParam("b=1")
                        .hasHeader("c")
                        .hasHeader("c=1")
                        .consumes("a/1", "a/2")
                        .produces("a/1", "a/2"))
                .mayAmbiguousWith(RoutePredicate
                        .parseFrom(Mapping.mapping()
                                .path("/abc")
                                .hasParam("a")
                                .hasParam("b=1")
                                .hasHeader("a")
                                .hasHeader("b=1")
                                .consumes("a/1", "a/2")
                                .produces("a/1", "a/2"))));
        assertFalse(RoutePredicate
                .parseFrom(Mapping.mapping()
                        .path("/abc")
                        .hasParam("a")
                        .hasParam("b=1")
                        .hasHeader("b")
                        .hasHeader("b=1")
                        .consumes("c/1", "c/2")
                        .produces("a/1", "a/2"))
                .mayAmbiguousWith(RoutePredicate
                        .parseFrom(Mapping.mapping()
                                .path("/abc")
                                .hasParam("a")
                                .hasParam("b=1")
                                .hasHeader("a")
                                .hasHeader("b=1")
                                .consumes("a/1", "a/2")
                                .produces("a/1", "a/2"))));
        assertFalse(RoutePredicate
                .parseFrom(Mapping.mapping()
                        .path("/abc")
                        .hasParam("a")
                        .hasParam("b=1")
                        .hasHeader("a")
                        .hasHeader("a=1")
                        .consumes("a/1", "a/2")
                        .produces("c/1", "c/2"))
                .mayAmbiguousWith(RoutePredicate
                        .parseFrom(Mapping.mapping()
                                .path("/abc")
                                .hasParam("a")
                                .hasParam("b=1")
                                .hasHeader("a")
                                .hasHeader("b=1")
                                .consumes("a/1", "a/2")
                                .produces("a/1", "a/2"))));
    }
}
