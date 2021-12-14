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
package io.esastack.restlight.core.resolver.param;

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.util.ConverterUtils;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Implementation of {@link ParamResolverFactory} for resolving argument that annotated by the RequestHeader.
 */
public abstract class AbstractHeaderParamResolver implements ParamResolverFactory {

    @Override
    public ParamResolver createResolver(Param param,
                                        List<? extends HttpRequestSerializer> serializers) {
        if (HttpHeaders.class.equals(param.type())) {
            return new HeadersResolver(param);
        }
        return new StringOrListResolver(param);
    }

    protected abstract NameAndValue createNameAndValue(Param param);

    private abstract class BaseResolver extends AbstractNameAndValueParamResolver {

        private BaseResolver(Param param) {
            super(param);
        }

        @Override
        protected NameAndValue createNameAndValue(Param param) {
            return AbstractHeaderParamResolver.this.createNameAndValue(param);
        }

    }

    private class StringOrListResolver extends BaseResolver {

        private final Function<String, Object> converter;
        private final Function<Collection<String>, Object> strsConverter;

        private StringOrListResolver(Param param) {
            super(param);
            this.converter = ConverterUtils.str2ObjectConverter(param.genericType(), p -> p);
            this.strsConverter = ConverterUtils.strs2ObjectConverter(param.genericType());
        }

        @Override
        protected Object resolveName(String name, RequestContext context) {
            if (strsConverter != null) {
                List<String> values = context.request().headers().getAll(name);
                if (values == null || values.isEmpty()) {
                    return null;
                } else if (values.size() > 1) {
                    return strsConverter.apply(values);
                } else {
                    return converter.apply(values.get(0));
                }
            } else {
                return converter.apply(context.request().headers().get(name));
            }
        }
    }

    private class HeadersResolver extends BaseResolver {

        private HeadersResolver(Param param) {
            super(param);
        }

        @Override
        protected HttpHeaders resolveName(String name, RequestContext context) {
            return context.request().headers();
        }
    }

}
