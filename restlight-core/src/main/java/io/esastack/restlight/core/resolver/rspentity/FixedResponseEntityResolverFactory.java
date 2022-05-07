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
package io.esastack.restlight.core.resolver.rspentity;

import io.esastack.restlight.core.annotation.ResponseSerializer;
import io.esastack.restlight.core.annotation.Serializer;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;

import java.lang.reflect.Modifier;
import java.util.List;

public abstract class FixedResponseEntityResolverFactory implements ResponseEntityResolverFactory {

    @Override
    public ResponseEntityResolver createResolver(HandlerMethod method,
                                                 List<? extends HttpResponseSerializer> serializers) {
        final Class<? extends HttpResponseSerializer> target = findResponseSerializer(method);
        //findFor the first matched one
        HttpResponseSerializer serializer = serializers.stream()
                .filter(s -> target.isAssignableFrom(s.getClass()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Could not findFor ResponseBody serializer. " +
                        "target type:" + target.getName()));
        return new FixedResponseEntityResolver(method, serializer);
    }

    @Override
    public boolean supports(HandlerMethod method) {
        if (!supports0(method)) {
            return false;
        }
        final Class<? extends HttpResponseSerializer> target = findResponseSerializer(method);
        if (target != null && target != HttpResponseSerializer.class) {
            if (target.isInterface() || Modifier.isAbstract(target.getModifiers())) {
                throw new IllegalArgumentException("Could not resolve ResponseBody serializer class. target type " +
                        "is interface or abstract class. target type:" + target.getName());
            }
            return true;
        }
        return false;
    }

    protected abstract boolean supports0(HandlerMethod method);

    private Class<? extends HttpResponseSerializer> findResponseSerializer(HandlerMethod method) {
        Class<? extends HttpResponseSerializer> target = null;

        // find @ResponseSerializer from the method and class
        ResponseSerializer responseSerializer;
        if ((responseSerializer = method.getMethodAnnotation(ResponseSerializer.class, true)) != null
                || (responseSerializer = method.getClassAnnotation(ResponseSerializer.class, true)) != null) {
            target = responseSerializer.value();
        }

        // find @Serializer from the method and class
        if (target == null) {
            Serializer serializer;
            if ((serializer = method.getMethodAnnotation(Serializer.class, true)) != null
                    || (serializer = method.getClassAnnotation(Serializer.class, true)) != null) {
                target = serializer.value();
            }

        }
        return target;
    }
}

