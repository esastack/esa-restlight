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
package io.esastack.restlight.ext.multipart.resolver;

import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.core.util.ConverterUtils;
import io.esastack.restlight.ext.multipart.annotation.FormParam;
import io.esastack.restlight.ext.multipart.core.MultipartConfig;

import java.util.List;
import java.util.function.Function;

public class MultipartAttrArgumentResolver implements ParamResolverFactory {

    private final MultipartConfig config;

    public MultipartAttrArgumentResolver(MultipartConfig config) {
        this.config = config;
    }

    @Override
    public boolean supports(Param param) {
        return param.hasAnnotation(FormParam.class);
    }

    @Override
    public ParamResolver createResolver(Param param,
                                        List<? extends HttpRequestSerializer> serializers) {
        return new Resolver(param, config);
    }

    private static class Resolver extends AbstractMultipartParamResolver {

        private final Function<String, Object> converter;

        Resolver(Param param, MultipartConfig config) {
            super(param, config);
            this.converter = ConverterUtils.str2ObjectConverter(param.genericType(), p -> p);
        }

        @Override
        protected Object getParamValue(String name, HttpRequest request) {
            return converter.apply(request.getUncheckedAttribute(PREFIX + name));
        }

        @Override
        protected NameAndValue createNameAndValue(Param param) {
            FormParam formParam = param.getAnnotation(FormParam.class);
            assert formParam != null;
            return new NameAndValue(formParam.value(), formParam.required(),
                    ConverterUtils.normaliseDefaultValue(formParam.defaultValue()));
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
