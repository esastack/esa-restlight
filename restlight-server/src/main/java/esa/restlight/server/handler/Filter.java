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
package esa.restlight.server.handler;

import esa.commons.spi.SPI;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.util.Ordered;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.CompletableFuture;

@SPI
@FunctionalInterface
public interface Filter extends Ordered {

    /**
     * listener on connection connected.
     * <p>
     * note: please be careful to use the ctx argument, otherwise it is possible to affect to the downstream operations.
     * and we will not catch exception thrown by this method!
     *
     * @param ctx context
     *
     * @return {@code true} this connection should be executed next.
     */
    default boolean onConnected(ChannelHandlerContext ctx) {
        return true;
    }

    /**
     * Note: we do not allowed any exception or error here. if a exception or error is threw in this function we will
     * ignore it to protect the process of current request.
     * <p>
     * IMPORTANT: never block current thread please, cause that will effect the performance.
     *
     * @param request  request
     * @param response response
     * @param chain    filter chain
     * @return future
     */
    CompletableFuture<Void> doFilter(AsyncRequest request, AsyncResponse response, FilterChain chain);

    /**
     * Shutdown event
     */
    default void shutdown() {
    }

    /**
     * Default to lowest order.
     *
     * @return order
     */
    @Override
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
