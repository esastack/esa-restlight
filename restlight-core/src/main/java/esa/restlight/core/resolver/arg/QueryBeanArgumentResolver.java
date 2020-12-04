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
package esa.restlight.core.resolver.arg;

import esa.commons.StringUtils;
import esa.restlight.core.DeployContext;
import esa.restlight.core.annotation.QueryBean;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.method.FieldParam;
import esa.restlight.core.method.Param;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.core.resolver.HandlerResolverFactory;

/**
 * @see QueryBean
 */
public class QueryBeanArgumentResolver extends RequestBeanArgumentResolver {

    public QueryBeanArgumentResolver(DeployContext<? extends RestlightOptions> ctx) {
        super(ctx);
    }

    @Override
    public boolean supports(Param param) {
        return param.hasAnnotation(QueryBean.class);
    }

    @Override
    protected RequestBeanArgumentResolver.TypeMeta newTypeMeta(Class<?> type, HandlerResolverFactory resolverFactory) {
        return new TypeMeta(type, resolverFactory);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private static class TypeMeta extends RequestBeanArgumentResolver.TypeMeta {

        TypeMeta(Class<?> c, HandlerResolverFactory resolverFactory) {
            super(c, resolverFactory);
        }

        @Override
        protected ArgumentResolver findResolver(FieldParam fieldParam, HandlerResolverFactory resolverFactory) {
            return AlwaysUseParamArgumentResolver.INSTANCE.createResolver(fieldParam, resolverFactory.rxSerializers());
        }
    }

    private static class AlwaysUseParamArgumentResolver extends AbstractParamArgumentResolver {

        private static final AlwaysUseParamArgumentResolver INSTANCE = new AlwaysUseParamArgumentResolver();

        @Override
        protected NameAndValue createNameAndValue(Param param) {
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
        public boolean supports(Param param) {
            // always return true
            return true;
        }
    }
}
