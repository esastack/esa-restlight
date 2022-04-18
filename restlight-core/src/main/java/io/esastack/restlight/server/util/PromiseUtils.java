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
package io.esastack.restlight.server.util;

import io.netty.util.concurrent.Promise;

import java.util.concurrent.CompletionStage;

public final class PromiseUtils {

    public static void setSuccess(Promise<?> promise) {
        setSuccess(promise, false);
    }

    public static void setSuccess(Promise<?> promise, boolean whatever) {
        if (promise == null) {
            return;
        }
        if (!promise.trySuccess(null) && !whatever) {
            LoggerUtils.logger().warn("Unexpected error, failed to set promise to success.");
        }
    }

    public static void setFailure(Promise<?> promise, Throwable throwable) {
        setFailure(promise, throwable, false);
    }

    public static void setFailure(Promise<?> promise, Throwable throwable, boolean whatever) {
        if (promise == null) {
            return;
        }
        if (!promise.tryFailure(throwable) && !whatever) {
            LoggerUtils.logger().warn("Unexpected error, failed to set promise to failure.");
        }
    }

    public static void setSuccess(CompletionStage<?> promise) {
        setSuccess(promise, false);
    }

    public static void setSuccess(CompletionStage<?> promise, boolean whatever) {
        if (promise == null) {
            return;
        }
        if (!promise.toCompletableFuture().complete(null) && !whatever) {
            LoggerUtils.logger().warn("Unexpected error, failed to set promise to success.");
        }
    }

    public static void setFailure(CompletionStage<?> promise, Throwable throwable) {
        if (promise == null) {
            return;
        }
        if (!promise.toCompletableFuture().completeExceptionally(throwable)) {
            LoggerUtils.logger().warn("Unexpected error, failed to set promise to failure.", throwable);
        }
    }

    private PromiseUtils() {
    }

}
