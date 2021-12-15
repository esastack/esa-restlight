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

import esa.commons.StringUtils;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.annotation.QueryBean;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.method.FieldParam;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.nav.NameAndValue;

import java.util.function.BiFunction;

/**
 * @see QueryBean
 */
public class QueryBeanParamResolver extends RequestBeanParamResolver {

    public QueryBeanParamResolver(DeployContext<? extends RestlightOptions> ctx) {
        super(ctx);
    }

    @Override
    public boolean supports(Param param) {
        return param.hasAnnotation(QueryBean.class);
    }

    @Override
    protected RequestBeanParamResolver.TypeMeta newTypeMeta(Class<?> type, HandlerResolverFactory resolverFactory) {
        return new TypeMeta(type, resolverFactory);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private static class TypeMeta extends RequestBeanParamResolver.TypeMeta {

        private TypeMeta(Class<?> c, HandlerResolverFactory resolverFactory) {
            super(c, resolverFactory);
        }

        @Override
        protected ParamResolver findResolver(FieldParam fieldParam, HandlerResolverFactory resolverFactory) {
            return AlwaysUseParamArgumentResolver.INSTANCE.createResolver(fieldParam, resolverFactory);
        }
    }

    private static class AlwaysUseParamArgumentResolver extends AbstractParamResolver {

        private static final AlwaysUseParamArgumentResolver INSTANCE = new AlwaysUseParamArgumentResolver();

        @Override
        public boolean supports(Param param) {
            // always return true
            return true;
        }

        @Override
        protected NameAndValue createNameAndValue(Param param, BiFunction<String, Boolean, Object> defaultValueConverter) {
            String name;
            QueryBean.Name alia = param.getAnnotation(QueryBean.Name.class);
            if (alia != null && !StringUtils.isEmpty(alia.value())) {
                name = alia.value();
            } else {
                name = param.name();
            }
            return new NameAndValue(name, false, null);
        }

        @Override
        public String extractParamName(Param param) {
            QueryBean.Name alia = param.getAnnotation(QueryBean.Name.class);
            if (alia != null && !StringUtils.isEmpty(alia.value())) {
                return alia.value();
            } else {
                return param.name();
            }
        }
    }
}
