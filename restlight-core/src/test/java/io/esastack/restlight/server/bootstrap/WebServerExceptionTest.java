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
package io.esastack.restlight.server.bootstrap;

import io.esastack.commons.net.http.HttpStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebServerExceptionTest {

    @Test
    void testConstructor() {
        WebServerException ex = new WebServerException(HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.status());
        assertNull(ex.getMessage());
        assertNull(ex.getCause());

        ex = new WebServerException("foo");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.status());
        assertEquals("foo", ex.getMessage());
        assertNull(ex.getCause());

        Throwable t = new Error();
        ex = new WebServerException("foo", t);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.status());
        assertEquals("foo", ex.getMessage());
        assertSame(t, ex.getCause());

        ex = new WebServerException(t);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.status());
        assertEquals(t.toString(), ex.getMessage());
        assertSame(t, ex.getCause());

        ex = new WebServerException(HttpStatus.NOT_FOUND);
        assertEquals(HttpStatus.NOT_FOUND, ex.status());
        assertNull(ex.getMessage());
        assertNull(ex.getCause());

        ex = new WebServerException(HttpStatus.NOT_FOUND, "foo");
        assertEquals(HttpStatus.NOT_FOUND, ex.status());
        assertEquals("foo", ex.getMessage());
        assertNull(ex.getCause());

        ex = new WebServerException(HttpStatus.NOT_FOUND, "foo", t);
        assertEquals(HttpStatus.NOT_FOUND, ex.status());
        assertEquals("foo", ex.getMessage());
        assertSame(t, ex.getCause());

        ex = new WebServerException(HttpStatus.NOT_FOUND, t);
        assertEquals(HttpStatus.NOT_FOUND, ex.status());
        assertEquals(t.toString(), ex.getMessage());
        assertSame(t, ex.getCause());
    }

    @Test
    void testWrap() {
        Throwable t = new Error();
        final WebServerException wrap = WebServerException.wrap(t);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, wrap.status());
        assertEquals(t.toString(), wrap.getMessage());
        assertSame(t, wrap.getCause());

        t = WebServerException.badRequest("bad");
        assertSame(t, WebServerException.wrap(t));
    }

    @Test
    void testBadRequest() {
        WebServerException ex = WebServerException.badRequest("bad");

        assertEquals(HttpStatus.BAD_REQUEST, ex.status());
        assertEquals("bad", ex.getMessage());
        assertNull(ex.getCause());

        ex = WebServerException.badRequest("foo");
        assertEquals(HttpStatus.BAD_REQUEST, ex.status());
        assertEquals("foo", ex.getMessage());
        assertNull(ex.getCause());

        final Throwable t = new Error();
        ex = WebServerException.badRequest(t);
        assertEquals(HttpStatus.BAD_REQUEST, ex.status());
        assertEquals(t.toString(), ex.getMessage());
        assertSame(t, ex.getCause());

        ex = WebServerException.badRequest("foo", t);
        assertEquals(HttpStatus.BAD_REQUEST, ex.status());
        assertEquals("foo", ex.getMessage());
        assertSame(t, ex.getCause());
    }

}
