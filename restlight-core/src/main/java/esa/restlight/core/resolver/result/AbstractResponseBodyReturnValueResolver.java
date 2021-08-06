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

import esa.commons.StringUtils;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.resolver.ReturnValueResolver;
import esa.restlight.core.resolver.ReturnValueResolverFactory;
import esa.restlight.core.serialize.HttpResponseSerializer;
import esa.restlight.core.serialize.ProtoBufHttpBodySerializer;
import esa.restlight.core.serialize.Serializers;
import esa.restlight.core.util.Constants;
import esa.restlight.core.util.FutureUtils;
import esa.restlight.core.util.MediaType;
import esa.restlight.server.bootstrap.WebServerException;
import io.netty.util.internal.InternalThreadLocalMap;

import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link ArgumentResolverFactory} for resolving argument that annotated by the ResponseBody.
 */
public abstract class AbstractResponseBodyReturnValueResolver implements ReturnValueResolverFactory {

    private static final Logger logger = LoggerFactory.getLogger(AbstractResponseBodyReturnValueResolver.class);

    private final boolean negotiation;
    private final String parameterName;

    public AbstractResponseBodyReturnValueResolver(boolean negotiation, String parameterName) {
        this.negotiation = negotiation;
        this.parameterName = StringUtils.nonEmptyOrElse(parameterName,
                Constants.DEFAULT_NEGOTIATION_FORMAT_PARAMETER);
    }

    @Override
    public ReturnValueResolver createResolver(InvocableMethod method,
                                              List<? extends HttpResponseSerializer> serializers) {

        // try to extract the real type of the return value instance in every request and serialize it if
        // the return value type is Object.class(maybe the real type of the return value instance would
        // be String, byte[], ByteBuf, or primitives...)
        final boolean isAnyType =
                Object.class.equals(method.method().getReturnType())
                        || Object.class.equals(FutureUtils.retrieveFirstGenericTypeOfFutureReturnType(method.method()));
        // use negotiation implementation if it is enable
        return negotiation
                ? new NegotiationResolver(serializers, parameterName, isAnyType)
                : new DefaultResolver(serializers, isAnyType);
    }

    @Override
    public int getOrder() {
        return 10;
    }

    static class DefaultResolver extends AbstractDetectableReturnValueResolver implements ReturnValueResolver {

        private final List<? extends HttpResponseSerializer> serializers;

        DefaultResolver(List<? extends HttpResponseSerializer> serializers, boolean detect) {
            super(detect);
            this.serializers = serializers;
        }

        @Override
        protected byte[] resolve0(Object returnValue,
                                  List<MediaType> mediaTypes,
                                  AsyncRequest request,
                                  AsyncResponse response) throws Exception {
            if (serializers.isEmpty()) {
                throw WebServerException.badRequest("Could not find any compatible serializer to handle " +
                        "the return value(type=" + returnValue.getClass().getName() +
                        "), acceptMediaTypes: " + mediaTypes);
            }

            HttpResponseSerializer serializer = null;
            if (mediaTypes.isEmpty()) {
                serializer = serializers.get(0);
            } else {
                outerloop:
                for (MediaType mediaType : mediaTypes) {
                    for (HttpResponseSerializer ser : serializers) {
                        if (ser.supportsWrite(mediaType, returnValue.getClass())) {
                            serializer = ser;
                            break outerloop;
                        }
                    }
                }
                if (serializer == null) {
                    logger.warn("Failed to find serializer for media type '{}', try to use default serializer.",
                            mediaTypes);
                    serializer = serializers.get(0);
                }
            }

            final Object returnValueToWrite = serializer.customResponse(request, response, returnValue);
            return Serializers.serializeBySerializer(serializer, returnValueToWrite, response);
        }
    }

    static class NegotiationResolver extends DefaultResolver {

        private final String parameterName;

        NegotiationResolver(List<? extends HttpResponseSerializer> serializers,
                            String parameterName, boolean isAnyType) {
            super(serializers, isAnyType);
            this.parameterName = parameterName;
        }

        @Override
        protected List<MediaType> getMediaTypes(AsyncRequest request) {
            // judge by parameter
            final String format = request.getParameter(parameterName);
            if (Constants.NEGOTIATION_JSON_FORMAT.equals(format)) {
                List<MediaType> ret = InternalThreadLocalMap.get().arrayList(1);
                ret.add(MediaType.APPLICATION_JSON);
                return ret;
            } else if (Constants.NEGOTIATION_PROTO_BUF_FORMAT.equals(format)) {
                List<MediaType> ret = InternalThreadLocalMap.get().arrayList(1);
                ret.add(ProtoBufHttpBodySerializer.PROTOBUF);
                return ret;
            } else {
                // fallback to default
                return super.getMediaTypes(request);
            }
        }
    }

}
