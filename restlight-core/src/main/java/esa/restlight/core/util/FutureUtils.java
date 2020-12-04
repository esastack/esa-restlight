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
package esa.restlight.core.util;

import com.google.common.util.concurrent.ListenableFuture;
import esa.commons.ClassUtils;
import esa.commons.concurrent.DirectExecutor;
import esa.restlight.server.util.LoggerUtils;
import io.netty.util.concurrent.Future;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.google.common.util.concurrent.Futures.getDone;

public final class FutureUtils {

    private static final boolean HAS_GUAVA_FUTURE;

    static {
        final String guavaFutureName = "com.google.common.util.concurrent.ListenableFuture";
        HAS_GUAVA_FUTURE = ClassUtils.hasClass(guavaFutureName);
        if (!HAS_GUAVA_FUTURE) {
            LoggerUtils.logger().debug("No Guava ListenableFuture detected.");
        }
    }

    public static boolean hasGuavaFuture() {
        return HAS_GUAVA_FUTURE;
    }

    public static Object getFutureResult(Object obj) {
        if (obj == null) {
            return null;
        }
        Class<?> type = obj.getClass();
        try {
            if (CompletableFuture.class.isAssignableFrom(type)) {
                return ((CompletableFuture) obj).get();
            } else if (hasGuavaFuture() && ListenableFuture.class.isAssignableFrom(type)) {
                return ((ListenableFuture) obj).get();
            } else if (Future.class.isAssignableFrom(type)) {
                return ((Future) obj).get();
            } else {
                return obj;
            }
        } catch (InterruptedException | ExecutionException ignored) {
            return obj;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> CompletableFuture<T> transferNettyFuture(Future<T> future) {
        final CompletableFuture<T> transfer = new CompletableFuture<>();
        future.addListener(f -> {
            if (f.isSuccess()) {
                transfer.complete((T) f.getNow());
            } else {
                transfer.completeExceptionally(f.cause());
            }
        });
        return transfer;
    }

    public static <T> CompletableFuture<T> transferListenableFuture(ListenableFuture<T> future) {
        final CompletableFuture<T> transfer = new CompletableFuture<>();
        future.addListener(() -> {
            try {
                transfer.complete(getDone(future));
            } catch (Throwable t) {
                // unwrap ExecutionException which wrapped by ListenableFuture
                if (t instanceof ExecutionException) {
                    transfer.completeExceptionally(t.getCause());
                } else {
                    transfer.completeExceptionally(t);
                }
            }
        }, DirectExecutor.INSTANCE);
        return transfer;
    }

    public static Class<?> retrieveFirstGenericTypeOfFutureReturnType(Method method) {
        Class<?> type = method.getReturnType();
        if (CompletableFuture.class.isAssignableFrom(type)
                || (hasGuavaFuture() && ListenableFuture.class.isAssignableFrom(type))
                || Future.class.isAssignableFrom(type)) {
            return ClassUtils.retrieveFirstGenericType(method.getGenericReturnType()).orElse(Object.class);
        }
        return null;
    }

    private FutureUtils() {
    }

}
