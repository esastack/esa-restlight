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

import esa.commons.StringUtils;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolver;
import io.esastack.restlight.ext.multipart.annotation.UploadFile;
import io.esastack.restlight.ext.multipart.core.MultipartFile;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MultipartFileArgumentResolver extends AbstractMultipartParamResolver<List<MultipartFile>> {

    private final static NameAndValueResolver.Converter<List<MultipartFile>> singleFileConverter =
            (name, ctx, valueProvider) -> {
                List<MultipartFile> files = valueProvider.apply(name, ctx);
                if (files == null) {
                    return null;
                }
                return files.isEmpty() ? null : files.get(0);
            };

    private final static NameAndValueResolver.Converter<List<MultipartFile>> filesConverter =
            (name, ctx, valueProvider) ->
                    valueProvider.apply(name, ctx);

    @Override
    public boolean supports(Param param) {
        return param.hasAnnotation(UploadFile.class)
                && (MultipartFile.class.isAssignableFrom(param.type())
                || List.class.isAssignableFrom(param.type()));
    }

    @Override
    protected Function<Param, NameAndValue> initNameAndValueCreator(BiFunction<String, Boolean, Object> defaultValueConverter) {
        return (param) -> {
            UploadFile uploadFile = param.getAnnotation(UploadFile.class);
            assert uploadFile != null;
            return new NameAndValue(uploadFile.value(), uploadFile.required(), null);
        };
    }

    @Override
    protected BiFunction<String, Boolean, Object> initDefaultValueConverter(NameAndValueResolver.Converter<List<MultipartFile>> converter) {
        return (defaultValue, isLazy) -> null;
    }

    @Override
    protected BiFunction<String, RequestContext, List<MultipartFile>> doInitValueProvider(Param param) {
        return (name, ctx) -> {
            final List<MultipartFile> files = ctx.request().getUncheckedAttribute(MULTIPART_FILES);
            if (files == null) {
                return null;
            }

            final List<MultipartFile> result = new LinkedList<>();
            for (MultipartFile file : files) {
                if (StringUtils.isEmpty(name) || name.equals(file.filedName())) {
                    result.add(file);
                }
            }
            return result;
        };
    }

    @Override
    protected NameAndValueResolver.Converter<List<MultipartFile>> doInitConverter(Param param,
                                                                                  BiFunction<Class<?>,
                                                                                          Type,
                                                                                          StringConverter> converterLookup) {

        if (MultipartFile.class.isAssignableFrom(param.type())) {
            return singleFileConverter;
        }
        if (List.class.isAssignableFrom(param.type())) {
            return filesConverter;
        }
        throw new IllegalStateException("Unexpected");
    }
}
