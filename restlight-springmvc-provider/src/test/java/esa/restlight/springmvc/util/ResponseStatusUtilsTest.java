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
package esa.restlight.springmvc.util;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResponseStatusUtilsTest {

    @Test
    void testGetCustomResponseFromMethod() throws NoSuchMethodException {
        final HttpResponseStatus status =
                ResponseStatusUtils.getCustomResponse(Subject.class, Subject.class.getDeclaredMethod("method2"));
        assertNotNull(status);
        assertEquals(HttpResponseStatus.BAD_GATEWAY, status);
        assertEquals("bar", status.reasonPhrase());

        final HttpResponseStatus status1 =
                ResponseStatusUtils.getCustomResponse(Subject.class, Subject.class.getDeclaredMethod("method1"));
        assertNotNull(status1);
        assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, status1);
        assertEquals("foo", status1.reasonPhrase());

        final HttpResponseStatus status2 =
                ResponseStatusUtils.getCustomResponse(ResponseStatusUtilsTest.class,
                        ResponseStatusUtilsTest.class.getDeclaredMethod(
                        "testGetCustomResponseFromMethod"));
        assertNull(status2);
    }

    @Test
    void testGetCustomResponseFromThrowable() {
        final HttpResponseStatus status =
                ResponseStatusUtils.getCustomResponse(new Error());
        assertNotNull(status);
        assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, status);
        assertEquals("foo", status.reasonPhrase());

        final HttpResponseStatus status1 =
                ResponseStatusUtils.getCustomResponse(new Exception());
        assertNull(status1);
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "foo")
    static class Subject {

        void method1() {
        }

        @ResponseStatus(value = HttpStatus.BAD_GATEWAY, reason = "bar")
        void method2() {
        }
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "foo")
    static class Error extends Throwable {

        private static final long serialVersionUID = 3429313721015902895L;
    }


}
