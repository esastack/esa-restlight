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

import esa.commons.StringUtils;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.ext.multipart.annotation.UploadFile;
import io.esastack.restlight.ext.multipart.core.MultipartConfig;
import io.esastack.restlight.ext.multipart.core.MultipartFile;

import java.util.LinkedList;
import java.util.List;

public class MultipartFileArgumentResolver implements ParamResolverFactory {

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
    public ParamResolver createResolver(Param param,
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
        protected Object getParamValue(String name, RequestContext context) {
            final List<MultipartFile> files = context.attr(MULTIPART_FILES).get();
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
