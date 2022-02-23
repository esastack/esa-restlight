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
package io.esastack.restlight.jaxrs.configure;

import jakarta.ws.rs.container.ContainerRequestFilter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtensionHandlerProxyTest {

    @Test
    void testNewProxy() throws Throwable {
        final AtomicInteger count = new AtomicInteger();
        ContainerRequestFilter proxied = (ContainerRequestFilter) ExtensionHandlerProxy
                .newProxy(ContainerRequestFilter.class, (proxy, method, args) -> {
                    count.incrementAndGet();
                    return count;
                });

        proxied.filter(null);
        assertEquals(1, count.intValue());
    }

}

