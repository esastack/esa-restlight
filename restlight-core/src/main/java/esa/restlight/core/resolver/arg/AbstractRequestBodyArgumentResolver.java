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

import esa.commons.StringUtils;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.method.Param;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.serialize.HttpRequestSerializer;
import esa.restlight.core.serialize.ProtoBufHttpBodySerializer;
import esa.restlight.core.util.Constants;
import esa.restlight.core.util.ConverterUtils;
import esa.restlight.core.util.MediaType;
import esa.restlight.server.bootstrap.WebServerException;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

/**
 * Implementation of {@link ArgumentResolverFactory} for resolving argument that annotated by the RequestBody.
 */
public abstract class AbstractRequestBodyArgumentResolver implements ArgumentResolverFactory {

    private final boolean negotiation;
    private final String paramName;

    public AbstractRequestBodyArgumentResolver() {
        this(false, null);
    }

    public AbstractRequestBodyArgumentResolver(boolean negotiation, String paramName) {
        this.negotiation = negotiation;
        this.paramName = StringUtils.nonEmptyOrElse(paramName,
                Constants.DEFAULT_NEGOTIATION_FORMAT_PARAMETER);
    }

    @Override
    public boolean supports(Param param) {
        // current parameter only
        return param.isMethodParam() && param.methodParam().method().getParameterCount() == 1;
    }

    @Override
    public ArgumentResolver createResolver(Param param,
                                           List<? extends HttpRequestSerializer> serializers) {
        return negotiation
                ? new NegotiationResolver(serializers, param, paramName, required(param), defaultValue(param))
                : new DefaultResolver(serializers, param, required(param), defaultValue(param));
    }

    protected abstract boolean required(Param param);

    protected String defaultValue(Param param) {
        return null;
    }

    private static class DefaultResolver implements ArgumentResolver {

        private final List<? extends HttpRequestSerializer> serializers;
        final boolean required;
        final Object defaultValue;
        final Param param;
        final Function<String, Object> converter;

        private DefaultResolver(List<? extends HttpRequestSerializer> serializers,
                                Param param,
                                boolean required,
                                String defaultValue) {
            this.serializers = serializers;
            // should be not null
            this.required = required;
            this.param = param;
            this.converter = ConverterUtils.str2ObjectConverter(param.genericType(), p -> p);
            this.defaultValue =
                    ConverterUtils.forceConvertStringValue(defaultValue, param.genericType());
        }

        @Override
        public Object resolve(AsyncRequest request, AsyncResponse response) throws Exception {
            MediaType contentType = getMediaType(request);
            //convert argument if content-type is text/plain or missing.
            if (contentType == null || MediaType.TEXT_PLAIN.isCompatibleWith(contentType)) {
                //ignore empty body.
                if (request.byteBufBody().readableBytes() == 0) {
                    return checkRequired(null);
                }
                return checkRequired(converter.apply(request.byteBufBody()
                        .toString(StandardCharsets.UTF_8)));
            }

            //search serializer to resolve argument
            for (HttpRequestSerializer serializer : serializers) {
                if (!serializer.supportsRead(contentType, param.genericType())) {
                    continue;
                }
                return checkRequired(readArgFromWithSerializer(request, param, serializer));
            }
            throw new WebServerException(HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Unsupported media type:" + contentType.toString());
        }

        protected MediaType getMediaType(AsyncRequest request) {
            String contentTypeStr = request.getHeader(HttpHeaderNames.CONTENT_TYPE);
            return StringUtils.isEmpty(contentTypeStr) ? null :
                    MediaType.valueOf(contentTypeStr);
        }

        private Object checkRequired(Object arg) {
            if (arg == null) {
                if (defaultValue == null) {
                    if (required) {
                        throw WebServerException.badRequest("Missing required value: " + param.name());
                    }
                } else {
                    return defaultValue;
                }
            }
            return arg;
        }

        private Object readArgFromWithSerializer(AsyncRequest request,
                                                 Param param,
                                                 HttpRequestSerializer serializer) throws Exception {
            if (serializer.preferStream()) {
                return serializer.deserialize(request.inputStream(), param.genericType());
            }
            return serializer.deserialize(request.body(), param.genericType());
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private static class NegotiationResolver extends DefaultResolver {

        private final String paramName;

        private NegotiationResolver(List<? extends HttpRequestSerializer> serializers,
                                    Param param,
                                    String paramName,
                                    boolean required,
                                    String defaultValue) {
            super(serializers, param, required, defaultValue);
            this.paramName = paramName;
        }

        @Override
        protected MediaType getMediaType(AsyncRequest request) {
            // judge by param
            final String format = request.getParameter(paramName);
            if (Constants.NEGOTIATION_JSON_FORMAT.equals(format)) {

                return MediaType.APPLICATION_JSON;
            } else if (Constants.NEGOTIATION_PROTO_BUF_FORMAT.equals(format)) {
                return ProtoBufHttpBodySerializer.PROTOBUF;
            } else {
                // fallback to default
                return super.getMediaType(request);
            }
        }
    }
}
