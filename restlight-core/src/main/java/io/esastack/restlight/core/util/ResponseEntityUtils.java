/*
 * Copyright 2021 OPPO ESA Stack Project
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
package io.esastack.restlight.core.util;

import esa.commons.Primitives;
import esa.commons.collection.AttributeKey;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.server.context.RequestContext;

public final class ResponseEntityUtils {

    private static final AttributeKey<HandlerMethod> HANDLED_METHOD = AttributeKey.valueOf("$internal.handled.method");

    public static void setHandledMethod(RequestContext context, HandlerMethod method) {
        context.attr(HANDLED_METHOD).set(method);
    }

    public static HandlerMethod getHandledMethod(RequestContext context) {
        return context.attr(HANDLED_METHOD).get();
    }

    public static boolean isAssignableFrom(ResponseEntity entity, Class<?> target) {
        if (target.isAssignableFrom(entity.type())) {
            return true;
        }
        // find return type from generic type of Future(CompletableFuture<T>, ListenableFuture<T>, Netty#Future<T>)
        Class<?> type = FutureUtils.retrieveFirstGenericTypeOfFutureReturnType(entity.type(), entity.genericType());
        if (type != null) {
            return target.isAssignableFrom(type);
        }
        return false;
    }

    public static boolean isPrimitiveOrWrapperType(ResponseEntity entity) {
        if (Primitives.isPrimitiveOrWraperType(entity.type())) {
            return true;
        }
        // find return type from generic type of Future(CompletableFuture<T>, ListenableFuture<T>, Netty#Future<T>)
        Class<?> type = FutureUtils.retrieveFirstGenericTypeOfFutureReturnType(entity.type(), entity.genericType());
        if (type != null) {
            return Primitives.isPrimitiveOrWraperType(type);
        }
        return false;
    }

    private ResponseEntityUtils() {
    }

}

