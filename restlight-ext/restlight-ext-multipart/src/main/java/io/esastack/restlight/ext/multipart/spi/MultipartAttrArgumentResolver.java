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
package io.esastack.restlight.ext.multipart.spi;

import esa.commons.collection.AttributeKey;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.nav.NameAndStringValueResolver;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolver;
import io.esastack.restlight.core.util.ConverterUtils;
import io.esastack.restlight.ext.multipart.annotation.FormParam;

public class MultipartAttrArgumentResolver extends AbstractMultipartParamResolver {

    @Override
    public boolean supports(Param param) {
        return param.hasAnnotation(FormParam.class);
    }

    @Override
    protected NameAndValueResolver doCreateResolver(Param param, HandlerResolverFactory resolverFactory) {
        return new NameAndStringValueResolver(
                param,
                resolverFactory,
                (name, ctx) -> ctx.attr(AttributeKey.stringKey(PREFIX + name)).get(),
                createNameAndValue(param)
        );
    }

    private NameAndValue<String> createNameAndValue(Param param) {
        FormParam formParam = param.getAnnotation(FormParam.class);
        assert formParam != null;
        return new NameAndValue<>(formParam.value(),
                formParam.required(),
                ConverterUtils.normaliseDefaultValue(formParam.defaultValue()));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
