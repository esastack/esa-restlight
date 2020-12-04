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
package esa.restlight.core.resolver.result;

import esa.commons.Primitives;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.resolver.ReturnValueResolver;
import esa.restlight.core.resolver.ReturnValueResolverFactory;
import esa.restlight.core.serialize.HttpResponseSerializer;
import esa.restlight.core.serialize.Serializers;
import esa.restlight.core.util.FutureUtils;
import esa.restlight.core.util.MediaType;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Method;
import java.util.List;


/**
 * Implementation of {@link ReturnValueResolverFactory} for resolving simple return value type: {@link CharSequence},
 * byte array, {@link ByteBuf} and Primitives.
 */
public class SimpleReturnValueResolver implements ReturnValueResolverFactory {

    private static boolean isSimpleType(Class<?> clazz) {
        return CharSequence.class.isAssignableFrom(clazz) || byte[].class.isAssignableFrom(clazz)
                || ByteBuf.class.isAssignableFrom(clazz) || Primitives.isPrimitiveOrWraperType(clazz);
    }

    @Override
    public final boolean supports(InvocableMethod invocableMethod) {
        return findSimpleReturnType(invocableMethod.method()) != null;
    }

    @Override
    public ReturnValueResolver createResolver(InvocableMethod method,
                                              List<? extends HttpResponseSerializer> serializers) {

        final Class<?> returnType = findSimpleReturnType(method.method());
        assert returnType != null;

        if (CharSequence.class.isAssignableFrom(returnType)) {
            return new CharSequenceResolver();
        }
        if (byte[].class.isAssignableFrom(returnType)) {
            return new ByteArrayResolver();
        }
        if (ByteBuf.class.isAssignableFrom(returnType)) {
            return new ByteBufResolver();
        }
        if (returnType.isPrimitive() || Primitives.isWrapperType(returnType)) {
            return new PrimitiveResolver();
        }
        throw new IllegalStateException("Could not resolve the return value type: " +
                returnType.getName() + ", is the '@ResponseBody' missing?");
    }

    private static Class<?> findSimpleReturnType(Method method) {
        if (isSimpleType(method.getReturnType())) {
            return method.getReturnType();
        }
        // find return type from generic type of Future(CompletableFuture<T>, ListenableFuture<T>, Netty#Future<T>)
        Class<?> type = FutureUtils.retrieveFirstGenericTypeOfFutureReturnType(method);
        if (type != null) {
            if (isSimpleType(type)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public int getOrder() {
        return 100;
    }

    /**
     * Implementation for resolving return value type of {@link CharSequence}
     */
    private static class CharSequenceResolver extends AbstractDetectableReturnValueResolver {


        CharSequenceResolver() {
            super(false);
        }

        @Override
        protected byte[] resolve0(Object returnValue,
                                  List<MediaType> mediaTypes,
                                  AsyncRequest request,
                                  AsyncResponse response) {
            return Serializers.serializeCharSequence(((CharSequence) returnValue),
                    response,
                    getMediaType(mediaTypes));
        }
    }

    /**
     * Implementation for resolving return value type of byte array
     */
    private static class ByteArrayResolver extends AbstractDetectableReturnValueResolver {

        ByteArrayResolver() {
            super(false);
        }

        @Override
        protected byte[] resolve0(Object returnValue,
                                  List<MediaType> mediaTypes,
                                  AsyncRequest request,
                                  AsyncResponse response) {
            return Serializers.serializeByteArray((byte[]) returnValue,
                    response,
                    getMediaType(mediaTypes));
        }
    }

    /**
     * Implementation for resolving return value type of {@link ByteBuf}
     */
    private static class ByteBufResolver extends AbstractDetectableReturnValueResolver {

        ByteBufResolver() {
            super(false);
        }

        @Override
        protected byte[] resolve0(Object returnValue,
                                  List<MediaType> mediaTypes,
                                  AsyncRequest request,
                                  AsyncResponse response) {
            return Serializers.serializeByteBuf((ByteBuf) returnValue,
                    response,
                    getMediaType(mediaTypes));
        }
    }

    /**
     * Implementation for resolving return value type of Primitives
     */
    private static class PrimitiveResolver extends AbstractDetectableReturnValueResolver {

        PrimitiveResolver() {
            super(false);
        }

        @Override
        protected byte[] resolve0(Object returnValue,
                                  List<MediaType> mediaTypes,
                                  AsyncRequest request,
                                  AsyncResponse response) {
            return Serializers.serializePrimitives(returnValue,
                    response,
                    getMediaType(mediaTypes));
        }
    }
}
