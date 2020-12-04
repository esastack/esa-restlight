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

import esa.commons.reflect.AnnotationUtils;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.annotation.ResponseSerializer;
import esa.restlight.core.annotation.Serializer;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.resolver.ReturnValueResolver;
import esa.restlight.core.resolver.ReturnValueResolverFactory;
import esa.restlight.core.serialize.HttpResponseSerializer;
import esa.restlight.core.serialize.Serializers;
import esa.restlight.core.util.FutureUtils;
import esa.restlight.core.util.MediaType;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link ArgumentResolverFactory} for resolving argument that annotated by the ResponseBody,
 * {@link ResponseSerializer}, {@link Serializer}.
 */
public abstract class AbstractSpecifiedFixedResponseBodyReturnValueResolver implements ReturnValueResolverFactory {

    @Override
    public boolean supports(InvocableMethod invocableMethod) {
        if (!supports0(invocableMethod)) {
            return false;
        }
        final Class<? extends HttpResponseSerializer> target = findSpecifiedSerializer(invocableMethod);
        if (target != null && target != HttpResponseSerializer.class) {
            if (target.isInterface() || Modifier.isAbstract(target.getModifiers())) {
                throw new IllegalArgumentException("Could not resolve ResponseBody serializer class. target type " +
                        "is interface or abstract class. target type:" + target.getName());
            }
            return true;
        }
        return false;
    }

    @Override
    public ReturnValueResolver createResolver(InvocableMethod method,
                                              List<? extends HttpResponseSerializer> serializers) {
        final Class<? extends HttpResponseSerializer> target = findSpecifiedSerializer(method);
        //findFor the first matched one
        HttpResponseSerializer serializer = serializers.stream()
                .filter(s -> target.isAssignableFrom(s.getClass()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Could not findFor ResponseBody serializer. " +
                        "target type:" + target.getName()));

        // try to judge the real type of the return value instance in every request and serialize it if
        // the return value type is Object.class(maybe the real type of the return value instance would
        // be String, byte[], ByteBuf, or primitives...)
        final boolean isAnyType =
                Object.class.equals(method.method().getReturnType())
                        || Object.class.equals(FutureUtils.retrieveFirstGenericTypeOfFutureReturnType(method.method()));
        return new Resolver(serializer, isAnyType);
    }

    @Override
    public int getOrder() {
        return 20;
    }

    protected abstract boolean supports0(InvocableMethod invocableMethod);

    private Class<? extends HttpResponseSerializer> findSpecifiedSerializer(InvocableMethod invocableMethod) {
        Class<? extends HttpResponseSerializer> target = null;

        // find @ResponseSerializer from the method and class
        ResponseSerializer responseSerializer;
        if ((responseSerializer = invocableMethod.getMethodAnnotation(ResponseSerializer.class)) != null
                || (responseSerializer = AnnotationUtils.findAnnotation(invocableMethod.beanType(),
                ResponseSerializer.class)) != null) {
            target = responseSerializer.value();
        }

        // find @Serializer from the method and class
        if (target == null) {
            Serializer serializer;
            if ((serializer = invocableMethod.getMethodAnnotation(Serializer.class)) != null
                    || (serializer = AnnotationUtils.findAnnotation(invocableMethod.beanType(),
                    Serializer.class)) != null) {
                target = serializer.value();
            }

        }
        return target;
    }

    private static class Resolver extends AbstractDetectableReturnValueResolver implements ReturnValueResolver {

        private final HttpResponseSerializer serializer;

        private Resolver(HttpResponseSerializer serializer, boolean detect) {
            super(detect);
            this.serializer = serializer;
        }

        @Override
        protected List<MediaType> getMediaTypes(AsyncRequest request) {
            return Collections.emptyList();
        }

        @Override
        protected byte[] resolve0(Object returnValue,
                                  List<MediaType> mediaTypes,
                                  AsyncRequest request,
                                  AsyncResponse response) throws Exception {
            final Object returnValueToWrite = serializer.customResponse(request, response, returnValue);
            return Serializers.serializeBySerializer(serializer, returnValueToWrite, response);
        }
    }

}
