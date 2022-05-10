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
package io.esastack.restlight.core.resolver.entity.request;

import esa.commons.Result;
import esa.commons.StringUtils;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.context.RequestEntity;
import io.esastack.restlight.core.resolver.converter.StringConverter;
import io.esastack.restlight.core.resolver.converter.StringConverterProvider;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.serialize.ProtoBufHttpBodySerializer;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.core.exception.WebServerException;
import io.esastack.restlight.core.context.RequestContext;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Supplier;

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
                                                StringConverterProvider converters,
                                                List<? extends HttpRequestSerializer> serializers) {
        if (param.isMethodParam()
                && param.methodParam().method().getParameterCount() == 1
                && byte[].class.isAssignableFrom(param.type())) {
            return (entity, context) -> Result.ok(context.request().body().getBytes());
        }
        StringConverter converter = converters.get(StringConverterProvider.Key.from(param));
        if (converter == null) {
            converter = value -> value;
        }
        return negotiation
                ? new NegotiationResolver(converter, serializers, param, paramName)
                : new DefaultResolver(converter, serializers, param);
    }

    /**
     * Creates {@link NameAndValue} for given {@link Param}.
     *
     * @param param param
     * @return NameAndValue
     */
    protected abstract NameAndValue<String> createNameAndValue(Param param);

    @Override
    public int getOrder() {
        return 100;
    }

    protected static Result<?, Void> checkRequired(NameAndValue<String> nav,
                                                   StringConverter converter,
                                                   Result<?, Void> handled) {
        if (handled.isOk() && handled.get() != null) {
            return handled;
        }
        Supplier<String> defaultValue;
        if ((defaultValue = nav.defaultValue()) != null) {
            return Result.ok(converter.fromString(defaultValue.get()));
        } else if (nav.required()) {
            throw WebServerException.badRequest("Missing required value: " + nav.name());
        }
        return handled;
    }

    private class DefaultResolver implements RequestEntityResolver {

        private final NameAndValue<String> nav;
        private final StringConverter converter;
        private final List<? extends HttpRequestSerializer> serializers;

        private DefaultResolver(StringConverter converter,
                                List<? extends HttpRequestSerializer> serializers,
                                Param param) {
            this.serializers = serializers;
            this.nav = createNameAndValue(param);
            this.converter = converter;
        }

        @Override
        public Result<?, Void> readFrom(RequestEntity entity, RequestContext context)
                throws Exception {
            MediaType contentType = getMediaType(entity, context);
            //convert argument if content-type is text/plain or missing.
            if (contentType == null || MediaType.TEXT_PLAIN.isCompatibleWith(contentType)) {
                //ignore empty body.
                if (entity.inputStream().available() == 0) {
                    return checkRequired(nav, converter, Result.ok());
                }
                return checkRequired(nav, converter, Result.ok(converter
                        .fromString(context.request().body().string(StandardCharsets.UTF_8))));
            }

            //search serializer to resolve argument
            Result<?, Void> handled;
            for (HttpRequestSerializer serializer : serializers) {
                if ((handled = serializer.deserialize(entity)).isOk()) {
                    return checkRequired(nav, converter, handled);
                }
            }
            return checkRequired(nav, converter, Result.err());
        }

        protected MediaType getMediaType(RequestEntity entity, RequestContext ctx) {
            return entity.mediaType();
        }
    }

    private class NegotiationResolver extends DefaultResolver {

        private final String paramName;

        private NegotiationResolver(StringConverter converter,
                                    List<? extends HttpRequestSerializer> serializers,
                                    Param param,
                                    String paramName) {
            super(converter, serializers, param);
            this.paramName = paramName;
        }

        @Override
        protected MediaType getMediaType(RequestEntity entity, RequestContext ctx) {
            // judge by param
            final String format = ctx.request().getParam(paramName);
            if (Constants.NEGOTIATION_JSON_FORMAT.equals(format)) {
                entity.mediaType(MediaType.APPLICATION_JSON);
            } else if (Constants.NEGOTIATION_PROTO_BUF_FORMAT.equals(format)) {
                entity.mediaType(ProtoBufHttpBodySerializer.PROTOBUF);
            }
            return super.getMediaType(entity, ctx);
        }
    }
}

