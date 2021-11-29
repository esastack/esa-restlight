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
import esa.commons.StringUtils;
import io.esastack.httpserver.core.HttpResponse;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.serialize.Serializers;
import io.esastack.restlight.server.util.LoggerUtils;

public final class ResponseEntityUtils {

    private static final String HANDLED_METHOD = "$internal.handled.method";

    public static void setHandledMethod(RequestContext context, HandlerMethod method) {
        context.setAttribute(HANDLED_METHOD, method);
    }

    public static HandlerMethod getHandledMethod(RequestContext context) {
        return context.getUncheckedAttribute(HANDLED_METHOD);
    }

    public static void writeTo(ResponseEntity entity, byte[] data, HttpResponse response) {
        if (!response.isCommitted()) {
            if (!Serializers.alreadyWrite(data)) {
                if (response.isCommitted()) {
                    LoggerUtils.logger().warn(StringUtils.concat("Ignore the non-null return value '{}'," +
                            " because response is " + "not writable.", entity.handler().isPresent()
                            ? entity.handler().get().toString() : ""), entity.entity());
                }
                response.sendResult(data);
            }
        }
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

