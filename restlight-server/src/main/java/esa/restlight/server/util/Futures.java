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
package esa.restlight.server.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class Futures {

    private Futures() {

    }

    public static <T> CompletableFuture<T> completedExceptionally(Throwable t) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(t);
        return future;
    }

    public static <T> CompletableFuture<T> completedFuture() {
        return CompletableFuture.completedFuture(null);
    }

    public static <T> CompletableFuture<T> completedFuture(T v) {
        return CompletableFuture.completedFuture(v);
    }

    public static Throwable unwrapCompletionException(Throwable t) {
        if (t instanceof CompletionException || t instanceof ExecutionException) {
            // unwrap exception of CompletableFuture
            return t.getCause();
        }
        return t;
    }
}
