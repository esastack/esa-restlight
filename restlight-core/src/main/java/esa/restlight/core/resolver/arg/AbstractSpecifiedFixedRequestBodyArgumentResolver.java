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
package esa.restlight.core.resolver.arg;

import esa.commons.reflect.AnnotationUtils;
import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.annotation.RequestSerializer;
import esa.restlight.core.annotation.Serializer;
import esa.restlight.core.method.Param;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.serialize.HttpRequestSerializer;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Implementation of {@link ArgumentResolverFactory} for resolving argument that annotated by the RequestBody and
 * {@link RequestSerializer}, {@link Serializer}
 */
public abstract class AbstractSpecifiedFixedRequestBodyArgumentResolver implements ArgumentResolverFactory {

    @Override
    public boolean supports(Param param) {
        if (!supports0(param)) {
            return false;
        }
        final Class<? extends HttpRequestSerializer> target = findSpecifiedSerializer(param);
        if (target != null && target != HttpRequestSerializer.class) {
            if (target.isInterface() || Modifier.isAbstract(target.getModifiers())) {
                throw new IllegalArgumentException("Could not resolve RequestBody serializer class. target type " +
                        "is interface or abstract class. target type:" + target.getName());
            }
            return true;
        }
        return false;
    }

    protected boolean supports0(Param param) {
        // current parameter only
        return param.isMethodParam() && param.methodParam().method().getParameterCount() == 1;
    }

    /**
     * Create an instance of {@link NameAndValue} for the parameter.
     *
     * @param param parameter
     *
     * @return name and value
     */
    protected abstract NameAndValue createNameAndValue(Param param);

    @Override
    public ArgumentResolver createResolver(Param param,
                                           List<? extends HttpRequestSerializer> serializers) {

        final Class<? extends HttpRequestSerializer> target = findSpecifiedSerializer(param);
        //findFor the first matched one
        HttpRequestSerializer serializer = serializers.stream()
                .filter(s -> target.isAssignableFrom(s.getClass()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Could not findFor RequestBody serializer. " +
                        "target type:" + target.getName()));

        return new Resolver(serializer, param);
    }

    private Class<? extends HttpRequestSerializer> findSpecifiedSerializer(Param param) {
        Class<? extends HttpRequestSerializer> target = null;

        // find @RequestSerializer from the param -> method -> and class
        RequestSerializer requestSerializer;
        if ((requestSerializer = param.getAnnotation(RequestSerializer.class)) != null) {
            target = requestSerializer.value();
        } else if ((param.isMethodParam() && (requestSerializer =
                param.methodParam().getMethodAnnotation(RequestSerializer.class)) != null)) {
            target = requestSerializer.value();
        } else if ((requestSerializer = AnnotationUtils.findAnnotation(param.declaringClass(),
                RequestSerializer.class)) != null) {
            target = requestSerializer.value();
        }

        // find @Serializer from the param -> method -> and class
        if (target == null) {
            Serializer serializer;
            if ((serializer = param.getAnnotation(Serializer.class)) != null) {
                target = serializer.value();
            } else if ((param.isMethodParam() && (serializer =
                    param.methodParam().getMethodAnnotation(Serializer.class)) != null)) {
                target = serializer.value();
            } else if ((serializer = AnnotationUtils.findAnnotation(param.declaringClass(),
                    Serializer.class)) != null) {
                target = serializer.value();
            }
        }
        return target;
    }

    class Resolver extends AbstractNameAndValueArgumentResolver {

        private final HttpRequestSerializer serializer;

        Resolver(HttpRequestSerializer serializer, Param param) {
            super(param);
            this.serializer = serializer;
        }

        @Override
        protected NameAndValue createNameAndValue(Param param) {
            return AbstractSpecifiedFixedRequestBodyArgumentResolver.this.createNameAndValue(param);
        }

        @Override
        protected Object resolveName(String name, AsyncRequest request) throws Exception {
            Object resolved;
            if (serializer.preferStream()) {
                resolved = serializer.deSerialize(request.inputStream(), param.genericType());
            } else {
                resolved = serializer.deSerialize(request.body(), param.genericType());
            }
            return resolved;
        }
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
