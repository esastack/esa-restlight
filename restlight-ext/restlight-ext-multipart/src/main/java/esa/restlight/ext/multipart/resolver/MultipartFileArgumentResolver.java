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
package esa.restlight.ext.multipart.resolver;

import esa.commons.StringUtils;
import esa.httpserver.core.AsyncRequest;
import esa.restlight.core.method.Param;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.resolver.arg.NameAndValue;
import esa.restlight.core.serialize.HttpRequestSerializer;
import esa.restlight.ext.multipart.annotation.UploadFile;
import esa.restlight.ext.multipart.core.MultipartConfig;
import esa.restlight.ext.multipart.core.MultipartFile;

import java.util.LinkedList;
import java.util.List;

public class MultipartFileArgumentResolver implements ArgumentResolverFactory {

    private final MultipartConfig config;

    public MultipartFileArgumentResolver(MultipartConfig config) {
        this.config = config;
    }

    @Override
    public boolean supports(Param param) {
        return param.hasAnnotation(UploadFile.class)
                && (MultipartFile.class.isAssignableFrom(param.type())
                || List.class.isAssignableFrom(param.type()));
    }

    @Override
    public ArgumentResolver createResolver(Param param,
                                           List<? extends HttpRequestSerializer> serializers) {
        if (MultipartFile.class.isAssignableFrom(param.type())) {
            return new SingleFileResolver(param, config);
        }
        if (List.class.isAssignableFrom(param.type())) {
            return new Resolver(param, config);
        }
        throw new IllegalStateException("Unexpected");
    }

    private static class Resolver extends AbstractMultipartParamResolver {

        Resolver(Param param,
                 MultipartConfig config) {
            super(param, config);
        }

        @Override
        Object getParamValue(String name, AsyncRequest request) {
            final List<MultipartFile> files = request.getUncheckedAttribute(MULTIPART_FILES);
            if (files == null) {
                return null;
            }

            final List<MultipartFile> result = new LinkedList<>();
            for (MultipartFile file : files) {
                if (StringUtils.isEmpty(name) || name.equals(file.filedName())) {
                    result.add(file);
                }
            }
            return resolveValue(result);
        }

        Object resolveValue(List<MultipartFile> result) {
            return result;
        }

        @Override
        protected NameAndValue createNameAndValue(Param param) {
            UploadFile uploadFile = param.getAnnotation(UploadFile.class);
            assert uploadFile != null;
            return new NameAndValue(uploadFile.value(), uploadFile.required(), null);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private static class SingleFileResolver extends Resolver {

        SingleFileResolver(Param param,
                           MultipartConfig config) {
            super(param, config);
        }

        @Override
        Object resolveValue(List<MultipartFile> result) {
            return result.isEmpty() ? null : result.get(0);
        }

    }
}
