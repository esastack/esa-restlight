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
import esa.commons.function.Function3;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolver;
import io.esastack.restlight.ext.multipart.annotation.UploadFile;
import io.esastack.restlight.ext.multipart.core.MultipartFile;
import io.esastack.restlight.server.context.RequestContext;
import io.netty.handler.codec.http.multipart.HttpDataFactory;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import static io.esastack.restlight.ext.multipart.spi.AbstractMultipartParamResolver.MULTIPART_FILES;

public class MultipartFileParamResolverProvider extends AbstractMultipartParamResolverProvider {

    @Override
    protected AbstractMultipartParamResolver createResolver(HttpDataFactory factory) {
        return new MultipartFileParamResolver(factory);
    }

    final class MultipartFileParamResolver extends AbstractMultipartParamResolver {

        MultipartFileParamResolver(HttpDataFactory factory) {
            super(factory);
        }

        @Override
        public boolean supports(Param param) {
            return param.hasAnnotation(UploadFile.class)
                    && (MultipartFile.class.isAssignableFrom(param.type())
                    || List.class.isAssignableFrom(param.type()));
        }

        @Override
        protected NameAndValueResolver doCreateResolver(Param param,
                                                        Function3<Class<?>, Type, Param, StringConverter>
                                                                converterFunc) {
            if (MultipartFile.class.isAssignableFrom(param.type())) {
                return new SingleFileResolver();
            }
            if (List.class.isAssignableFrom(param.type())) {
                return new FilesResolver();
            }
            throw new IllegalStateException("Unexpected");
        }
    }

    private NameAndValue<Object> createNameAndValue(Param param) {
        UploadFile uploadFile = param.getAnnotation(UploadFile.class);
        assert uploadFile != null;
        return new NameAndValue<>(uploadFile.value(), uploadFile.required());
    }

    private List<MultipartFile> extractFiles(String name, RequestContext ctx) {
        final List<MultipartFile> files = ctx.attrs().attr(MULTIPART_FILES).get();
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
    }

    private final class SingleFileResolver implements NameAndValueResolver {

        @Override
        public Object resolve(String name, RequestContext ctx) {
            List<MultipartFile> files = extractFiles(name, ctx);
            if (files == null) {
                return null;
            }
            return files.isEmpty() ? null : files.get(0);
        }

        @Override
        public NameAndValue<Object> createNameAndValue(Param param) {
            return MultipartFileParamResolverProvider.this.createNameAndValue(param);
        }
    }

    private final class FilesResolver implements NameAndValueResolver {

        @Override
        public Object resolve(String name, RequestContext ctx) {
            return extractFiles(name, ctx);
        }

        @Override
        public NameAndValue<Object> createNameAndValue(Param param) {
            return MultipartFileParamResolverProvider.this.createNameAndValue(param);
        }
    }
}
