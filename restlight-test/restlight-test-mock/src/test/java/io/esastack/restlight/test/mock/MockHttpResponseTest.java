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
package io.esastack.restlight.test.mock;

import io.esastack.commons.net.netty.http.CookieImpl;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockHttpResponseTest {

    @Test
    void testOperateStatus() {
        final MockHttpResponse response = MockHttpResponse.aMockResponse()
                .build();
        response.status(302);
        assertEquals(302, response.status());
    }

    @Test
    void testOperateEntity() {
        final MockHttpResponse response = MockHttpResponse.aMockResponse()
                .build();
        Object entity = new Object();
        response.entity(entity);
        assertSame(entity, response.entity());
    }

    @Test
    void testOperateHeader() {
        final MockHttpResponse response = MockHttpResponse.aMockResponse()
                .build();
        assertNotNull(response.headers());
    }

    @Test
    void testOperateCookie() {
        final MockHttpResponse response = MockHttpResponse.aMockResponse().build();
        response.addCookie(new CookieImpl("c1", "v1"));
        response.addCookie("c2", "v2");

        final Collection<String> cookies = response.headers().getAll(HttpHeaderNames.SET_COOKIE);
        assertEquals(2, cookies.size());
    }

    @Test
    void testCallEndListener() {
        final AtomicBoolean end0 = new AtomicBoolean();
        final AtomicBoolean end1 = new AtomicBoolean();

        final MockHttpResponse response = MockHttpResponse.aMockResponse()
                .withEndListener(rsp -> end0.set(true))
                .withEndListeners(Collections.singletonList(rsp -> end1.set(true)))
                .build();
        response.callEndListener();
        assertTrue(end0.get());
        assertTrue(end1.get());
    }
}
