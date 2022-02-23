/*
 * Copyright 2022 OPPO ESA Stack Project
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
package io.esastack.restlight.jaxrs.adapter;

import esa.commons.collection.AttributeMap;
import esa.commons.collection.Attributes;
import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.server.bootstrap.WebServerException;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.impl.RequestContextImpl;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.route.RouteFailureException;
import io.esastack.restlight.server.route.predicate.RoutePredicate;
import io.esastack.restlight.server.util.Futures;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class JaxrsExceptionAdapterTest {

    @Test
    void testBasic() {
        final JaxrsExceptionAdapter adapter = new JaxrsExceptionAdapter();

        // getOrder()
        assertEquals(Ordered.HIGHEST_PRECEDENCE, adapter.getOrder());

        // handle()
        final AtomicReference<Throwable> exHolder = new AtomicReference<>();
        adapter.handle(mock(RequestContext.class), null, (context, th) -> {
            exHolder.set(th);
            return Futures.completedFuture();
        });
        assertNull(exHolder.get());

        // toJakartaException()
        final Attributes attrs = new AttributeMap();
        final RequestContext context = new RequestContextImpl(attrs,
                mock(HttpRequest.class), mock(HttpResponse.class));

        final WebApplicationException ex1 = new WebApplicationException();
        assertSame(ex1, adapter.toJakartaException(context, ex1));

        final WebServerException ex2 = WebServerException.badRequest(ex1);
        assertTrue(adapter.toJakartaException(context, ex2) instanceof BadRequestException);
        assertSame(ex1, adapter.toJakartaException(context, ex2).getCause());

        final WebServerException ex3 = WebServerException.notAcceptable("ABC");
        assertSame(ex3, adapter.toJakartaException(context, ex3).getCause());
        assertEquals("ABC", adapter.toJakartaException(context, ex3).getMessage());

        final WebServerException ex4 = WebServerException.notSupported("DEF");
        assertSame(ex4, adapter.toJakartaException(context, ex4).getCause());
        assertEquals("DEF", adapter.toJakartaException(context, ex4).getMessage());

        final WebServerException ex5 = new WebServerException(HttpStatus.NOT_FOUND, "XYZ", ex1);
        context.attrs().attr(RoutePredicate.MISMATCH_ERR).set(RouteFailureException.RouteFailure.METHOD_MISMATCH);
        assertTrue(adapter.toJakartaException(context, ex5) instanceof NotAllowedException);
        assertNull(adapter.toJakartaException(context, ex5).getCause());
        assertEquals("HTTP 405 Method Not Allowed", adapter.toJakartaException(context, ex5).getMessage());

        context.attrs().attr(RoutePredicate.MISMATCH_ERR).set(RouteFailureException.RouteFailure.CONSUMES_MISMATCH);
        assertTrue(adapter.toJakartaException(context, ex5) instanceof NotSupportedException);
        assertSame(ex1, adapter.toJakartaException(context, ex5).getCause());
        assertEquals("XYZ", adapter.toJakartaException(context, ex5).getMessage());

        context.attrs().attr(RoutePredicate.MISMATCH_ERR).set(RouteFailureException.RouteFailure.PRODUCES_MISMATCH);
        assertTrue(adapter.toJakartaException(context, ex5) instanceof NotAcceptableException);
        assertSame(ex1, adapter.toJakartaException(context, ex5).getCause());
        assertEquals("XYZ", adapter.toJakartaException(context, ex5).getMessage());

        context.attrs().attr(RoutePredicate.MISMATCH_ERR).set(RouteFailureException.RouteFailure.PATTERN_MISMATCH);
        assertTrue(adapter.toJakartaException(context, ex5) instanceof BadRequestException);
        assertSame(ex1, adapter.toJakartaException(context, ex5).getCause());
        assertEquals("XYZ", adapter.toJakartaException(context, ex5).getMessage());

        context.attrs().attr(RoutePredicate.MISMATCH_ERR).remove();
        assertTrue(adapter.toJakartaException(context, ex5) instanceof NotFoundException);
        assertSame(ex1, adapter.toJakartaException(context, ex5).getCause());
        assertEquals("XYZ", adapter.toJakartaException(context, ex5).getMessage());

        final WebServerException ex6 = new WebServerException(HttpStatus.INTERNAL_SERVER_ERROR, ex1);
        assertTrue(adapter.toJakartaException(context, ex6) instanceof InternalServerErrorException);
        assertSame(ex1, adapter.toJakartaException(context, ex6).getCause());

        final RuntimeException ex7 = new RuntimeException();
        assertTrue(adapter.toJakartaException(context, ex7) instanceof InternalServerErrorException);
        assertSame(ex7, adapter.toJakartaException(context, ex7).getCause());
    }

}

