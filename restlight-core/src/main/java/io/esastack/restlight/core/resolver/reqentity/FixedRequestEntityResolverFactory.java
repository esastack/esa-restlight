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
package io.esastack.restlight.core.resolver.reqentity;

import esa.commons.function.Function3;
import esa.commons.reflect.AnnotationUtils;
import io.esastack.restlight.core.annotation.RequestSerializer;
import io.esastack.restlight.core.annotation.Serializer;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandledValue;
import io.esastack.restlight.core.resolver.RequestEntity;
import io.esastack.restlight.core.resolver.RequestEntityResolver;
import io.esastack.restlight.core.resolver.RequestEntityResolverFactory;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.server.context.RequestContext;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

import static io.esastack.restlight.core.resolver.reqentity.FlexibleRequestEntityResolverFactory.checkRequired;

/**
 * Implementation of {@link RequestEntityResolverFactory} for resolving argument that annotated by the
 * {@code RequestBody} and {@link RequestSerializer}, {@link Serializer}
 */
public abstract class FixedRequestEntityResolverFactory implements RequestEntityResolverFactory {

    @Override
    public boolean supports(Param param) {
        if (!supports0(param)) {
            return false;
        }
        final Class<? extends HttpRequestSerializer> target = findRequestSerializer(param);
        if (target != null && target != HttpRequestSerializer.class) {
            if (target.isInterface() || Modifier.isAbstract(target.getModifiers())) {
                throw new IllegalArgumentException("Could not resolve RequestBody serializer class. target type " +
                        "is interface or abstract class. target type:" + target.getName());
            }
            return true;
        }
        return false;
    }

    @Override
    public RequestEntityResolver createResolver(Param param,
                                                Function3<Class<?>, Type, Param, StringConverter> converterFunc,
                                                List<? extends HttpRequestSerializer> serializers) {
        final Class<? extends HttpRequestSerializer> target = findRequestSerializer(param);
        //findFor the first matched one
        HttpRequestSerializer serializer = serializers.stream()
                .filter(s -> target.isAssignableFrom(s.getClass()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Could not findFor RequestBody serializer. " +
                        "target type:" + target.getName()));

        StringConverter converter = converterFunc.apply(param.type(), param.genericType(), param);
        if (converter == null) {
            converter = value -> value;
        }
        return new Resolver(converter, serializer, param);
    }

    /**
     * Creates {@link NameAndValue} by given {@link Param}.
     *
     * @param param     param
     * @return          NameAndValue
     */
    protected abstract NameAndValue<String> createNameAndValue(Param param);

    protected boolean supports0(Param param) {
        // current parameter only
        return param.isMethodParam() && param.methodParam().method().getParameterCount() == 1;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private Class<? extends HttpRequestSerializer> findRequestSerializer(Param param) {
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

    private class Resolver implements RequestEntityResolver {

        private final NameAndValue<String> nav;
        private final StringConverter converter;
        private final HttpRequestSerializer serializer;

        private Resolver(StringConverter converter,
                         HttpRequestSerializer serializer,
                         Param param) {
            this.nav = createNameAndValue(param);
            this.converter = converter;
            this.serializer = serializer;
        }

        @Override
        public HandledValue<Object> readFrom(Param param, RequestEntity entity,
                                             RequestContext context) throws Exception {
            return checkRequired(nav, converter, serializer.deserialize(entity));
        }
    }
}

