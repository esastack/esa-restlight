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

import esa.commons.StringUtils;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandledValue;
import io.esastack.restlight.core.resolver.RequestEntity;
import io.esastack.restlight.core.resolver.RequestEntityResolver;
import io.esastack.restlight.core.resolver.RequestEntityResolverFactory;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.serialize.ProtoBufHttpBodySerializer;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.core.util.ConverterUtils;
import io.esastack.restlight.server.bootstrap.WebServerException;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

public abstract class FlexibleRequestEntityResolverFactory implements RequestEntityResolverFactory {

    private final boolean negotiation;
    private final String paramName;

    public FlexibleRequestEntityResolverFactory(boolean negotiation, String paramName) {
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
    public RequestEntityResolver createResolver(Param param,
                                                List<? extends HttpRequestSerializer> serializers) {
        return negotiation
                ? new NegotiationResolver(serializers, param, paramName)
                : new DefaultResolver(serializers, param);
    }

    /**
     * Create an instance of {@link NameAndValue} for the parameter.
     *
     * @param param parameter
     *
     * @return name and value
     */
    protected abstract NameAndValue createNameAndValue(Param param);

    private class DefaultResolver extends AbstractNameAndValueRequestEntityResolver {

        private final List<? extends HttpRequestSerializer> serializers;
        final Function<String, Object> converter;

        private DefaultResolver(List<? extends HttpRequestSerializer> serializers,
                                Param param) {
            super(param);
            this.serializers = serializers;
            this.converter = ConverterUtils.str2ObjectConverter(param.genericType(), p -> p);
        }

        @Override
        protected NameAndValue createNameAndValue(Param param) {
            return FlexibleRequestEntityResolverFactory.this.createNameAndValue(param);
        }

        @Override
        protected HandledValue<Object> readFrom0(String name, Param param, RequestEntity entity) throws Exception {
            MediaType contentType = entity.mediaType();
            //convert argument if content-type is text/plain or missing.
            if (contentType == null || MediaTypeUtil.TEXT_PLAIN.isCompatibleWith(contentType)) {
                //ignore empty body.
                if (entity.inputStream().readBytes() == 0) {
                    return HandledValue.succeed(null);
                }
                return HandledValue.succeed(converter.apply(new String(entity.byteData(), StandardCharsets.UTF_8)));
            }

            //search serializer to resolve argument
            HandledValue<Object> handled;
            for (HttpRequestSerializer serializer : serializers) {
                if ((handled = serializer.deserialize(entity)).isSuccess()) {
                    return handled;
                }
            }
            throw new WebServerException(HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Unsupported media type:" + contentType);
        }

        protected MediaType getMediaType(HttpRequest request) {
            String contentTypeStr = request.getHeader(HttpHeaderNames.CONTENT_TYPE);
            return StringUtils.isEmpty(contentTypeStr) ? null :
                    MediaTypeUtil.valueOf(contentTypeStr);
        }
    }

    private class NegotiationResolver extends DefaultResolver {

        private final String paramName;

        private NegotiationResolver(List<? extends HttpRequestSerializer> serializers,
                                    Param param,
                                    String paramName) {
            super(serializers, param);
            this.paramName = paramName;
        }

        @Override
        protected MediaType getMediaType(HttpRequest request) {
            // judge by param
            final String format = request.getParameter(paramName);
            if (Constants.NEGOTIATION_JSON_FORMAT.equals(format)) {
                return MediaTypeUtil.APPLICATION_JSON;
            } else if (Constants.NEGOTIATION_PROTO_BUF_FORMAT.equals(format)) {
                return ProtoBufHttpBodySerializer.PROTOBUF;
            } else {
                // fallback to default
                return super.getMediaType(request);
            }
        }
    }
}

