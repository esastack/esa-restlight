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
package io.esastack.restlight.core.resolver.rspentity;

import esa.commons.collection.AttributeKey;
import esa.commons.reflect.AnnotationUtils;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.annotation.ResponseSerializer;
import io.esastack.restlight.core.annotation.Serializer;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.HandledValue;
import io.esastack.restlight.core.resolver.ResponseEntity;
import io.esastack.restlight.core.resolver.ResponseEntityChannel;
import io.esastack.restlight.core.serialize.HttpResponseSerializer;
import io.esastack.restlight.core.serialize.Serializers;
import io.esastack.restlight.core.util.FutureUtils;
import io.esastack.restlight.server.context.RequestContext;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class FixedResponseEntityResolver extends AbstractResponseEntityResolver {

    private static final AttributeKey<HttpResponseSerializer> MATCHED_SERIALIZER = AttributeKey
            .valueOf("$matched_response_serializer");

    private final ConcurrentHashMap<Method, Optional<Class<? extends HttpResponseSerializer>>> cachedSerializers =
            new ConcurrentHashMap<>(64);

    private final List<? extends HttpResponseSerializer> serializers;

    protected FixedResponseEntityResolver(List<? extends HttpResponseSerializer> serializers) {
        super(true);
        this.serializers = serializers;
    }

    @Override
    public HandledValue<Void> writeTo(ResponseEntity entity,
                                      ResponseEntityChannel channel,
                                      RequestContext context) throws Exception {
        if (!supports(entity) || !entity.handler().isPresent()) {
            return HandledValue.failed();
        }
        Class<? extends HttpResponseSerializer> target = getResponseSerializer(entity.handler().get());
        if (target != null && target != HttpResponseSerializer.class) {
            if (target.isInterface() || Modifier.isAbstract(target.getModifiers())) {
                throw new IllegalArgumentException("Could not resolve ResponseBody serializer class. target type " +
                        "is interface or abstract class. target type:" + target.getName());
            }
            //findFor the first matched one
            HttpResponseSerializer serializer = serializers.stream()
                    .filter(s -> target.isAssignableFrom(s.getClass()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Could not findFor ResponseBody serializer. " +
                            "target type:" + target.getName()));
            context.attrs().attr(MATCHED_SERIALIZER).set(serializer);
            return super.writeTo(entity, channel, context);
        } else {
            return HandledValue.failed();
        }
    }

    @Override
    protected byte[] serialize(ResponseEntity entity,
                               List<MediaType> mediaTypes,
                               RequestContext context) throws Exception {
        final HttpResponseSerializer serializer = context.attrs().attr(MATCHED_SERIALIZER).getAndRemove();
        HandledValue<byte[]> value = Serializers.serializeBySerializer(serializer, entity);
        if (value.isSuccess()) {
            return value.value();
        } else {
            throw new IllegalStateException("Could not resolve the return value(type=" + entity.type().getName()
                    + ") by specified HttpResponseSerializer: " + serializer.getClass().getName());
        }
    }

    @Override
    protected boolean isSimpleType(ResponseEntity entity) {
        HandlerMethod method = entity.handler().orElse(null);
        if (method == null) {
            return false;
        }
        return Object.class.equals(method.method().getReturnType())
                || Object.class.equals(FutureUtils.retrieveFirstGenericTypeOfFutureReturnType(method.method()));
    }

    @Override
    protected List<MediaType> getMediaTypes(RequestContext context) {
        return Collections.emptyList();
    }

    private Class<? extends HttpResponseSerializer> getResponseSerializer(HandlerMethod method) {
        return cachedSerializers.computeIfAbsent(method.method(), (m) -> findResponseSerializer(method))
                .orElse(null);
    }

    private Optional<Class<? extends HttpResponseSerializer>> findResponseSerializer(HandlerMethod method) {
        Class<? extends HttpResponseSerializer> target = null;

        // find @ResponseSerializer from the method and class
        ResponseSerializer responseSerializer;
        if ((responseSerializer = method.getMethodAnnotation(ResponseSerializer.class)) != null
                || (responseSerializer = AnnotationUtils.findAnnotation(method.beanType(),
                ResponseSerializer.class)) != null) {
            target = responseSerializer.value();
        }

        // find @Serializer from the method and class
        if (target == null) {
            Serializer serializer;
            if ((serializer = method.getMethodAnnotation(Serializer.class)) != null
                    || (serializer = AnnotationUtils.findAnnotation(method.beanType(),
                    Serializer.class)) != null) {
                target = serializer.value();
            }

        }
        return Optional.ofNullable(target);
    }

}

